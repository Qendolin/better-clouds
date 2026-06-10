package com.qendolin.betterclouds.rendering;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.qendolin.betterclouds.*;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.renderdoc.RenderDoc;
import com.qendolin.betterclouds.rendering.blaze3d.Blaze3DRenderer;
import com.qendolin.betterclouds.rendering.opengl.Debug;
import com.qendolin.betterclouds.rendering.opengl.OpenGLRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import static com.qendolin.betterclouds.compat.ProfilerWrapper.getProfiler;

public class CloudRenderCoordinator {
    public static final CloudRenderCoordinator instance = new CloudRenderCoordinator();

    private final Vector3d tempVector = new Vector3d();

    public CloudRenderer renderer;
    public Matrix4f capturedViewMat;
    public Matrix4f capturedProjMat;
    public Frustum frustum;

    public void initialize(Minecraft client) {
        if (GraphicsCompat.instance.isIncompatible()) return;
        renderer = GraphicsCompat.isOpenGL ? new OpenGLRenderer(client) : new Blaze3DRenderer(client);
    }

    public CloudRenderer getRenderer() {
        return renderer;
    }

    public void captureMatrices(Matrix4fc viewMatrix, Matrix4fc projectionMatrix) {
        capturedViewMat = new Matrix4f(viewMatrix);
        capturedProjMat = new Matrix4f(projectionMatrix);
    }

    public void captureFrustum(CameraRenderState cameraState) {
        frustum = new Frustum(cameraState.cullFrustum);
        Vec3 cameraPos = cameraState.pos;
        frustum.prepare(cameraPos.x, cameraPos.y, cameraPos.z);
    }

    public void setLevel(ClientLevel level) {
        renderer.setLevel(level);
    }

    public void reload(ResourceManager manager) {
        if (renderer != null) renderer.reload(manager);
    }

    public boolean renderClouds(FrameGraphBuilder frameGraphBuilder, LevelTargetBundle targets, Vec3 cameraPos, long gameTime, float ticksInput) {
        try {
            return renderCloudsInternal(frameGraphBuilder, targets, cameraPos, gameTime, ticksInput);
        } catch (Exception e) {
            BetterCloudsStatic.getLogger().error("Failed to render clouds", e);
            Commands.sendCrashChatMessage();
            if (renderer != null) renderer.close();
        }
        return false;
    }

    protected boolean renderCloudsInternal(FrameGraphBuilder frameGraphBuilder, LevelTargetBundle targets, Vec3 cameraPos, long gameTime, float ticksInput) {
        double camX = cameraPos.x, camY = cameraPos.y, camZ = cameraPos.z;
        float tickDelta = Mth.frac(ticksInput);
        if (!shouldRenderClouds()) return false;

        getProfiler().push(BetterCloudsStatic.MODID);
        GraphicsCompat.instance.pushDebugGroupDev("Better Clouds");

        Vector3d cam = tempVector.set(camX, camY, camZ);

        int ticks = (int) gameTime;
        if (Debug.animationPause >= 0) {
            if (Debug.animationPause == 0) Debug.animationPause = ticks;
            else ticks = Debug.animationPause;
            tickDelta = 0;
        }

        PrepareResult prepareResult = renderer.prepare(capturedViewMat, capturedProjMat, ticks, tickDelta, cam);
        if (RenderDoc.isFrameCapturing())
            GraphicsCompat.instance.debugMessage("renderer prepare returned " + prepareResult.name());

        if (prepareResult == PrepareResult.RENDER) {
            FramePass renderPass = frameGraphBuilder.addPass("clouds");
            if (targets.clouds != null) {
                targets.clouds = renderPass.readsAndWrites(targets.clouds);
            } else {
                targets.main = renderPass.readsAndWrites(targets.main);
            }

            final int fticks = ticks;
            final float ftickDelta = tickDelta;
            renderPass.executes(() -> {
                getProfiler().push("clouds");
                GraphicsCompat.instance.pushDebugGroupDev("Better Clouds");
                renderer.render(fticks, ftickDelta, cam, cam, frustum);
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
