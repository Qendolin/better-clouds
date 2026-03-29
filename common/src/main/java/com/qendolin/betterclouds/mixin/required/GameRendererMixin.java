package com.qendolin.betterclouds.mixin.required;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.qendolin.betterclouds.compat.IrisCompat;
import com.qendolin.betterclouds.util.RenderHelper;
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
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;ZLnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;)V"
            )
    )
    private void captureCloudPassMatrices(
            DeltaTracker deltaTracker,
            CallbackInfo ci,
            @Local(name = "modelViewMatrix") Matrix4fc viewMatrix,
            @Local(name = "projectionMatrix") Matrix4f projectionMatrix,
            @Local(name = "bobStack") PoseStack bobStack
    ) {
        Matrix4f capturedViewMatrix = new Matrix4f(viewMatrix);
        if (IrisCompat.instance().isShadersEnabled()) {
            // Iris moves bob/hurt from the projection matrix onto the model-view matrix right before LevelRenderer.renderLevel.
            capturedViewMatrix.mulLocal(bobStack.last().pose());
        }
        RenderHelper.setViewMatrix(capturedViewMatrix);
        RenderHelper.setProjectionMatrix(new Matrix4f(projectionMatrix));
    }
}
