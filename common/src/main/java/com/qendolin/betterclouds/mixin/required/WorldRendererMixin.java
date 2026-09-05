package com.qendolin.betterclouds.mixin.required;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.qendolin.betterclouds.rendering.CloudRenderCoordinator;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 900)
public abstract class WorldRendererMixin {
    @Shadow
    @Final
    private OptionsRenderState optionsRenderState;

    @Accessor("targets")
    protected abstract LevelTargetBundle betterclouds$getTargets();

    @Inject(at = @At("HEAD"), method = "render")
    private void captureFrustumAndCheckState(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci) {
        CloudRenderCoordinator.instance.captureFrustum(cameraState);
        CloudRenderCoordinator.instance.checkState(optionsRenderState);
    }

    @Inject(
            at = @At("HEAD"),
            method = "addCloudsPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/CloudStatus;Lnet/minecraft/world/phys/Vec3;JFIFI)V",
            cancellable = true
    )
    private void renderClouds(FrameGraphBuilder frame, CloudStatus cloudStatus, Vec3 cameraPosition, long gameTime, float partialTicks, int cloudColor, float cloudHeight, int cloudRange, CallbackInfo ci) {
        if (CloudRenderCoordinator.instance.renderClouds(frame, betterclouds$getTargets(), cameraPosition, partialTicks))
            ci.cancel();
    }

    // NF calls a different overload of addCloudsPass somehow
    // 2026/7/27: this probably doesn't even work anymore, todo remove if the signature has changed
    @SuppressWarnings({ "MixinAnnotationTarget", "UnresolvedMixinReference" })
    @Inject(
            at = @At("HEAD"),
            method = "addCloudsPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/CloudStatus;Lnet/minecraft/world/phys/Vec3;JFIFILorg/joml/Matrix4fc;)V",
            cancellable = true,
            require = 0     // silently fail if not neoforge
    )
    private void renderCloudsNeoForge(FrameGraphBuilder frameGraphBuilder, CloudStatus _mode, Vec3 cameraPos, long _seed, float _ticks, int _color, float _cloudHeight, int _cloudRenderMode, Matrix4fc _viewMatrix, CallbackInfo ci) {
        if (CloudRenderCoordinator.instance.renderClouds(frameGraphBuilder, betterclouds$getTargets(), cameraPos, _ticks))
            ci.cancel();
    }


    @Inject(at = @At("HEAD"), method = "close")
    private void close(CallbackInfo ci) {
        CloudRenderCoordinator.instance.close();
    }
}
