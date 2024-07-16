package com.qendolin.betterclouds.clouds;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.qendolin.betterclouds.Config;
import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.clouds.shaders.ShaderParameters;
import com.qendolin.betterclouds.compat.*;
import com.qendolin.betterclouds.renderdoc.RenderDoc;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.*;

//? if >=1.21
 import net.minecraft.block.enums.CameraSubmersionType; 

import java.lang.Math;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.qendolin.betterclouds.Main.glCompat;
import static org.lwjgl.opengl.GL32.*;

public class Renderer implements AutoCloseable {
    private final MinecraftClient client;
    private ClientWorld world = null;

    private float cloudsHeight;
    private final Matrix4f mvpMatrix = new Matrix4f();
    private final Matrix4f mvMatrix = new Matrix4f();
    private final Matrix4f pMatrix = new Matrix4f();
    private final Matrix4d pInverseMatrix = new Matrix4d();
    private final Matrix4f rotationProjectionMatrix = new Matrix4f();
    private final Matrix4f tempMatrix = new Matrix4f();
    private final Vector3f tempVector = new Vector3f();
    private final Frustum tempFrustum = new Frustum(new Matrix4f().identity(), new Matrix4f().identity());
    private ShaderParameters shaderParameters = null;

    private final Resources res = new Resources();

    public Renderer(MinecraftClient client) {
        this.client = client;
    }

    public void setWorld(ClientWorld world) {
        this.world = world;
    }

    public void reload(ResourceManager manager) {
        Main.LOGGER.info("Reloading cloud renderer...");
        Main.LOGGER.debug("[1/6] Reloading shaders");
        shaderParameters = createShaderParameters(Main.getConfig());
        res.reloadShaders(manager, shaderParameters);
        Main.LOGGER.debug("[2/6] Reloading generator");
        res.reloadGenerator(isFancyMode());
        Main.LOGGER.debug("[3/6] Reloading textures");
        res.reloadTextures(client);
        Main.LOGGER.debug("[4/6] Reloading primitive meshes");
        res.reloadMeshPrimitives();
        Main.LOGGER.debug("[5/6] Reloading framebuffer");
        res.reloadFramebuffer(scaledFramebufferWidth(), scaledFramebufferHeight());
        Main.LOGGER.debug("[6/6] Reloading timers");
        res.reloadTimer();
        Main.LOGGER.info("Cloud renderer initialized");
    }

    private boolean isFancyMode() {
        return client.options.getCloudRenderModeValue() == CloudRenderMode.FANCY;
    }

    private int scaledFramebufferWidth() {
        return (int) (Main.getConfig().preset().upscaleResolutionFactor * client.getFramebuffer().textureWidth);
    }

    private int scaledFramebufferHeight() {
        return (int) (Main.getConfig().preset().upscaleResolutionFactor * client.getFramebuffer().textureHeight);
    }

    private ShaderParameters createShaderParameters(Config config) {
        return new ShaderParameters(
            client.options.getCloudRenderModeValue(),
            config.blockDistance(),
            config.fadeEdge, config.sizeXZ, config.sizeY, config.celestialBodyHalo,
            glCompat.useDepthWriteFallback(), glCompat.useStencilTextureFallback(),
            DistantHorizonsCompat.instance().isReady() && DistantHorizonsCompat.instance().isEnabled(),
            config.preset().worldCurvatureSize
        );
    }

    public PrepareResult prepare(Matrix4f viewMat, Matrix4f projMat, int ticks, float tickDelta, Vector3d cam) {
        assert RenderSystem.isOnRenderThread();
        client.getProfiler().swap("render_setup");
        Config config = Main.getConfig();

        if (res.failedToLoadCritical()) {
            if (RenderDoc.isFrameCapturing()) glCompat.debugMessage("prepare failed: critical resource not loaded");
            return PrepareResult.FALLBACK;
        }
        if (!config.irisSupport && IrisCompat.instance().isShadersEnabled()) {
            if (RenderDoc.isFrameCapturing()) glCompat.debugMessage("prepare failed: iris support disabled");
            return PrepareResult.FALLBACK;
        }

        // Rendering clouds when underwater was making them very visible in unloaded chunks
        if (client.gameRenderer.getCamera().getSubmersionType() != CameraSubmersionType.NONE) {
            return PrepareResult.NO_RENDER;
        }

        DimensionEffects effects = world.getDimensionEffects();
        cloudsHeight = effects.getCloudsHeight();

        res.generator().bind();
        ShaderParameters currentShaderParameters = createShaderParameters(config);
        if (!Objects.equals(currentShaderParameters, shaderParameters)) {
            shaderParameters = currentShaderParameters;
            res.reloadShaders(client.getResourceManager(), shaderParameters);
        }
        res.generator().reallocateIfStale(config, isFancyMode());

        float raininess = Math.max(0.6f * getTrueRainGradient(tickDelta), getTrueThunderGradient(tickDelta));
        float cloudiness = raininess * 0.3f + 0.5f;

        res.generator().update(cam, ticks + tickDelta, Main.getConfig(), cloudiness);
        if (res.generator().canGenerate() && !res.generator().generating() && !Debug.generatorPause) {
            client.getProfiler().swap("generate_clouds");
            res.generator().generate();
            client.getProfiler().swap("render_setup");
        }

        if (res.generator().canSwap()) {
            client.getProfiler().swap("swap");
            res.generator().swap();
            client.getProfiler().swap("render_setup");
        }

        tempMatrix.set(viewMat);

        rotationProjectionMatrix.set(projMat);
        // This is fixes issue #14, not entirely sure why, but it forces the matrix to be homogenous
        tempMatrix.m30(0);
        tempMatrix.m31(0);
        tempMatrix.m32(0);
        tempMatrix.m33(0);
        tempMatrix.m23(0);
        tempMatrix.m13(0);
        tempMatrix.m03(0);
        rotationProjectionMatrix.mul(tempMatrix);

        tempMatrix.translate((float) res.generator().renderOriginX(cam.x), (float) (cloudsHeight - cam.y), (float) res.generator().renderOriginZ(cam.z));
        tempMatrix.m33(1);

        pMatrix.set(projMat);
        pInverseMatrix.set(projMat);
        pInverseMatrix.invert();

        mvMatrix.set(tempMatrix);
        mvpMatrix.set(projMat);
        mvpMatrix.mul(mvMatrix);

        return PrepareResult.RENDER;
    }

    // Don't forget to push / pop matrix stack outside
    // Note: render must not return early, this will cause corruption because prepare binds stuff
    public void render(int ticks, float tickDelta, Vector3d cam, Vector3d frustumPos, Frustum frustum) {
        client.getProfiler().swap("render_setup");
        if (Main.isProfilingEnabled()) {
            if (res.timer() == null) res.reloadTimer();
            res.timer().start();
        }

        Config config = Main.getConfig();

        if (isFramebufferStale()) {
            res.reloadFramebuffer(scaledFramebufferWidth(), scaledFramebufferHeight());
        }

        RenderSystem.viewport(0, 0, res.fboWidth(), res.fboHeight());
        GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, res.oitFbo());
        RenderSystem.clearColor(0, 0, 0, 0);
        RenderSystem.clearDepth(1);

        client.getProfiler().swap("draw_coverage");
        drawCoverage(ticks + tickDelta, cam, frustumPos, frustum);


        client.getProfiler().swap("draw_shading");

        RenderPhase renderPhase = null;
        if (IrisCompat.instance().isShadersEnabled() && config.useIrisFBO) {
            IrisCompat.instance().bindFramebuffer();
        } else {
            client.getFramebuffer().beginWrite(false);
            renderPhase = RenderPhase.CLOUDS_TARGET;
            renderPhase.startDrawing();
        }

        drawShading(cam, tickDelta);


        client.getProfiler().swap("render_cleanup");
        res.generator().unbind();
        Resources.unbindShader();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(GL_LEQUAL);
        RenderSystem.activeTexture(GL_TEXTURE0);
        RenderSystem.colorMask(true, true, true, true);

        if(renderPhase != null) {
            renderPhase.endDrawing();
        }

        if (!glCompat.useStencilTextureFallback()) {
            glDisable(GL_STENCIL_TEST);
            glStencilFunc(GL_ALWAYS, 0x0, 0xff);
            glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
        }

        if (Debug.frustumCulling) {
            glCompat.pushDebugGroupDev("Frustum Culling Debug Draw");
            Debug.drawFrustumCulledBoxes(cam);
            glCompat.popDebugGroupDev();
        }

        if (Main.isProfilingEnabled() && res.timer() != null) {
            res.timer().stop();

            if (res.timer().frames() >= Debug.profileInterval) {
                List<Double> times = res.timer().get();
                times.sort(Double::compare);
                double median = times.get(times.size() / 2);
                double p25 = times.get((int) Math.ceil(times.size() * 0.25));
                double p75 = times.get((int) Math.ceil(times.size() * 0.75));
                double min = times.get(0);
                double max = times.get(times.size() - 1);
                double average = times.stream().mapToDouble(d -> d).average().orElse(0);
                Main.debugChatMessage("profiling.gpuTimes", min, average, max, p25, median, p75);
                res.timer().reset();
            }
        }
    }

    private boolean isFramebufferStale() {
        return res.fboWidth() != scaledFramebufferWidth() || res.fboHeight() != scaledFramebufferHeight();
    }

    private void drawCoverage(float ticks, Vector3d cam, Vector3d frustumPos, Frustum frustum) {

        RenderSystem.enableDepthTest();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);

        if (glCompat.useStencilTextureFallback()) {
            RenderSystem.depthFunc(GL_ALWAYS);
            RenderSystem.enableBlend();
            RenderSystem.blendEquation(GL_FUNC_ADD);
            // FIXME: buf0 needs depth sorting
            glCompat.blendFunci(0, GL_ONE, GL_ZERO);
            glCompat.blendFunci(1, GL_ONE, GL_ONE);
            glDisable(GL_STENCIL_TEST);
        } else {
            RenderSystem.depthFunc(GL_LESS);
            RenderSystem.disableBlend();
            glEnable(GL_STENCIL_TEST);
            glStencilMask(0xff);
            glClearStencil(0);
            glStencilOp(GL_KEEP, GL_INCR, GL_INCR);
            glStencilFunc(GL_ALWAYS, 0xff, 0xff);
        }

        if (isFancyMode()) RenderSystem.enableCull();
        else RenderSystem.disableCull();
        glClear(GL_STENCIL_BUFFER_BIT | GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        Config generatorConfig = getGeneratorConfig();
        Config config = Main.getConfig();

        res.coverageShader().bind();
        res.coverageShader().uMVPMatrix.setMat4(mvpMatrix);
        res.coverageShader().uOriginOffset.setVec3((float) -res.generator().renderOriginX(cam.x), (float) cam.y - cloudsHeight, (float) -res.generator().renderOriginZ(cam.z));
        res.coverageShader().uBoundingBox.setVec4((float) cam.x, (float) cam.z, generatorConfig.blockDistance() - generatorConfig.chunkSize / 2f, generatorConfig.yRange + config.sizeY);
        res.coverageShader().uTime.setFloat(ticks / 20);
        res.coverageShader().uMiscellaneous.setVec3(config.scaleFalloffMin, config.windEffectFactor, config.windSpeedFactor);
        FogShape shape = RenderSystem.getShaderFogShape();
        if (shape == FogShape.CYLINDER) {
            res.coverageShader().uFogRange.setVec2(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        } else {
            res.coverageShader().uFogRange.setVec2(RenderSystem.getShaderFogStart(), RenderSystem.getShaderFogEnd());
        }

        // "Fast" computation of the near and far plane doesn't work because of nausea and view bobbing.
        // This is slightly slower but should always give the correct results.
        Vector4d farPlane = new Vector4d(0, 0, 1, 1);
        Vector4d nearPlane = new Vector4d(0, 0, -1, 1);
        pInverseMatrix.transform(farPlane);
        pInverseMatrix.transform(nearPlane);
        res.coverageShader().uDepthRange.setVec2((float) (-nearPlane.z / nearPlane.w), (float) (-farPlane.z / farPlane.w));

        RenderSystem.activeTexture(GL_TEXTURE0);
        RenderSystem.bindTexture(client.getFramebuffer().getDepthAttachment());

        // Distant Horizons compat
        if (DistantHorizonsCompat.instance().isReady() && DistantHorizonsCompat.instance().isEnabled()) {
            res.coverageShader().uMVMatrix.setMat4(mvMatrix);
            res.coverageShader().uMcPMatrix.setMat4(pMatrix);

            Optional<Integer> depthId = DistantHorizonsCompat.instance().getDepthTextureId();
            RenderSystem.activeTexture(GL_TEXTURE6);
            if (depthId.isPresent()) {
                Matrix4f dhProjectionMatrix = DistantHorizonsCompat.instance().getProjectionMatrix();
                RenderSystem.bindTexture(depthId.get());
                res.coverageShader().uDhPMatrix.setMat4(dhProjectionMatrix);
            } else {
                RenderSystem.bindTexture(0);
                res.coverageShader().uDhPMatrix.setMat4(DistantHorizonsCompat.NOOP_MATRIX);
            }
        }

        RenderSystem.activeTexture(GL_TEXTURE5);
        client.getTextureManager().getTexture(Resources.NOISE_TEXTURE).bindTexture();

        res.generator().bind();
        if (glCompat.useBaseInstanceFallback()) {
            res.generator().buffer().bindDrawBuffer();
        }

        setFrustumTo(tempFrustum, frustum);
        Frustum frustumAtOrigin = tempFrustum;
        frustumAtOrigin.setPosition(frustumPos.x - res.generator().originX(), frustumPos.y, frustumPos.z - res.generator().originZ());
        Debug.clearFrustumCulledBoxed();

        if (!res.generator().canRender()) {
            RenderSystem.enableCull();
            return;
        }

        int runStart = -1;
        int runCount = 0;
        for (ChunkedGenerator.ChunkIndex chunk : res.generator().chunks()) {
            boolean frustumCulling = config.useFrustumCulling && config.preset().worldCurvatureSize == 0;
            Box bounds = chunk.bounds(cloudsHeight, config.sizeXZ, config.sizeY);
            if (frustumCulling && !frustumAtOrigin.isVisible(bounds)) {
                Debug.addFrustumCulledBox(bounds, false);
                if (runCount != 0) {
                    if (glCompat.useBaseInstanceFallback()) {
                        res.generator().buffer().setVAPointerToInstance(runStart);
                    }
                    glCompat.drawArraysInstancedBaseInstanceFallback(GL_TRIANGLE_STRIP, 0, res.generator().instanceVertexCount(), runCount, runStart);
                }
                runStart = -1;
                runCount = 0;
            } else {
                Debug.addFrustumCulledBox(bounds, true);
                if (runStart == -1) runStart = chunk.start();
                runCount += chunk.count();
            }
        }
        if (runCount != 0) {
            if (glCompat.useBaseInstanceFallback()) {
                res.generator().buffer().setVAPointerToInstance(runStart);
            }
            glCompat.drawArraysInstancedBaseInstanceFallback(GL_TRIANGLE_STRIP, 0, res.generator().instanceVertexCount(), runCount, runStart);
        }

        RenderSystem.enableCull();
    }

    private void drawShading(Vector3d cam, float tickDelta) {
        Config config = Main.getConfig();
        RenderSystem.depthFunc(GL_LESS);

        if (!glCompat.useDepthWriteFallback()) {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }

        RenderSystem.enableBlend();
        RenderSystem.blendEquation(GL_FUNC_ADD);
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.colorMask(false, false, false, false);
        glColorMaski(0, true, true, true, true);
        if (!glCompat.useStencilTextureFallback()) {
            glDisable(GL_STENCIL_TEST);
        }

        RenderSystem.activeTexture(GL_TEXTURE1);
        if (glCompat.useDepthWriteFallback()) {
            RenderSystem.bindTexture(0);
        } else {
            RenderSystem.bindTexture(res.oitCoverageDepthTexture());
        }
        RenderSystem.activeTexture(GL_TEXTURE2);
        RenderSystem.bindTexture(res.oitDataTexture());
        RenderSystem.activeTexture(GL_TEXTURE3);
        RenderSystem.bindTexture(res.oitCoverageTexture());
        RenderSystem.activeTexture(GL_TEXTURE4);
        client.getTextureManager().getTexture(Resources.LIGHTING_TEXTURE).bindTexture();

        float effectLuma = getEffectLuminance(cam, tickDelta);
        long skyTime = world.getLunarTime() % 24000;
        float skyAngleRad = world.getSkyAngleRadians(tickDelta);
        float sunPathAngleRad = (float) Math.toRadians(config.preset().sunPathAngle);
        float dayNightFactor = interpolateDayNightFactor(skyTime, config.preset().sunriseStartTime, config.preset().sunriseEndTime, config.preset().sunsetStartTime, config.preset().sunsetEndTime);
        float brightness = (1 - dayNightFactor) * config.preset().nightBrightness + dayNightFactor * config.preset().dayBrightness;
        float sunAxisY = MathHelper.sin(sunPathAngleRad);
        float sunAxisZ = MathHelper.cos(sunPathAngleRad);
        Vector3f sunDir = tempVector.set(1, 0, 0).rotateAxis(skyAngleRad + MathHelper.HALF_PI, 0, sunAxisY, sunAxisZ);

        // TODO: fit light gradient rotation to configured sunset / sunrise values. Solas shader looks weird at sunset
        res.shadingShader().bind();
        res.shadingShader().uVPMatrix.setMat4(rotationProjectionMatrix);
        res.shadingShader().uSunDirection.setVec4(sunDir.x, sunDir.y, sunDir.z, (world.getTimeOfDay() % 24000) / 24000f);
        res.shadingShader().uSunAxis.setVec3(0, sunAxisY, sunAxisZ);
        res.shadingShader().uOpacity.setVec3(config.preset().opacity, config.preset().opacityFactor, config.preset().opacityExponent);
        res.shadingShader().uColorGrading.setVec4(brightness, 1f / config.preset().gamma(), effectLuma, config.preset().saturation);
        res.shadingShader().uTint.setVec3(config.preset().tintRed, config.preset().tintGreen, config.preset().tintBlue);
        res.shadingShader().uNoiseFactor.setFloat(config.colorVariationFactor);

        glBindVertexArray(res.cubeVao());
        glDrawArrays(GL_TRIANGLES, 0, Mesh.CUBE_MESH_VERTEX_COUNT);

        if (glCompat.useDepthWriteFallback()) {
            RenderSystem.activeTexture(GL_TEXTURE6);
            RenderSystem.bindTexture(res.oitCoverageDepthTexture());
            glTexParameteri(GL_TEXTURE_2D, glCompat.GL_DEPTH_STENCIL_TEXTURE_MODE, GL_DEPTH_COMPONENT);
            res.depthShader().bind();
            glDrawArrays(GL_TRIANGLES, 0, Mesh.QUAD_MESH_VERTEX_COUNT);
            glTexParameteri(GL_TEXTURE_2D, glCompat.GL_DEPTH_STENCIL_TEXTURE_MODE, GL_STENCIL_INDEX);
        }
    }

    private Config getGeneratorConfig() {
        Config config = res.generator().config();
        if (config != null) return config;
        return Main.getConfig();
    }

    private void setFrustumTo(Frustum dst, Frustum src) {
        dst.frustumIntersection = src.frustumIntersection;
        dst.positionProjectionMatrix.set(src.positionProjectionMatrix);
        dst.x = src.x;
        dst.y = src.y;
        dst.z = src.z;
        dst.recession = src.recession;
    }

    private float getEffectLuminance(Vector3d cam, float tickDelta) {
        //? if >=1.21 {
        BackgroundRenderer.applyFogColor();
        //?} else
        /*BackgroundRenderer.setFogBlack();*/

        float[] fogRgb = RenderSystem.getShaderFogColor();
        Vec3d fogColor = new Vec3d(fogRgb[0], fogRgb[1], fogRgb[2]);
        Vec3d skyColor = world.getSkyColor(new Vec3d(cam.x, cam.y, cam.z), tickDelta);
        Vec3d cloudsColor = world.getCloudsColor(tickDelta);

        Vec3d color = new Vec3d(
            (cloudsColor.x * 2 + skyColor.x * 1.5786 + fogColor.x * 1.2458) / (2 + 1 + 1),
            (cloudsColor.y * 2 + skyColor.y * 1.5786 + fogColor.y * 1.2458) / (2 + 1 + 1),
            (cloudsColor.z * 2 + skyColor.z * 1.5786 + fogColor.z * 1.2458) / (2 + 1 + 1)
        );
        double luma = color.x * 0.299 + color.y * 0.587 + color.z * 0.114;
        return (float) MathHelper.clamp(luma * 0.95 + 0.05, 0.0, 1.0);
    }

    private float getTrueRainGradient(float tickDelta) {
        if (HeadInTheCloudsCompat.IS_LOADED) {
            return ((WorldDuck) world).betterclouds$getOriginalRainGradient(tickDelta);
        }
        return world.getRainGradient(tickDelta);
    }

    private float getTrueThunderGradient(float tickDelta) {
        if (HeadInTheCloudsCompat.IS_LOADED) {
            return ((WorldDuck) world).betterclouds$getOriginalThunderGradient(tickDelta);
        }
        return world.getThunderGradient(tickDelta);
    }

    private float interpolateDayNightFactor(float time, float riseStart, float riseEnd, float setStart, float setEnd) {
        if (time <= 6000 || time > 18000) {
            // sunrise time
            if (time > 18000) time -= 24000;
            return smoothstep(time, riseStart, riseEnd);
        } else {
            // sunset time
            return 1 - smoothstep(time, setStart, setEnd);
        }
    }

    private float smoothstep(float x, float e0, float e1) {
        x = MathHelper.clamp((x - e0) / (e1 - e0), 0, 1);
        return x * x * (3 - 2 * x);
    }

    public void close() {
        res.close();
    }

    public enum PrepareResult {
        RENDER, NO_RENDER, FALLBACK
    }
}
