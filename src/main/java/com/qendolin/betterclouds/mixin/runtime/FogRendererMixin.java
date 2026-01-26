package com.qendolin.betterclouds.mixin.runtime;

//? if >=1.21.6 && <1.22.0 {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.qendolin.betterclouds.util.RenderHelper;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@SuppressWarnings("UnusedMixin")
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {


    @Inject(
        //? if >=1.21.11 {
        method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
        //?} else {
        /*method = "applyFog(Lnet/minecraft/client/render/Camera;IZLnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",*/
        //?}
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/fog/FogRenderer;applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V", shift = At.Shift.AFTER)
    )
    private void captureFogData(CallbackInfoReturnable<Vector4f> cir, @Local(ordinal = 0) FogData fogData, @Share("fogData") LocalRef<FogData> fogDataRef) {
        fogDataRef.set(fogData);
    }

    @ModifyReturnValue(
        //? if >=1.21.11 {
        method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
        //?} else {
        /*method = "applyFog(Lnet/minecraft/client/render/Camera;IZLnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",*/
        //?}
        at = @At("RETURN")
    )
    private Vector4f captureFogColor(Vector4f color, @Share("fogData") LocalRef<FogData> fogDataRef) {
        RenderHelper.setFogDataAndColor(new RenderHelper.FogDataAndColor(fogDataRef.get(), color));
        return color;
    }
}
//?}
