package com.qendolin.betterclouds.mixin.required;

import com.qendolin.betterclouds.BetterClouds;
import com.qendolin.betterclouds.renderdoc.CaptureManager;
import com.qendolin.betterclouds.rendering.CloudRenderCoordinator;
import net.minecraft.client.GameLoadCookie;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.qendolin.betterclouds.compat.GLCompat.instance;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    public abstract ResourceManager getResourceManager();

    @Inject(
            method = "renderFrame(Z)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/GpuSurface;present()V", shift = At.Shift.AFTER)
    )
    private void afterSwapBuffers(boolean advanceGameTime, CallbackInfo ci) {
        CaptureManager.onSwapBuffers();
    }

    @Inject(at = @At("TAIL"), method = "onResourceLoadFinished(Lnet/minecraft/client/GameLoadCookie;)V")
    private void reloadBetterCloudsRenderer(GameLoadCookie loadCookie, CallbackInfo ci) {
        if (!BetterClouds.isInitialized()) return;
        if (instance.isIncompatible()) return;
        CloudRenderCoordinator.instance.reload(getResourceManager());
    }
}
