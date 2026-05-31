package com.qendolin.betterclouds.mixin.required;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.qendolin.betterclouds.rendering.CloudRenderCoordinator;
import net.minecraft.client.*;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 900)
public abstract class WorldRendererMixin {

    @Accessor("targets")
    protected abstract LevelTargetBundle better_clouds$getTargets();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        CloudRenderCoordinator.instance.initialize(Minecraft.getInstance());
    }

    @Inject(at = @At("HEAD"), method = "render")
    private void captureFrustum(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci) {
        CloudRenderCoordinator.instance.captureFrustum(cameraState);
    }

    @Inject(
            at = @At("HEAD"),
            method = "addCloudsPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/CloudStatus;Lnet/minecraft/world/phys/Vec3;JFIFI)V",
            cancellable = true
    )
    private void renderClouds(FrameGraphBuilder frame, CloudStatus cloudStatus, Vec3 cameraPosition, long gameTime, float partialTicks, int cloudColor, float cloudHeight, int cloudRange, CallbackInfo ci) {
        if (CloudRenderCoordinator.instance.renderClouds(frame, better_clouds$getTargets(), cameraPosition, gameTime, partialTicks))
            ci.cancel();
    }

    // NF calls a different overload of addCloudsPass somehow
    @SuppressWarnings({ "MixinAnnotationTarget", "UnresolvedMixinReference" })
    @Inject(
            at = @At("HEAD"),
            method = "addCloudsPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/CloudStatus;Lnet/minecraft/world/phys/Vec3;JFIFILorg/joml/Matrix4fc;)V",
            cancellable = true,
            require = 0     // silently fail if not neoforge
    )
    private void renderCloudsNeoForge(FrameGraphBuilder frameGraphBuilder, CloudStatus _mode, Vec3 cameraPos, long _seed, float _ticks, int _color, float _cloudHeight, int _cloudRenderMode, Matrix4fc _viewMatrix, CallbackInfo ci) {
        if (CloudRenderCoordinator.instance.renderClouds(frameGraphBuilder, better_clouds$getTargets(), cameraPos, _seed, _ticks))
            ci.cancel();
    }


    @Inject(at = @At("HEAD"), method = "close")
    private void close(CallbackInfo ci) {
        CloudRenderCoordinator.instance.close();
    }
}
