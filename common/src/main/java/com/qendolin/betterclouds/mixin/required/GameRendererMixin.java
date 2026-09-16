package com.qendolin.betterclouds.mixin.required;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.qendolin.betterclouds.rendering.CloudRenderCoordinator;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// run before iris, so that wrap operation gets variables after iris postprocessing
@Mixin(value = GameRenderer.class, priority = 1100)
public abstract class GameRendererMixin {

    @WrapOperation(
            method = { "renderLevel" },
            at = { @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;Lorg/joml/Vector4f;ZZ)V"
            ) }
    )
    private void iris$renderLevel(
            LevelRenderer instance,
            GraphicsResourceAllocator resourceAllocator,
            boolean renderOutline,
            CameraRenderState cameraState,
            GpuBufferSlice terrainFog,
            Vector4f fogColor,
            boolean shouldRenderSky,
            boolean hasPostEffects,
            Operation<Void> original
    ) {
        CloudRenderCoordinator.instance.captureMatrices(cameraState.viewRotationMatrix, cameraState.projectionMatrix);
        original.call(instance, resourceAllocator, renderOutline, cameraState, terrainFog, fogColor, shouldRenderSky, hasPostEffects);
    }
}
