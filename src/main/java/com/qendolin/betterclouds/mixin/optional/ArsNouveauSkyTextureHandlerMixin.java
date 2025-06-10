package com.qendolin.betterclouds.mixin.optional;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.qendolin.betterclouds.compat.ArsNouveauCompat;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Pseudo
@Mixin(targets = "com.hollingsworth.arsnouveau.client.SkyTextureHandler", remap = false)
public class ArsNouveauSkyTextureHandlerMixin {

    @SuppressWarnings("UnresolvedMixinReference")
    @WrapOperation(method = "renderSky", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/LevelRenderer;renderClouds(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FDDD)V"))
    private static void onRenderClouds(@Coerce Object instance, @Coerce Object poseStack, Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, double camX, double camY, double camZ, Operation<Void> original) {
        try {
            ArsNouveauCompat.IS_SKY_TEXTURE_CLOUDS_RENDERING.set(true);
            original.call(instance, poseStack, frustumMatrix, projectionMatrix, partialTick, camX, camY, camZ);
        } finally {
            ArsNouveauCompat.IS_SKY_TEXTURE_CLOUDS_RENDERING.set(false);
        }
    }
}
