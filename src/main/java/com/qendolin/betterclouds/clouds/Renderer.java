package com.qendolin.betterclouds.clouds;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.qendolin.betterclouds.Config;
import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.clouds.shaders.Shader;
import com.qendolin.betterclouds.compat.IrisCompat;
import com.qendolin.betterclouds.compat.SodiumExtraCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Util;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.List;

import static com.qendolin.betterclouds.Main.glCompat;
import static org.lwjgl.opengl.GL32.*;

public class Renderer implements AutoCloseable {
    private final MinecraftClient client;
    private ClientWorld world = null;

    private final long startTime = Util.getEpochTimeMs();
    private float cloudsHeight;
    private int defaultFbo;
    private final Matrix4f mvpMatrix = new Matrix4f();
    private final Matrix4f rotationProjectionMatrix = new Matrix4f();
    private final Matrix4f tempMatrix = new Matrix4f();
    private final PrimitiveChangeDetector shaderInvalidator = new PrimitiveChangeDetector(false);

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
        res.reloadShaders(manager);
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

    private Config getGeneratorConfig() {
        Config config = res.generator().config();
        if(config != null) return config;
        return Main.getConfig();
    }

    public boolean prepare(MatrixStack matrices, Matrix4f projMat, float tickDelta, Vector3d cam) {
        assert RenderSystem.isOnRenderThread();
        client.getProfiler().swap("render_setup");
        Config config = Main.getConfig();

        if(res.failedToLoadCritical()) return false;
        if(!config.irisSupport && IrisCompat.IS_LOADED && IrisCompat.isShadersEnabled()) return false;

        DimensionEffects effects = world.getDimensionEffects();
        if(SodiumExtraCompat.IS_LOADED && effects.getSkyType() == DimensionEffects.SkyType.NORMAL) {
            cloudsHeight = SodiumExtraCompat.getCloudsHeight() + config.yOffset;
        } else {
            cloudsHeight = effects.getCloudsHeight() + config.yOffset;
        }

        res.generator().bind();
        if(shaderInvalidator.hasChanged(client.options.getCloudRenderModeValue(), config.blockDistance(),
            config.fadeEdge, config.sizeXZ, config.sizeY, config.writeDepth)) {
            res.reloadShaders(client.getResourceManager());
        }
        res.generator().reallocateIfStale(config, isFancyMode());

        float raininess = Math.max(0.6f * world.getRainGradient(tickDelta), world.getThunderGradient(tickDelta));
        float cloudiness = raininess * 0.3f + 0.5f;

        res.generator().update(cam, tickDelta, Main.getConfig(), cloudiness);
        if(res.generator().canGenerate() && !res.generator().generating() && !Debug.generatorPause) {
            client.getProfiler().swap("generate_clouds");
            res.generator().generate();
            client.getProfiler().swap("render_setup");
        }

        if(res.generator().canSwap()) {
            client.getProfiler().swap("swap");
            res.generator().swap();
            client.getProfiler().swap("render_setup");
        }

        tempMatrix.set(matrices.peek().getPositionMatrix());

        matrices.translate(res.generator().renderOriginX(cam.x), cloudsHeight-cam.y, res.generator().renderOriginZ(cam.z));

        // FIXME: moon corona in wrong spot when upscaling is used
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

        mvpMatrix.set(projMat);
        mvpMatrix.mul(matrices.peek().getPositionMatrix());

        // TODO: don't do this dynamically
        defaultFbo = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING);

        return true;
    }

    // Don't forget to push / pop matrix stack outside
    public void render(float tickDelta, Vector3d cam, Vector3d frustumPos, Frustum frustum) {
        // Rendering clouds when underwater was making them very visible in unloaded chunks
        if(client.gameRenderer.getCamera().getSubmersionType() != CameraSubmersionType.NONE) return;

        client.getProfiler().swap("render_setup");
        if(Main.isProfilingEnabled()) {
            if(res.timer() == null) res.reloadTimer();
            res.timer().start();
        }

        Config config = Main.getConfig();

        if(isFramebufferStale()) {
            res.reloadFramebuffer(scaledFramebufferWidth(), scaledFramebufferHeight());
        }

        RenderSystem.viewport(0, 0, res.fboWidth(), res.fboHeight());
        GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, res.oitFbo());
        RenderSystem.clearDepth(1);
        RenderSystem.clearColor(0, 0, 0, 0);

        client.getProfiler().swap("draw_depth");
        drawDepth();

        client.getProfiler().swap("draw_coverage");
        drawCoverage(cam, frustumPos, frustum);

        client.getProfiler().swap("draw_shading");
        if(IrisCompat.IS_LOADED && IrisCompat.isShadersEnabled() && config.useIrisFBO) {
            IrisCompat.bindFramebuffer();
        } else {
            GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, defaultFbo);
        }
        // TODO: viewport might not be correct always
        RenderSystem.viewport(0, 0, client.getFramebuffer().textureWidth, client.getFramebuffer().textureHeight);

        drawShading(tickDelta);

        client.getProfiler().swap("render_cleanup");

        res.generator().unbind();
        Shader.unbind();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(GL_LEQUAL);
        RenderSystem.activeTexture(GL_TEXTURE0);
        RenderSystem.colorMask(true, true, true, true);
        glDisable(GL_STENCIL_TEST);
        glStencilFunc(GL_ALWAYS, 0x0, 0xff);

        if(Debug.frustumCulling) {
            glCompat.pushDebugGroup("Frustum Culling Debug Draw");
            Debug.drawFrustumCulling(cam, frustum, frustumPos, res.generator(), cloudsHeight);
            glCompat.popDebugGroup();
        }

        if(Main.isProfilingEnabled() && res.timer() != null) {
            res.timer().stop();

            if(res.timer().frames() >= Debug.profileInterval) {
                List<Double> times = res.timer().get();
                times.sort(Double::compare);
                double median = times.get(times.size()/2);
                double p25 = times.get((int) Math.ceil(times.size()*0.25));
                double p75 = times.get((int) Math.ceil(times.size()*0.75));
                double min = times.get(0);
                double max = times.get(times.size()-1);
                double average = times.stream().mapToDouble(d -> d).average().orElse(0);
                Main.debugChatMessage("profiling.gpuTimes", min, average, max, p25, median, p75);
                res.timer().reset();
            }
        }
    }

    private void drawShading(float tickDelta) {
        Config config = Main.getConfig();

        if(config.writeDepth) {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL_ALWAYS);
        } else {
            RenderSystem.disableDepthTest();
        }

        RenderSystem.enableBlend();
        RenderSystem.blendEquation(GL_FUNC_ADD);
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
        RenderSystem.colorMask(false, false, false, false);
        glColorMaski(0, true, true, true, true);
        glStencilFunc(GL_GREATER, 0x0, 0xff);
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);

        RenderSystem.activeTexture(GL_TEXTURE1);
        RenderSystem.bindTexture(res.oitCoverageDepthView());
        RenderSystem.activeTexture(GL_TEXTURE2);
        RenderSystem.bindTexture(res.oitDataTexture());
        RenderSystem.activeTexture(GL_TEXTURE3);
        RenderSystem.bindTexture(res.oitCoverageTexture());
        RenderSystem.activeTexture(GL_TEXTURE4);
        client.getTextureManager().getTexture(Resources.LIGHTING_TEXTURE).bindTexture();

        float effectLuma = getEffectLuminance(tickDelta);
        float skyAngle = world.getSkyAngle(tickDelta);
        float skyAngleRad = world.getSkyAngleRadians(tickDelta);
        float sunPathAngleRad = (float) Math.toRadians(config.preset().sunPathAngle);
        float dayNightFactor = smoothstep(skyAngle, 0.17333f, 0.25965086f) * smoothstep(skyAngle, 0.82667f, 0.7403491f);
        float brightness = dayNightFactor * config.preset().nightBrightness + (1-dayNightFactor) * config.preset().dayBrightness;
        // FIXME: sunDir seems misaligned at sunrise / sunset
        Vector3f sunDir = new Vector3f(0, 1, 0).rotateAxis(skyAngleRad, 0, MathHelper.sin(sunPathAngleRad/2), MathHelper.cos(sunPathAngleRad/2));

        res.shadingShader().bind();
        res.shadingShader().uVPMatrix.setMat4(rotationProjectionMatrix);
        res.shadingShader().uSunDirection.setVec4(sunDir.x, sunDir.y, sunDir.z,(world.getTimeOfDay()%24000)/24000f);
        res.shadingShader().uOpacity.setVec2(config.preset().opacity, config.preset().opacityFactor);
        res.shadingShader().uColorGrading.setVec4(brightness, 1f/config.preset().gamma(), effectLuma, config.preset().saturation);
        res.shadingShader().uTint.setVec3(config.preset().tintRed, config.preset().tintGreen, config.preset().tintBlue);
        res.shadingShader().uNoiseFactor.setFloat(config.colorVariationFactor);


        glBindVertexArray(res.cubeVao());
        glDrawArrays(GL_TRIANGLES, 0, Mesh.CUBE_MESH_VERTEX_COUNT);
    }

    private void drawCoverage(Vector3d cam, Vector3d frustumPos, Frustum frustum) {
        glEnable(GL_STENCIL_TEST);
        glStencilMask(0xff);
        glStencilOp(GL_KEEP, GL_INCR, GL_INCR);
        glStencilFunc(GL_ALWAYS, 0xff, 0xff);
        RenderSystem.depthFunc(GL_LESS);
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);
        glClear(GL_STENCIL_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

        Config generatorConfig = getGeneratorConfig();
        Config config = Main.getConfig();

        res.coverageShader().bind();
        res.coverageShader().uMVPMatrix.setMat4(mvpMatrix);
        res.coverageShader().uOriginOffset.setVec3((float) -res.generator().renderOriginX(cam.x), (float) cam.y-cloudsHeight, (float) -res.generator().renderOriginZ(cam.z));
        res.coverageShader().uBoundingBox.setVec4((float) cam.x, (float) cam.z, generatorConfig.blockDistance() - generatorConfig.chunkSize/2f, generatorConfig.yRange + config.sizeY);
        res.coverageShader().uTime.setFloat((Util.getEpochTimeMs() - startTime)/1000f);
        res.coverageShader().uMiscellaneous.setVec2(config.scaleFalloffMin, config.windFactor);

        RenderSystem.activeTexture(GL_TEXTURE5);
        client.getTextureManager().getTexture(Resources.NOISE_TEXTURE).bindTexture();

        res.generator().bind();

        Frustum frustumAtOrigin = new Frustum(frustum);
        frustumAtOrigin.setPosition(frustumPos.x - res.generator().originX(), frustumPos.y, frustumPos.z - res.generator().originZ());
        if(res.generator().canRender()) {
            int runStart = -1;
            int runCount = 0;
            for (ChunkedGenerator.ChunkIndex chunk : res.generator().chunks()) {
                Box bounds = chunk.bounds(cloudsHeight, config.sizeXZ, config.sizeY);
                if(!frustumAtOrigin.isVisible(bounds)) {
                    if(runCount != 0) {
                        glCompat.drawArraysInstancedBaseInstance(GL_TRIANGLE_STRIP, 0, res.generator().instanceVertexCount(), runCount, runStart);
                    }
                    runStart = -1;
                    runCount = 0;
                } else {
                    if(runStart == -1) runStart = chunk.start();
                    runCount += chunk.count();
                }
            }
            if(runCount != 0) {
                glCompat.drawArraysInstancedBaseInstance(GL_TRIANGLE_STRIP, 0, res.generator().instanceVertexCount(), runCount, runStart);
            }
        }
    }

    private void drawDepth() {
        RenderSystem.disableBlend();
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL_ALWAYS);
        RenderSystem.depthMask(true);

        res.depthShader().bind();

        RenderSystem.activeTexture(GL_TEXTURE0);
        RenderSystem.bindTexture(client.getFramebuffer().getDepthAttachment());

        glBindVertexArray(res.cubeVao());
        glDrawArrays(GL_TRIANGLES, 0, Mesh.QUAD_MESH_VERTEX_COUNT);
    }

    private boolean isFancyMode() {
        return client.options.getCloudRenderModeValue() == CloudRenderMode.FANCY;
    }

    private float smoothstep(float x, float e0, float e1) {
        x = MathHelper.clamp((x-e0) / (e1-e0), 0, 1);
        return x * x * (3 - 2*x);
    }

    private float getEffectLuminance(float tickDelta) {
        float luma = 1.0f;
        float rain = world.getRainGradient(tickDelta);
        if (rain > 0.0f) {
            float f = rain * 0.95f;
            luma *= (1.0 - f) + f * 0.6f;
        }
        float thunder = world.getThunderGradient(tickDelta);
        if (thunder > 0.0f) {
            float f = thunder * 0.95f;
            luma *= (1.0 - f) + f * 0.2f;
        }
        return luma;
    }

    private boolean isFramebufferStale() {
        return res.fboWidth() != scaledFramebufferWidth() || res.fboHeight() != scaledFramebufferHeight();
    }

    private int scaledFramebufferWidth() {
        return (int) (Main.getConfig().preset().upscaleResolutionFactor * client.getFramebuffer().textureWidth);
    }

    private int scaledFramebufferHeight() {
        return (int) (Main.getConfig().preset().upscaleResolutionFactor * client.getFramebuffer().textureHeight);
    }

    public void close() {
        res.close();
    }
}
