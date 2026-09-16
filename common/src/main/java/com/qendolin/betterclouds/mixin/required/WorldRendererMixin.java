package com.qendolin.betterclouds.mixin.required;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.client.renderer.oit.OitStage;
import net.minecraft.client.renderer.oit.OitRenderPassProvider;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.qendolin.betterclouds.rendering.CloudRenderCoordinator;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 900)
public abstract class WorldRendererMixin {
    @Shadow
    @Final
    private OptionsRenderState optionsRenderState;

    @Shadow @Final private LevelTargetBundle targets;
    @Unique private boolean betterclouds$replaceClouds;

    @Inject(at = @At("HEAD"), method = "render")
    private void captureFrustumAndCheckState(GraphicsResourceAllocator resourceAllocator, boolean renderOutline, CameraRenderState cameraState, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, boolean hasPostEffects, CallbackInfo ci) {
        betterclouds$replaceClouds = false;
        CloudRenderCoordinator.instance.captureFrustum(cameraState);
        CloudRenderCoordinator.instance.checkState(optionsRenderState);
    }

    // Vanilla clouds now render inside the transparency passes. Add our own pass
    // before executing the frame graph, and skip vanilla only if we replace them.
    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;execute(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder$Inspector;)V"))
    private void addCloudPass(GraphicsResourceAllocator allocator, boolean renderOutline,
            CameraRenderState camera, GpuBufferSlice terrainFog, Vector4f fogColor,
            boolean renderSky, boolean hasPostEffects, CallbackInfo ci,
            @Local(name = "frame") FrameGraphBuilder frame) {
        if (optionsRenderState.cloudStatus != CloudStatus.OFF) {
            betterclouds$replaceClouds = CloudRenderCoordinator.instance.renderClouds(
                    frame, targets, camera.pos, camera.cameraEntityPartialTicks);
        }
    }

    @WrapWithCondition(method = "executeClassicTransparency", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/CloudRenderer;render(Lnet/minecraft/client/CloudStatus;Lcom/mojang/renderpearl/api/commands/RenderPass;)V"))
    private boolean renderVanillaClouds(CloudRenderer renderer, CloudStatus status, RenderPass pass) {
        return !betterclouds$replaceClouds;
    }

    @WrapWithCondition(method = "executeOit", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/CloudRenderer;renderOit(Lnet/minecraft/client/CloudStatus;Lnet/minecraft/client/renderer/oit/OitStage;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Lnet/minecraft/client/renderer/oit/OitRenderPassProvider$Parameters;)V"))
    private boolean renderVanillaOitClouds(CloudRenderer renderer, CloudStatus status, OitStage stage,
            GpuTextureView depth, OitRenderPassProvider.Parameters parameters) {
        return !betterclouds$replaceClouds;
    }


    @Inject(at = @At("HEAD"), method = "close")
    private void close(CallbackInfo ci) {
        CloudRenderCoordinator.instance.close();
    }
}
