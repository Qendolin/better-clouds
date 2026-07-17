package com.qendolin.betterclouds.mixin.required;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.qendolin.betterclouds.rendering.CloudRenderCoordinator;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// run before iris, so that wrap operation gets variables after iris postprocessing
@Mixin(value = GameRenderer.class, priority = 1100)
public abstract class GameRendererMixin {

    @WrapOperation(
            method = { "renderLevel" },
            at = { @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V"
            ) }
    )
    private void iris$renderLevel(
            LevelRenderer instance,
            GraphicsResourceAllocator resourceAllocator,
            DeltaTracker deltaTracker,
            boolean renderOutline,
            CameraRenderState cameraState,
            Matrix4fc modelViewMatrix,
            GpuBufferSlice terrainFog,
            Vector4f fogColor,
            boolean shouldRenderSky,
            Operation<Void> original,
            @Local(name = { "projectionMatrix" }) Matrix4f projectionMatrix
    ) {
        CloudRenderCoordinator.instance.captureMatrices((Matrix4f) modelViewMatrix, projectionMatrix);
        original.call(instance, resourceAllocator, deltaTracker, renderOutline, cameraState, modelViewMatrix, terrainFog, fogColor, shouldRenderSky);
    }
}
