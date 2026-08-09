package com.qendolin.betterclouds.rendering.opengl;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.compat.*;
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.generator.ChunkedGenerator;
import com.qendolin.betterclouds.mixin.provider.EffectTintProvider;
import com.qendolin.betterclouds.mixin.provider.FogProvider;
import com.qendolin.betterclouds.renderdoc.RenderDoc;
import com.qendolin.betterclouds.rendering.*;
import com.qendolin.betterclouds.rendering.opengl.internal.Buffer;
import com.qendolin.betterclouds.rendering.opengl.internal.Mesh;
import com.qendolin.betterclouds.rendering.opengl.shaders.ShaderParameters;
import com.qendolin.betterclouds.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.joml.*;
import org.jspecify.annotations.NonNull;

import java.util.*;

import static com.qendolin.betterclouds.compat.ProfilerWrapper.getProfiler;
import static org.lwjgl.opengl.GL32.*;

public class OpenGLRenderer extends CloudRenderer {
    private final Matrix4f mvpMatrix = new Matrix4f();
    private final Matrix4f mvMatrix = new Matrix4f();
    private final Matrix4f pMatrix = new Matrix4f();
    private final Matrix4d pInverseMatrix = new Matrix4d();
    private final Matrix4f rotationProjectionMatrix = new Matrix4f();
    private final Matrix4f tempMatrix = new Matrix4f();
    private final Vector3f tempVector = new Vector3f();
    private final Frustum tempFrustum = new Frustum(new Matrix4f().identity(), new Matrix4f().identity());
    private final Resources res = new Resources();
    private final GLCompat glCompat = (GLCompat) GraphicsCompat.instance;
    private Buffer buffer;
    private ShaderParameters shaderParameters = null;

    private static void setFrustumTo(Frustum dst, Frustum src) {
        dst.set(src);
    }

    private static int calcBufferSize() {
        Config options = ConfigManager.instance();
        int distance = options.blockDistance();
        int size = Mth.floor(distance / options.spacing) + Mth.ceil(distance / options.spacing);
        return size > 0 ? size : 8 * 16;
    }

    public void reload(ResourceManager manager) {
        BetterCloudsStatic.getLogger().info("Reloading cloud renderer...");
        BetterCloudsStatic.getLogger().info("[1/6] Reloading shaders");
        shaderParameters = createShaderParameters(ConfigManager.instance());
        res.reloadShaders(manager, shaderParameters);
        BetterCloudsStatic.getLogger().info("[2/6] Reloading generator");
        reloadGenerator();
        reloadBuffer();
        BetterCloudsStatic.getLogger().info("[3/6] Reloading textures");
        res.reloadTextures(client);
        BetterCloudsStatic.getLogger().info("[4/6] Reloading primitive meshes");
        res.reloadMeshPrimitives();
        BetterCloudsStatic.getLogger().info("[5/6] Reloading framebuffer");
        res.reloadFramebuffer(scaledFramebufferWidth(), scaledFramebufferHeight());
        BetterCloudsStatic.getLogger().info("[6/6] Reloading timers");
        reloadTimer();
        BetterCloudsStatic.getLogger().info("Cloud renderer initialized");
    }

    private void reloadBuffer() {
        if (buffer != null && !buffer.hasChanged(calcBufferSize(), useCubeClouds(), ConfigManager.instance().usePersistentBuffers))
            return;
        BetterCloudsStatic.getLogger().debug("Reloading buffer");
        if (buffer != null) buffer.close();
        buffer = new Buffer(calcBufferSize(), useCubeClouds(), ConfigManager.instance().usePersistentBuffers);
        buffer.unbind();
    }

    public Resources resources() {
        return res;
    }

    private int scaledFramebufferWidth() {
        return (int) (ConfigManager.instance().shaderPreset().upscaleResolutionFactor * client.gameRenderer.mainRenderTarget().width);
    }

    private int scaledFramebufferHeight() {
        return (int) (ConfigManager.instance().shaderPreset().upscaleResolutionFactor * client.gameRenderer.mainRenderTarget().height);
    }

    public void uploadPointsToBuffer() {
        reloadBuffer();
        buffer.clear();

        for (ChunkedGenerator.Point point : generator.points()) {
            buffer.put(point.x(), point.y(), point.z());
        }
        buffer.swap();
    }

    private ShaderParameters createShaderParameters(Config config) {
        return new ShaderParameters(
                client.options.getCloudStatus(),
                config.blockDistance(), config.sizeXZ, config.sizeY, config.celestialBodyHalo,
                glCompat.useDepthWriteFallback(), glCompat.useStencilTextureFallback(),
                DistantHorizonsCompat.instance().isReady() && DistantHorizonsCompat.instance().isEnabled(),
                IrisCompat.instance().isShadersEnabled(),
                config.shaderPreset().worldCurvatureSize
        );
    }

    public @NonNull PrepareResult prepare(Matrix4f viewMat, Matrix4f projMat, long cloudTicks, long clientTicks, float tickDelta, Vector3d cam) {
        assert RenderSystem.isOnRenderThread();
        getProfiler().popPush("render_setup");
        Config config = ConfigManager.instance();

        if (res.failedToLoadCritical()) {
            if (RenderDoc.isFrameCapturing()) glCompat.debugMessage("prepare failed: critical resource not loaded");
            return PrepareResult.FALLBACK;
        }

        buffer.bind();
        ShaderParameters currentShaderParameters = createShaderParameters(config);
        if (!Objects.equals(currentShaderParameters, shaderParameters)) {
            shaderParameters = currentShaderParameters;
            res.reloadShaders(client.getResourceManager(), shaderParameters);
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

        tempMatrix.translate((float) generator.renderOriginX(cam.x), (float) (cloudHeight - cam.y), (float) generator.renderOriginZ(cam.z));
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
    @Override
    public void render(long ticks, float tickDelta, Vector3d cam, Vector3d frustumPos, Frustum frustum) {
        // In 1.21.3 render is called some time after prepare, so this may be false by now
        if (res.failedToLoadCritical()) return;

        getProfiler().popPush("render_setup");

        // Unbind vanilla shader, this is for compatability (with iris)
        RenderHelper.saveShader();
        RenderHelper.saveColorMask();
        RenderHelper.saveDepthMask();

        Config config = ConfigManager.instance();
        if (isFramebufferStale()) {
            res.reloadFramebuffer(scaledFramebufferWidth(), scaledFramebufferHeight());
        }

        FogProvider.Fog fog = FogProvider.instance.getFog(client, config, tickDelta);

        // Render clouds to our framebuffer
        getProfiler().popPush("draw_coverage");
        GlStateManager._viewport(0, 0, res.fboWidth(), res.fboHeight());
        GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, res.oitFbo());
        glClearColor(0, 0, 0, 0);
        clearDepth();
        setDepthFuncGEqual();
        drawCoverage(ticks + tickDelta, cam, frustumPos, frustum, fog);

        // Draw to game framebuffer
        VanillaRenderTarget rt = new VanillaRenderTarget(client, config);
        rt.begin();

        // Render debug stuff
        getProfiler().popPush("draw_debug");
        Debug.render(res, cam);

        // Resolve and shade clouds
        getProfiler().popPush("draw_shading");
        drawShading(tickDelta, fog, cam);

        // Restore state
        getProfiler().popPush("render_cleanup");
        rt.end();
        if (buffer != null) buffer.unbind();
        GlStateManager._disableBlend(0);
        GlStateManager._enableDepthTest();
        RenderHelper.depthMask(true);
        RenderHelper.restoreDepthMask();
        setDepthFuncGEqual();
        GlStateManager._activeTexture(GL_TEXTURE0);
        RenderHelper.colorMask(true, true, true, true);
        RenderHelper.restoreColorMask();
        RenderHelper.restoreShader();

        if (!glCompat.useStencilTextureFallback()) {
            glDisable(GL_STENCIL_TEST);
            glStencilFunc(GL_ALWAYS, 0x0, 0xff);
            glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
        }

        stopTiming();
    }

    private boolean isFramebufferStale() {
        return res.fboWidth() != scaledFramebufferWidth() || res.fboHeight() != scaledFramebufferHeight();
    }

    private void clearDepth() {
        // iris isn't using reverse z
        glClearDepth(IrisCompat.instance().isShadersEnabled() ? 1 : 0);
    }

    private void setDepthFuncGEqual() {
        // iris isn't using reverse z
        GlStateManager._depthFunc(IrisCompat.instance().isShadersEnabled() ? GL_LEQUAL : GL_GEQUAL);
    }

    private void drawCoverage(float ticks, Vector3d cam, Vector3d frustumPos, Frustum frustum, FogProvider.Fog fog) {
        GlStateManager._enableDepthTest();
        RenderHelper.colorMask(true, true, true, true);
        RenderHelper.depthMask(true);
        glEnable(GL_DEPTH_CLAMP);

        if (glCompat.useStencilTextureFallback()) {
            GlStateManager._depthFunc(GL_ALWAYS);
            GlStateManager._enableBlend(0);
            glBlendEquation(GL_FUNC_ADD);
            // FIXME: buf0 needs depth sorting
            glCompat.blendFunci(0, GL_ONE, GL_ZERO);
            glCompat.blendFunci(1, GL_ONE, GL_ONE);
            glDisable(GL_STENCIL_TEST);
        } else {
            setDepthFuncGEqual();
            GlStateManager._disableBlend(0);
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
        res.coverageShader().uOriginOffset.setVec3((float) -generator.renderOriginX(cam.x), (float) cam.y - cloudHeight, (float) -generator.renderOriginZ(cam.z));
        res.coverageShader().uBoundingBox.setVec4((float) cam.x, (float) cam.z, generatorConfig.blockDistance() - generatorConfig.chunkSize / 2f, generatorConfig.yRange + config.sizeY);
        res.coverageShader().uTime.setFloat(ticks / 20);
        res.coverageShader().uMiscellaneous.setVec3(config.scaleFalloffMin, config.windEffectFactor, config.windSpeedFactor);
        if (fog == null) { // Fog off
            res.coverageShader().uFogRange.setVec2(config.blockDistance() - 8, config.blockDistance());
        } else {
            res.coverageShader().uFogRange.setVec2(fog.start(), fog.end());
        }

        GlStateManager._activeTexture(GL_TEXTURE0);
        RenderHelper.bindTexture(client.gameRenderer.mainRenderTarget().getDepthTexture());

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

        buffer.bind();
        if (glCompat.useBaseInstanceFallback()) {
            buffer.bindDrawBuffer();
        }

        setFrustumTo(tempFrustum, frustum);
        Frustum frustumAtOrigin = tempFrustum;
        frustumAtOrigin.prepare(frustumPos.x - generator.originX(), frustumPos.y, frustumPos.z - generator.originZ());

        if (!generator.canRender()) {
            GlStateManager._enableCull();
            return;
        }

        boolean frustumCulling = config.useFrustumCulling;
        if (IrisCompat.instance().isFrustumCullingDisabled() || config.shaderPreset().worldCurvatureSize != 0) {
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
        for (ChunkedGenerator.ChunkIndex chunk : generator.chunks()) {
            AABB bounds = chunk.bounds(cloudHeight, config.sizeXZ, config.sizeY);
            if (!frustumAtOrigin.isVisible(bounds)) {
                Debug.addFrustumCulledBox(bounds, false);
                if (runCount != 0) {
                    if (glCompat.useBaseInstanceFallback()) {
                        buffer.setVAPointerToInstance(runStart);
                    }
                    glCompat.drawArraysInstancedBaseInstanceFallback(GL_TRIANGLE_STRIP, 0, buffer.instanceVertexCount(), runCount, runStart);
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
                buffer.setVAPointerToInstance(runStart);
            }
            glCompat.drawArraysInstancedBaseInstanceFallback(GL_TRIANGLE_STRIP, 0, buffer.instanceVertexCount(), runCount, runStart);
        }
    }

    private void drawCloudsWithoutFrustumCulling() {

        List<ChunkedGenerator.ChunkIndex> chunks = generator.chunks();
        if (chunks.isEmpty()) return;
        ChunkedGenerator.ChunkIndex first = chunks.getFirst();
        ChunkedGenerator.ChunkIndex last = chunks.getLast();
        int start = first.start();
        int count = last.start() + last.count();
        if (glCompat.useBaseInstanceFallback()) {
            buffer.setVAPointerToInstance(start);
        }
        glCompat.drawArraysInstancedBaseInstanceFallback(GL_TRIANGLE_STRIP, 0, buffer.instanceVertexCount(), count, start);
    }

    private void drawShading(float tickDelta, FogProvider.Fog fog, Vector3d cam) {

        Config config = ConfigManager.instance();
        setDepthFuncGEqual();

        if (!glCompat.useDepthWriteFallback()) {
            RenderHelper.depthMask(true);
            GlStateManager._enableDepthTest();
        } else {
            GlStateManager._disableDepthTest();
        }

        GlStateManager._enableBlend(0);
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

        Vector3f effectTint = EffectTintProvider.getEffectTint(client, fog, tickDelta, cam);
        float skyAngleRad = EffectTintProvider.getSunAngleRadians(level, cam);
        float sunPathAngleRad = config.shaderPreset().sunPathAngle * Mth.DEG_TO_RAD;
        float dayNightFactor = MathUtil.interpolateDayNightFactor(dayTime(), config.shaderPreset().sunriseStartTime, config.shaderPreset().sunriseEndTime, config.shaderPreset().sunsetStartTime, config.shaderPreset().sunsetEndTime);
        float brightness = (1 - dayNightFactor) * config.shaderPreset().nightBrightness + dayNightFactor * config.shaderPreset().dayBrightness;

        float sunAxisY = Mth.sin(sunPathAngleRad);
        float sunAxisZ = Mth.cos(sunPathAngleRad);
        Vector3f sunDir = tempVector.set(1, 0, 0).rotateAxis(skyAngleRad + Mth.HALF_PI, 0, sunAxisY, sunAxisZ);
        float mappedTime = MathUtil.mapTimeOfDay(dayTime(), config.shaderPreset().sunriseStartTime, config.shaderPreset().sunriseEndTime, config.shaderPreset().sunsetStartTime, config.shaderPreset().sunsetEndTime);

        res.shadingShader().bind();
        res.shadingShader().uVPMatrix.setMat4(rotationProjectionMatrix);
        res.shadingShader().uSunDirection.setVec4(sunDir.x, sunDir.y, sunDir.z, mappedTime / 24000f);
        res.shadingShader().uSunAxis.setVec3(0, sunAxisY, sunAxisZ);
        res.shadingShader().uOpacity.setVec3(config.shaderPreset().opacity, config.shaderPreset().opacityFactor, config.shaderPreset().opacityExponent);
        res.shadingShader().uColorGrading.setVec4(brightness, 1f / config.shaderPreset().gamma(), 0.0f, config.shaderPreset().saturation);
        res.shadingShader().uTint.setVec3(config.shaderPreset().topColorRed * effectTint.x, config.shaderPreset().topColorGreen * effectTint.y, config.shaderPreset().topColorBlue * effectTint.z);
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
        Config config = generator.config();
        if (config != null) return config;
        return ConfigManager.instance();
    }

    public void onClose() {
        if (buffer != null) {
            buffer.close();
            buffer = null;
        }
        res.close();
    }
}
