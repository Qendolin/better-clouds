package com.qendolin.betterclouds.rendering;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.qendolin.betterclouds.*;
import com.qendolin.betterclouds.compat.IrisCompat;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.renderdoc.RenderDoc;
import com.qendolin.betterclouds.rendering.blaze3d.Blaze3DRenderer;
import com.qendolin.betterclouds.rendering.opengl.Debug;
import com.qendolin.betterclouds.rendering.opengl.OpenGLRenderer;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import static com.qendolin.betterclouds.compat.ProfilerWrapper.getProfiler;

public class CloudRenderCoordinator {
    public static final CloudRenderCoordinator instance = new CloudRenderCoordinator();

    public CloudRenderer renderer;
    public Matrix4f capturedViewMat;
    public Matrix4f capturedProjMat;
    public Frustum frustum;
    public long clientTicks;

    private boolean sentCloudsDisabledMessage = false;
    private final Vector3d tempVector = new Vector3d();

    public void initialize() {
        if (GraphicsCompat.instance.isIncompatible()) return;
        renderer = GraphicsCompat.isOpenGL ? new OpenGLRenderer() : new Blaze3DRenderer();
    }

    public CloudRenderer getRenderer() {
        return renderer;
    }

    public void captureMatrices(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        capturedViewMat = viewMatrix;
        capturedProjMat = projectionMatrix;
    }

    public void captureFrustum(CameraRenderState cameraState) {
        frustum = new Frustum(cameraState.cullFrustum);
        Vec3 cameraPos = cameraState.pos;
        frustum.prepare(cameraPos.x, cameraPos.y, cameraPos.z);
    }

    public void checkState(OptionsRenderState optionsRenderState) {
        if (sentCloudsDisabledMessage || !optionsRenderState.cloudStatus.equals(CloudStatus.OFF) || IrisCompat.instance().isShadersEnabled() || !ConfigManager.instance().cloudsDisabledMessageEnabled) return;
        Commands.sendCloudsDisabledMessage();
        sentCloudsDisabledMessage = true;
    }

    public void reload(ResourceManager manager) {
        if (renderer == null)
            renderer = GraphicsCompat.isOpenGL ? new OpenGLRenderer() : new Blaze3DRenderer();
        renderer.reload(manager);
    }

    public boolean renderClouds(FrameGraphBuilder frameGraphBuilder, LevelTargetBundle targets, Vec3 cameraPos, float ticksInput) {
        if (renderer == null) return false;
        try {
            return renderCloudsInternal(frameGraphBuilder, targets, cameraPos, ticksInput);
        } catch (Exception e) {
            BetterCloudsStatic.getLogger().error("Failed to render clouds", e);
            Commands.sendCrashChatMessage();
            if (renderer != null) renderer.close();
            renderer = null;
        }
        return false;
    }

    protected boolean renderCloudsInternal(FrameGraphBuilder frameGraphBuilder, LevelTargetBundle targets, Vec3 cameraPos, float ticksInput) {
        double camX = cameraPos.x, camY = cameraPos.y, camZ = cameraPos.z;
        float tickDelta = Mth.frac(ticksInput);
        if (!shouldRenderClouds()) return false;

        getProfiler().push(BetterCloudsStatic.MODID);
        GraphicsCompat.instance.pushDebugGroupDev("Better Clouds");

        Vector3d cam = tempVector.set(camX, camY, camZ);

        if (Debug.animationPause >= 0) {
            if (Debug.animationPause == 0) Debug.animationPause = clientTicks;
            else clientTicks = Debug.animationPause;
            tickDelta = 0;
        }

        long trueCloudTicks = Math.floorMod(
                ConfigManager.instance().getCloudTicks(Minecraft.getInstance(), clientTicks),
                Integer.MAX_VALUE       // arbitrary value that is well before the generation precision loss
        );
        long clampedCloudTicks = Math.floorMod(trueCloudTicks, CloudRenderer.CLOUD_TIME_PERIOD_TICKS);

        // generator gets true cloud time
        renderer.updateGenerator(cam, trueCloudTicks, clientTicks, tickDelta);
        // renderer gets clamped cloud time
        PrepareResult prepareResult = renderer.checkAndPrepare(capturedViewMat, capturedProjMat, clampedCloudTicks, clientTicks, tickDelta, cam);

        if (RenderDoc.isFrameCapturing())
            GraphicsCompat.instance.debugMessage("renderer prepare returned " + prepareResult.name());

        if (prepareResult == PrepareResult.RENDER) {
            FramePass renderPass = frameGraphBuilder.addPass("clouds");
            if (targets.clouds != null) {
                targets.clouds = renderPass.readsAndWrites(targets.clouds);
            } else {
                targets.main = renderPass.readsAndWrites(targets.main);
            }

            final long fticks = clampedCloudTicks;
            final float ftickDelta = tickDelta;
            renderPass.executes(() -> {
                getProfiler().push("clouds");
                GraphicsCompat.instance.pushDebugGroupDev("Better Clouds");
                try {
                    renderer.checkAndRender(fticks, ftickDelta, cam, cam, frustum);
                } catch (Exception e) {
                    BetterCloudsStatic.getLogger().error("Failed to render clouds", e);
                    renderer.close();
                    renderer = null;
                    Commands.sendCrashChatMessage();
                }
                getProfiler().pop();
                GraphicsCompat.instance.popDebugGroupDev();
            });
        }

        getProfiler().pop();
        GraphicsCompat.instance.popDebugGroupDev();
        return prepareResult != PrepareResult.FALLBACK;
    }

    public void close() {
        if (renderer != null) renderer.close();
    }

    private boolean shouldRenderClouds() {
        if (renderer == null) return false;
        if (GraphicsCompat.instance.isIncompatible()) return false;

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return false;
        if (!ConfigManager.instance().enabledDimensions.contains(level.dimensionTypeRegistration().unwrapKey().orElse(null)))
            return false;

        return BetterClouds.isEnabled();
    }
}
