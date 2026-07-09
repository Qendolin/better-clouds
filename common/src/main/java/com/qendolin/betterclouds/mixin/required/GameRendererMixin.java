package com.qendolin.betterclouds.mixin.required;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.qendolin.betterclouds.compat.IrisCompat;
import com.qendolin.betterclouds.rendering.CloudRenderCoordinator;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(
            method = "renderLevel(Lnet/minecraft/client/DeltaTracker;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V"
            )
    )
    private void captureCloudPassMatrices(
            DeltaTracker deltaTracker,
            CallbackInfo ci,
            @Local(name = "modelViewMatrix") Matrix4fc modelViewMatrix,
            @Local(name = "projectionMatrix") Matrix4f projectionMatrix,
            @Local(name = "bobStack") PoseStack bobStack
    ) {
        Matrix4f capturedProjectionMatrix = new Matrix4f(projectionMatrix);
        if (IrisCompat.instance().isShadersEnabled()) {
            // Iris stores bob/hurt/nausea in bobStack, then moves it onto the model-view matrix before level rendering.
            capturedProjectionMatrix.mul(bobStack.last().pose());
        }
        CloudRenderCoordinator.instance.captureMatrices(modelViewMatrix, capturedProjectionMatrix);
    }
}
