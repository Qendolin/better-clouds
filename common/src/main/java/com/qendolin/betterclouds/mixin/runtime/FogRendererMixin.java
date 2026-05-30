package com.qendolin.betterclouds.mixin.runtime;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.qendolin.betterclouds.rendering.opengl.RenderHelper;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("UnusedMixin")
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @ModifyReturnValue(
            method = "setupFog(Lnet/minecraft/client/Camera;ILnet/minecraft/client/DeltaTracker;FLnet/minecraft/client/multiplayer/ClientLevel;)Lnet/minecraft/client/renderer/fog/FogData;",
            at = @At("RETURN")
    )
    private FogData captureFogData(FogData fogData) {
        Vector4f color = new Vector4f(fogData.color);
        RenderHelper.setFogDataAndColor(new RenderHelper.FogDataAndColor(fogData, color));
        return fogData;
    }
}
