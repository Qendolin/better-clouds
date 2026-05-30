package com.qendolin.betterclouds.mixin.required;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.qendolin.betterclouds.BetterClouds;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.mixin.duck.WorldRendererDuck;
import com.qendolin.betterclouds.renderdoc.RenderDoc;
import com.qendolin.betterclouds.rendering.MatrixCapture;
import com.qendolin.betterclouds.rendering.PrepareResult;
import com.qendolin.betterclouds.rendering.opengl.Debug;
import com.qendolin.betterclouds.rendering.opengl.OpenGLRenderer;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.qendolin.betterclouds.compat.GLCompat.instance;
import static com.qendolin.betterclouds.compat.ProfilerWrapper.getProfiler;

@Mixin(value = LevelRenderer.class, priority = 900)
public abstract class WorldRendererMixin implements WorldRendererDuck {

    @Unique
    private final Vector3d better_clouds$tempVector = new Vector3d();

    @Unique
    private OpenGLRenderer better_clouds$cloudRenderer;
    @Unique
    private Frustum better_clouds$frustum;
    @Shadow
    @Final
    private LevelTargetBundle targets;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        if (instance.isIncompatible()) return;
        better_clouds$cloudRenderer = new OpenGLRenderer(Minecraft.getInstance());
    }

    @Override
    public OpenGLRenderer betterclouds$getRenderer() {
        return better_clouds$cloudRenderer;
    }

    @Inject(at = @At("HEAD"), method = "render")
    private void captureFrustum(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci) {
        better_clouds$frustum = cameraState.cullFrustum;
        Vec3 cameraPos = cameraState.pos;
        better_clouds$frustum.prepare(cameraPos.x, cameraPos.y, cameraPos.z);
    }

    @Inject(
            at = @At("HEAD"),
            method = "addCloudsPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/CloudStatus;Lnet/minecraft/world/phys/Vec3;JFIFI)V",
            cancellable = true
    )
    private void renderClouds(FrameGraphBuilder frame, CloudStatus cloudStatus, Vec3 cameraPosition, long gameTime, float partialTicks, int cloudColor, float cloudHeight, int cloudRange, CallbackInfo ci) {
        better_clouds$renderCloudsInternal(frame, cameraPosition, gameTime, partialTicks, ci);
    }

    // NF calls a different overload of addCloudsPass that isn't even in the decompiled source. Like HOW
    @SuppressWarnings({ "MixinAnnotationTarget", "UnresolvedMixinReference" })
    @Inject(
            at = @At("HEAD"),
            method = "addCloudsPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/CloudStatus;Lnet/minecraft/world/phys/Vec3;JFIFILorg/joml/Matrix4fc;)V",
            cancellable = true,
            require = 0     // silently fail if not neoforge
    )
    private void renderCloudsNeoForge(FrameGraphBuilder frameGraphBuilder, CloudStatus _mode, Vec3 cameraPos, long _seed, float _ticks, int _color, float _cloudHeight, int _cloudRenderMode, Matrix4fc _viewMatrix, CallbackInfo ci) {
        better_clouds$renderCloudsInternal(frameGraphBuilder, cameraPos, _seed, _ticks, ci);
    }

    @Unique
    private void better_clouds$renderCloudsInternal(FrameGraphBuilder frameGraphBuilder, Vec3 cameraPos, long gameTime, float ticksInput, CallbackInfo ci) {
        double camX = cameraPos.x, camY = cameraPos.y, camZ = cameraPos.z;
        float tickDelta = Mth.frac(ticksInput);
        if (better_clouds$cloudRenderer == null) return;
        if (instance.isIncompatible()) return;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        if (!ConfigManager.instance().enabledDimensions.contains(level.dimensionTypeRegistration().unwrapKey().orElse(null)))
            return;
        if (!BetterClouds.isEnabled()) return;

        getProfiler().push(BetterCloudsStatic.MODID);
        instance.pushDebugGroupDev("Better Clouds");

        Vector3d cam = better_clouds$tempVector.set(camX, camY, camZ);
        Frustum frustum = this.better_clouds$frustum;

        int ticks = (int) gameTime;
        if (Debug.animationPause >= 0) {
            if (Debug.animationPause == 0) Debug.animationPause = ticks;
            else ticks = Debug.animationPause;
            tickDelta = 0;
        }

        PrepareResult prepareResult = better_clouds$cloudRenderer.prepare(MatrixCapture.capturedViewMat, MatrixCapture.capturedProjMat, ticks, tickDelta, cam);
        if (RenderDoc.isFrameCapturing())
            instance.debugMessage("renderer prepare returned " + prepareResult.name());

        if (prepareResult != PrepareResult.FALLBACK)
            ci.cancel();

        // Note to self: do not use return
        if (prepareResult == PrepareResult.RENDER) {
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
                instance.pushDebugGroupDev("Better Clouds");
                better_clouds$cloudRenderer.render(fticks, ftickDelta, fcam, ffrustumPos, ffrustum);
                getProfiler().pop();
                instance.popDebugGroupDev();
            });
        }

        getProfiler().pop();
        instance.popDebugGroupDev();
    }


    @Inject(at = @At("HEAD"), method = "close")
    private void close(CallbackInfo ci) {
        if (better_clouds$cloudRenderer != null) better_clouds$cloudRenderer.close();
    }
}
