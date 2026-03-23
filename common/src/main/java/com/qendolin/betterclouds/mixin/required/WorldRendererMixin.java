package com.qendolin.betterclouds.mixin.required;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.qendolin.betterclouds.BetterClouds;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.clouds.Debug;
import com.qendolin.betterclouds.clouds.Renderer;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.duck.WorldRendererDuck;
import com.qendolin.betterclouds.renderdoc.RenderDoc;
import com.qendolin.betterclouds.util.RenderHelper;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3d;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.qendolin.betterclouds.compat.GLCompat.glCompat;
import static com.qendolin.betterclouds.compat.ProfilerWrapper.getProfiler;

@Mixin(value = LevelRenderer.class, priority = 900)
public abstract class WorldRendererMixin implements WorldRendererDuck {

    @Unique
    private final Vector3d better_clouds$tempVector = new Vector3d();

    @Unique
    private Renderer better_clouds$cloudRenderer;
    @Unique
    private Frustum better_clouds$frustum;
    @Shadow
    private ClientLevel level;
    @Shadow
    private int ticks;
    @Shadow
    @Final
    private LevelTargetBundle targets;
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        if (glCompat.isIncompatible()) return;
        better_clouds$cloudRenderer = new Renderer(minecraft);
    }

    @Override
    public Renderer betterclouds$getRenderer() {
        return better_clouds$cloudRenderer;
    }

    @Inject(at = @At("TAIL"), method = "onResourceManagerReload(Lnet/minecraft/server/packs/resources/ResourceManager;)V")
    private void onReload(ResourceManager manager, CallbackInfo ci) {
        if (!BetterClouds.isInitialized()) return;
        if (glCompat.isIncompatible()) return;
        if (better_clouds$cloudRenderer != null)
            better_clouds$cloudRenderer.reload(manager);
    }

    @Inject(at = @At("TAIL"), method = "setLevel")
    private void onSetWorld(ClientLevel world, CallbackInfo ci) {
        if (better_clouds$cloudRenderer != null) better_clouds$cloudRenderer.setWorld(world);
    }

    @Inject(at = @At("HEAD"), method = "renderLevel")
    private void captureViewAndProjectionMatrix(GraphicsResourceAllocator allocator, DeltaTracker tickCounter, boolean renderBlockOutline, CameraRenderState cameraRenderState, Matrix4fc positionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, ChunkSectionsToRender chunkSectionsToRender, CallbackInfo ci) {
        RenderHelper.setProjectionMatrix(new Matrix4f(cameraRenderState.projectionMatrix));
        RenderHelper.setViewMatrix(new Matrix4f(positionMatrix));
        better_clouds$frustum = cameraRenderState.cullFrustum;
        Vec3 cameraPos = cameraRenderState.pos;
        better_clouds$frustum.prepare(cameraPos.x, cameraPos.y, cameraPos.z);
    }

    @Inject(at = @At("HEAD"), method = "addCloudsPass", cancellable = true, require = 0)
    private void renderClouds(FrameGraphBuilder frameGraphBuilder, CloudStatus _mode, Vec3 cameraPos, long _seed, float _ticks, int _color, float _cloudHeight, int _cloudRenderMode, CallbackInfo ci) {
        better_clouds$renderCloudsInternal(frameGraphBuilder, cameraPos, _ticks, ci);
    }

    @Unique
    private void better_clouds$renderCloudsInternal(FrameGraphBuilder frameGraphBuilder, Vec3 cameraPos, float ticksInput, CallbackInfo ci) {
        double camX = cameraPos.x, camY = cameraPos.y, camZ = cameraPos.z;
        float tickDelta = Mth.frac(ticksInput);
        Matrix4f viewMat = RenderHelper.getViewMatrix();
        Matrix4f projMat = RenderHelper.getProjectionMatrix();
        if (better_clouds$cloudRenderer == null) return;
        if (glCompat.isIncompatible()) return;
        if (level == null) return;
        if (!ConfigManager.instance().enabledDimensions.contains(level.dimensionTypeRegistration().unwrapKey().orElse(null)))
            return;
        if (!BetterClouds.isEnabled()) return;

        getProfiler().push(BetterCloudsStatic.MODID);
        glCompat.pushDebugGroupDev("Better Clouds");

        Vector3d cam = better_clouds$tempVector.set(camX, camY, camZ);
        Frustum frustum = this.better_clouds$frustum;

        int ticks = this.ticks;
        if (Debug.animationPause >= 0) {
            if (Debug.animationPause == 0) Debug.animationPause = ticks;
            else ticks = Debug.animationPause;
            tickDelta = 0;
        }

        Renderer.PrepareResult prepareResult = better_clouds$cloudRenderer.prepare(viewMat, projMat, ticks, tickDelta, cam);
        if (RenderDoc.isFrameCapturing())
            glCompat.debugMessage("renderer prepare returned " + prepareResult.name());

        if (prepareResult != Renderer.PrepareResult.FALLBACK)
            ci.cancel();

        // Note to self: do not use return
        if (prepareResult == Renderer.PrepareResult.RENDER) {
            var renderPass = frameGraphBuilder.addPass("clouds");
            if (targets.clouds != null) {
                targets.clouds = renderPass.readsAndWrites(targets.clouds);
            } else {
                targets.main = renderPass.readsAndWrites(targets.main);
            }

            final var fticks = ticks;
            final var ftickDelta = tickDelta;
            final var fcam = cam;
            final var ffrustumPos = cam;
            final var ffrustum = frustum;
            renderPass.executes(() -> {
                getProfiler().push("clouds");
                glCompat.pushDebugGroupDev("Better Clouds");
                better_clouds$cloudRenderer.render(fticks, ftickDelta, fcam, ffrustumPos, ffrustum);
                getProfiler().pop();
                glCompat.popDebugGroupDev();
            });
        }

        getProfiler().pop();
        glCompat.popDebugGroupDev();
    }


    @Inject(at = @At("HEAD"), method = "close")
    private void close(CallbackInfo ci) {
        if (better_clouds$cloudRenderer != null) better_clouds$cloudRenderer.close();
    }
}
