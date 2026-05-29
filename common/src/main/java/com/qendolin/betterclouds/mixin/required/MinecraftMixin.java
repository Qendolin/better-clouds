package com.qendolin.betterclouds.mixin.required;

import com.qendolin.betterclouds.renderdoc.CaptureManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(
            method = "renderFrame(Z)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/GpuSurface;present()V", shift = At.Shift.AFTER)
    )
    private void afterSwapBuffers(boolean tick, CallbackInfo ci) {
        CaptureManager.onSwapBuffers();
    }
}
