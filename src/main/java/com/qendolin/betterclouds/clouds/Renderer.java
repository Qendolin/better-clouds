package com.qendolin.betterclouds.clouds;

import com.mojang.blaze3d.systems.RenderSystem;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.clouds.fog.FogProvider;
import com.qendolin.betterclouds.clouds.shaders.ShaderParameters;
import com.qendolin.betterclouds.compat.ArsNouveauCompat;
import com.qendolin.betterclouds.compat.DistantHorizonsCompat;
import com.qendolin.betterclouds.compat.IrisCompat;
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.renderdoc.RenderDoc;
import com.qendolin.betterclouds.util.ChatUtil;
import com.qendolin.betterclouds.util.MathUtil;
import com.qendolin.betterclouds.util.RenderHelper;
import net.minecraft.client.MinecraftClient;
//? if <1.21.6 {
/*import net.minecraft.client.render.DimensionEffects;
*///?}
import net.minecraft.client.render.Frustum;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.*;
//? if >=1.21.11 {
import net.minecraft.world.attribute.EnvironmentAttributes;
//?}
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

//? if >=1.21 {
import net.minecraft.block.enums.CameraSubmersionType;
//?} else {
/*import net.minecraft.client.render.CameraSubmersionType;
*///?}

//? if >=1.21.5 {
import com.mojang.blaze3d.opengl.GlStateManager;
//?} else {
/*import com.mojang.blaze3d.platform.GlStateManager;
*///?}

import static com.qendolin.betterclouds.compat.GLCompat.glCompat;
import static com.qendolin.betterclouds.compat.ProfilerWrapper.getProfiler;
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
        BetterCloudsStatic.getLogger().info("Reloading cloud renderer...");
        BetterCloudsStatic.getLogger().debug("[1/6] Reloading shaders");
        shaderParameters = createShaderParameters(ConfigManager.instance());
        res.reloadShaders(manager, shaderParameters);
        BetterCloudsStatic.getLogger().debug("[2/6] Reloading generator");
        res.reloadGenerator(useCubeClouds());
        BetterCloudsStatic.getLogger().debug("[3/6] Reloading textures");
        res.reloadTextures(client);
        BetterCloudsStatic.getLogger().debug("[4/6] Reloading primitive meshes");
        res.reloadMeshPrimitives();
        BetterCloudsStatic.getLogger().debug("[5/6] Reloading framebuffer");
        res.reloadFramebuffer(scaledFramebufferWidth(), scaledFramebufferHeight());
        BetterCloudsStatic.getLogger().debug("[6/6] Reloading timers");
        res.reloadTimer();
        BetterCloudsStatic.getLogger().info("Cloud renderer initialized");
    }

    public Resources resources() {
        return res;
    }

    // Used to be called isFancyMode
    private boolean useCubeClouds() {
        return ConfigManager.instance().sizeY > 0;
    }

    private int scaledFramebufferWidth() {
        return (int) (ConfigManager.instance().preset().upscaleResolutionFactor * client.getFramebuffer().textureWidth);
    }

    private int scaledFramebufferHeight() {
        return (int) (ConfigManager.instance().preset().upscaleResolutionFactor * client.getFramebuffer().textureHeight);
    }

    private ShaderParameters createShaderParameters(Config config) {
        return new ShaderParameters(
            client.options.getCloudRenderModeValue(),
            config.blockDistance(), config.sizeXZ, config.sizeY, config.celestialBodyHalo,
            glCompat.useDepthWriteFallback(), glCompat.useStencilTextureFallback(),
            DistantHorizonsCompat.instance().isReady() && DistantHorizonsCompat.instance().isEnabled(),
            config.preset().worldCurvatureSize
        );
    }

    public PrepareResult prepare(Matrix4f viewMat, Matrix4f projMat, int ticks, float tickDelta, Vector3d cam) {
        assert RenderSystem.isOnRenderThread();
        getProfiler().swap("render_setup");
        Config config = ConfigManager.instance();

        if (res.failedToLoadCritical()) {
            if (RenderDoc.isFrameCapturing()) glCompat.debugMessage("prepare failed: critical resource not loaded");
            return PrepareResult.FALLBACK;
        }

        // Rendering clouds when underwater was making them very visible in unloaded chunks
        if (client.gameRenderer.getCamera().getSubmersionType() != CameraSubmersionType.NONE) {
            return PrepareResult.NO_RENDER;
        }

        // This doesn't make the Skyweave block work, but it prevents larger issues
        if (ArsNouveauCompat.isSkyTextureCloudsRendering()) {
            return PrepareResult.NO_RENDER;
        }

        //? if >=1.21.11 {
        cloudsHeight = world.getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.CLOUD_HEIGHT_VISUAL, new Vec3d(cam.x, cam.y, cam.z));
        //?} elif >=1.21.6 {
        /*cloudsHeight = world.getDimension().cloudHeight().orElse(192);
        *///?} else {
        /*DimensionEffects effects = world.getDimensionEffects();
        cloudsHeight = effects.getCloudsHeight();
        *///?}

        res.generator().bind();
        ShaderParameters currentShaderParameters = createShaderParameters(config);
        if (!Objects.equals(currentShaderParameters, shaderParameters)) {
            shaderParameters = currentShaderParameters;
            res.reloadShaders(client.getResourceManager(), shaderParameters);
        }
        res.generator().reallocateIfStale(config, useCubeClouds());

        float cloudiness = CloudinessProvider.getCloudiness(client.world, tickDelta);
        res.generator().update(cam, ticks, tickDelta, ConfigManager.instance(), cloudiness);
        if (res.generator().canGenerate() && !res.generator().generating() && !Debug.generatorPause) {
            getProfiler().swap("generate_clouds");
            res.generator().generate();
            getProfiler().swap("render_setup");
        }

        if (res.generator().canSwap()) {
            getProfiler().swap("swap");
            res.generator().swap();
            getProfiler().swap("render_setup");
        }

        // This is fixes issue #14, not entirely sure why, but it forces the matrix to be homogenous
        tempMatrix.set(viewMat);
        tempMatrix.m30(0);
        tempMatrix.m31(0);
        tempMatrix.m32(0);
        // If this isn't 0 then https://github.com/Qendolin/better-clouds/issues/165 occurs, but idk why.
        // If it is 0 then the matrix is not invertible
        tempMatrix.m33(0);
        tempMatrix.m23(0);
        tempMatrix.m13(0);
        tempMatrix.m03(0);

        rotationProjectionMatrix.set(projMat);
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
        // In 1.21.3 render is called some time after prepare, so this may be false by now
        if (res.failedToLoadCritical()) return;

        getProfiler().swap("render_setup");
        if (Debug.isProfilingEnabled()) {
            if (res.timer() == null)
                res.reloadTimer();
            if (res.timer() != null)
                res.timer().start();
        }

        // Unbind vanilla shader, this is for compatability (with iris)
        //? if <1.21.6 {
        /*RenderHelper.unbindShader();
        *///?}
        RenderHelper.saveShader();
        RenderHelper.saveColorMask();
        RenderHelper.saveDepthMask();

        Config config = ConfigManager.instance();
        if (isFramebufferStale()) {
            res.reloadFramebuffer(scaledFramebufferWidth(), scaledFramebufferHeight());
        }

        FogProvider.Fog fog = FogProvider.instance.getFog(client, config, tickDelta);

        // Render clouds to our framebuffer
        getProfiler().swap("draw_coverage");
        GlStateManager._viewport(0, 0, res.fboWidth(), res.fboHeight());
        GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, res.oitFbo());
        glClearColor(0, 0, 0, 0);
        glClearDepth(1);
        drawCoverage(ticks + tickDelta, cam, frustumPos, frustum, fog);

        // Draw to game framebuffer
        VanillaRenderTarget rt = new VanillaRenderTarget(client, config);
        rt.begin();

        // Render debug stuff
        getProfiler().swap("draw_debug");
        Debug.render(res, cam);

        // Resolve and shade clouds
        getProfiler().swap("draw_shading");
        drawShading(tickDelta, fog);

        // Restore state
        getProfiler().swap("render_cleanup");
        rt.end();
        res.generator().unbind();
        GlStateManager._disableBlend();
        GlStateManager._enableDepthTest();
        RenderHelper.depthMask(true);
        RenderHelper.restoreDepthMask();
        GlStateManager._depthFunc(GL_LEQUAL);
        GlStateManager._activeTexture(GL_TEXTURE0);
        RenderHelper.colorMask(true, true, true, true);
        RenderHelper.restoreColorMask();
        RenderHelper.restoreShader();

        if (!glCompat.useStencilTextureFallback()) {
            glDisable(GL_STENCIL_TEST);
            glStencilFunc(GL_ALWAYS, 0x0, 0xff);
            glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
        }

        if (Debug.isProfilingEnabled() && res.timer() != null) {
            res.timer().stop();

            if (res.timer().frames() >= Debug.profileInterval) {
                PerfTimer.Stats gpu = PerfTimer.Stats.of(res.timer().gpu());
                PerfTimer.Stats cpu = PerfTimer.Stats.of(res.timer().cpu());
                BetterCloudsStatic.getLogger().info("GPU Times (msec):\n" + gpu);
                BetterCloudsStatic.getLogger().info("CPU Times (msec):\n" + cpu);
                ChatUtil.debugChatMessage("profiling.gpuTimes", gpu.formatted());
                ChatUtil.debugChatMessage("profiling.cpuTimes", cpu.formatted());
                res.timer().reset();
            }
        }
    }

    private boolean isFramebufferStale() {
        return res.fboWidth() != scaledFramebufferWidth() || res.fboHeight() != scaledFramebufferHeight();
    }

    private void drawCoverage(float ticks, Vector3d cam, Vector3d frustumPos, Frustum frustum, FogProvider.Fog fog) {
        GlStateManager._enableDepthTest();
        RenderHelper.colorMask(true, true, true, true);
        RenderHelper.depthMask(true);
        glEnable(GL_DEPTH_CLAMP);

        if (glCompat.useStencilTextureFallback()) {
            GlStateManager._depthFunc(GL_ALWAYS);
            GlStateManager._enableBlend();
            glBlendEquation(GL_FUNC_ADD);
            // FIXME: buf0 needs depth sorting
            glCompat.blendFunci(0, GL_ONE, GL_ZERO);
            glCompat.blendFunci(1, GL_ONE, GL_ONE);
            glDisable(GL_STENCIL_TEST);
        } else {
            GlStateManager._depthFunc(GL_LEQUAL);
            GlStateManager._disableBlend();
            glEnable(GL_STENCIL_TEST);
            glStencilMask(0xff);
            glClearStencil(0);
            glStencilOp(GL_KEEP, GL_INCR, GL_INCR);
            glStencilFunc(GL_ALWAYS, 0xff, 0xff);
        }

        if (useCubeClouds()) GlStateManager._enableCull();
        else GlStateManager._disableCull();
        glClear(GL_STENCIL_BUFFER_BIT | GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        Config generatorConfig = getGeneratorConfig();
        Config config = ConfigManager.instance();

        res.coverageShader().bind();
        res.coverageShader().uMVPMatrix.setMat4(mvpMatrix);
        res.coverageShader().uOriginOffset.setVec3((float) -res.generator().renderOriginX(cam.x), (float) cam.y - cloudsHeight, (float) -res.generator().renderOriginZ(cam.z));
        res.coverageShader().uBoundingBox.setVec4((float) cam.x, (float) cam.z, generatorConfig.blockDistance() - generatorConfig.chunkSize / 2f, generatorConfig.yRange + config.sizeY);
        res.coverageShader().uTime.setFloat(ticks / 20);
        res.coverageShader().uMiscellaneous.setVec3(config.scaleFalloffMin, config.windEffectFactor, config.windSpeedFactor);
        if (fog == null) { // Fog off
            res.coverageShader().uFogRange.setVec2(config.blockDistance() - 8, config.blockDistance());
        } else {
            res.coverageShader().uFogRange.setVec2(fog.start(), fog.end());
        }

        GlStateManager._activeTexture(GL_TEXTURE0);
        RenderHelper.bindTexture(client.getFramebuffer().getDepthAttachment());

        // Distant Horizons compat
        if (DistantHorizonsCompat.instance().isReady() && DistantHorizonsCompat.instance().isEnabled()) {
            res.coverageShader().uMVMatrix.setMat4(mvMatrix);
            res.coverageShader().uMcPMatrix.setMat4(pMatrix);

            Optional<Integer> depthId = DistantHorizonsCompat.instance().getDepthTextureId();
            GlStateManager._activeTexture(GL_TEXTURE6);
            if (depthId.isPresent()) {
                Matrix4f dhProjectionMatrix = DistantHorizonsCompat.instance().getProjectionMatrix();
                RenderHelper.bindTexture(depthId.get());
                if (DistantHorizonsCompat.instance().isTextureCreateFlagSet()) {
                    // This fixes a bug in DH, see: https://discord.com/channels/881614130614767666/1211290858134052894
                    glBindTexture(GL_TEXTURE_2D, depthId.get());
                    DistantHorizonsCompat.instance().resetTextureCreateFlag();
                }
                res.coverageShader().uDhPMatrix.setMat4(dhProjectionMatrix);
            } else {
                RenderHelper.bindTexture(0);
                res.coverageShader().uDhPMatrix.setMat4(DistantHorizonsCompat.NOOP_MATRIX);
            }
        }

        GlStateManager._activeTexture(GL_TEXTURE5);
        RenderHelper.bindTexture(client.getTextureManager().getTexture(Resources.NOISE_TEXTURE));

        res.generator().bind();
        if (glCompat.useBaseInstanceFallback()) {
            res.generator().buffer().bindDrawBuffer();
        }

        setFrustumTo(tempFrustum, frustum);
        Frustum frustumAtOrigin = tempFrustum;
        frustumAtOrigin.setPosition(frustumPos.x - res.generator().originX(), frustumPos.y, frustumPos.z - res.generator().originZ());

        if (!res.generator().canRender()) {
            GlStateManager._enableCull();
            return;
        }


        boolean frustumCulling = config.useFrustumCulling;
        if (IrisCompat.instance().isFrustumCullingDisabled() || config.preset().worldCurvatureSize != 0) {
            frustumCulling = false;
        }

        if (frustumCulling)
            drawCloudsWithFrustumCulling(frustumAtOrigin, config);
        else
            drawCloudsWithoutFrustumCulling();

        glDisable(GL_DEPTH_CLAMP);
        GlStateManager._enableCull();
    }

    private void drawCloudsWithFrustumCulling(Frustum frustumAtOrigin, Config config) {
        // This algorithm loops over chunks, which are in a line-by-line order.
        // When a visible chunk is found it's marked as a run start. The run continues until
        // the next non-visible chunk is found. At the end of a run the entire run is rendered as once.
        // This is possible due to the memory layout of the instance buffers.
        int runStart = -1;
        int runCount = 0;
        for (ChunkedGenerator.ChunkIndex chunk : res.generator().chunks()) {
            Box bounds = chunk.bounds(cloudsHeight, config.sizeXZ, config.sizeY);
            if (!frustumAtOrigin.isVisible(bounds)) {
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
    }

    private void drawCloudsWithoutFrustumCulling() {
        List<ChunkedGenerator.ChunkIndex> chunks = res.generator().chunks();
        if (chunks.isEmpty()) return;
        ChunkedGenerator.ChunkIndex first = chunks.get(0);
        ChunkedGenerator.ChunkIndex last = chunks.get(chunks.size() - 1);
        int start = first.start();
        int count = last.start() + last.count();
        if (glCompat.useBaseInstanceFallback()) {
            res.generator().buffer().setVAPointerToInstance(start);
        }
        glCompat.drawArraysInstancedBaseInstanceFallback(GL_TRIANGLE_STRIP, 0, res.generator().instanceVertexCount(), count, start);
    }

    private void drawShading(float tickDelta, FogProvider.Fog fog) {
        Config config = ConfigManager.instance();
        GlStateManager._depthFunc(GL_LEQUAL);

        if (!glCompat.useDepthWriteFallback()) {
            RenderHelper.depthMask(true);
            GlStateManager._enableDepthTest();
        } else {
            GlStateManager._disableDepthTest();
        }

        GlStateManager._enableBlend();
        glBlendEquation(GL_FUNC_ADD);
        // sync up state manager state, can be desynced by use of blendFunci
        GlStateManager._blendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

        RenderHelper.colorMask(false, false, false, false);
        glColorMaski(0, true, true, true, true);
        if (!glCompat.useStencilTextureFallback()) {
            glDisable(GL_STENCIL_TEST);
        }

        GlStateManager._activeTexture(GL_TEXTURE1);
        if (glCompat.useDepthWriteFallback()) {
            RenderHelper.bindTexture(0);
        } else {
            RenderHelper.bindTexture(res.oitCoverageDepthTexture());
        }
        GlStateManager._activeTexture(GL_TEXTURE2);
        RenderHelper.bindTexture(res.oitDataTexture());
        GlStateManager._activeTexture(GL_TEXTURE3);
        RenderHelper.bindTexture(res.oitCoverageTexture());
        GlStateManager._activeTexture(GL_TEXTURE4);
        RenderHelper.bindTexture(client.getTextureManager().getTexture(Resources.LIGHTING_TEXTURE));

        Vector3f effectTint = EffectTintProvider.getEffectTint(client, fog, tickDelta);
        //? if >=1.21.11 {
        long skyTime = world.getTimeOfDay() % 24000;
        //?} else {
        /*long skyTime = world.getLunarTime() % 24000;
        *///?}
        //? if >=1.21.11 {
        float skyAngleRad = client.gameRenderer.getCamera()
            .getEnvironmentAttributeInterpolator()
            .get(EnvironmentAttributes.SUN_ANGLE_VISUAL, tickDelta) * (float) (Math.PI / 180.0);
        //?} else {
        /*float skyAngleRad = world.getSkyAngleRadians(tickDelta);
        *///?}
        float sunPathAngleRad = (float) Math.toRadians(config.preset().sunPathAngle);
        float dayNightFactor = MathUtil.interpolateDayNightFactor(skyTime, config.preset().sunriseStartTime, config.preset().sunriseEndTime, config.preset().sunsetStartTime, config.preset().sunsetEndTime);
        float brightness = (1 - dayNightFactor) * config.preset().nightBrightness + dayNightFactor * config.preset().dayBrightness;
        float sunAxisY = MathHelper.sin(sunPathAngleRad);
        float sunAxisZ = MathHelper.cos(sunPathAngleRad);
        Vector3f sunDir = tempVector.set(1, 0, 0).rotateAxis(skyAngleRad + MathHelper.HALF_PI, 0, sunAxisY, sunAxisZ);
        float dayTime = world.getTimeOfDay() % 24000;
        float mappedTime = MathUtil.mapTimeOfDay(dayTime, config.preset().sunriseStartTime, config.preset().sunriseEndTime, config.preset().sunsetStartTime, config.preset().sunsetEndTime);

        res.shadingShader().bind();
        res.shadingShader().uVPMatrix.setMat4(rotationProjectionMatrix);
        res.shadingShader().uSunDirection.setVec4(sunDir.x, sunDir.y, sunDir.z, mappedTime / 24000f);
        res.shadingShader().uSunAxis.setVec3(0, sunAxisY, sunAxisZ);
        res.shadingShader().uOpacity.setVec3(config.preset().opacity, config.preset().opacityFactor, config.preset().opacityExponent);
        res.shadingShader().uColorGrading.setVec4(brightness, 1f / config.preset().gamma(), 0.0f, config.preset().saturation);
        res.shadingShader().uTint.setVec3(config.preset().tintRed * effectTint.x, config.preset().tintGreen * effectTint.y, config.preset().tintBlue * effectTint.z);
        res.shadingShader().uNoiseFactor.setFloat(config.colorVariationFactor);

        glBindVertexArray(res.cubeVao());
        glDrawArrays(GL_TRIANGLES, 0, Mesh.CUBE_MESH_VERTEX_COUNT);

        if (glCompat.useDepthWriteFallback()) {
            RenderHelper.colorMask(false, false, false, false);
            GlStateManager._activeTexture(GL_TEXTURE6);
            RenderHelper.bindTexture(res.oitCoverageDepthTexture());
            glTexParameteri(GL_TEXTURE_2D, glCompat.GL_DEPTH_STENCIL_TEXTURE_MODE, GL_DEPTH_COMPONENT);
            res.depthShader().bind();
            glDrawArrays(GL_TRIANGLES, 0, Mesh.QUAD_MESH_VERTEX_COUNT);
            glTexParameteri(GL_TEXTURE_2D, glCompat.GL_DEPTH_STENCIL_TEXTURE_MODE, GL_STENCIL_INDEX);
        }
    }

    private Config getGeneratorConfig() {
        Config config = res.generator().config();
        if (config != null) return config;
        return ConfigManager.instance();
    }

    private static void setFrustumTo(Frustum dst, Frustum src) {
        dst.frustumIntersection = src.frustumIntersection;
        dst.positionProjectionMatrix.set(src.positionProjectionMatrix);
        dst.x = src.x;
        dst.y = src.y;
        dst.z = src.z;
        dst.recession = src.recession;
    }

    public void close() {
        res.close();
    }

    public enum PrepareResult {
        RENDER, NO_RENDER, FALLBACK
    }
}
