package com.qendolin.betterclouds.mixin.required;

import com.qendolin.betterclouds.BetterClouds;
import com.qendolin.betterclouds.mixin.duck.WorldRendererDuck;
import com.qendolin.betterclouds.renderdoc.CaptureManager;
import com.qendolin.betterclouds.rendering.opengl.OpenGLRenderer;
import net.minecraft.client.GameLoadCookie;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.qendolin.betterclouds.compat.GLCompat.glCompat;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    @Final
    public LevelRenderer levelRenderer;

    @Shadow
    public abstract ResourceManager getResourceManager();

    @Inject(
            method = "renderFrame(Z)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/GpuSurface;present()V", shift = At.Shift.AFTER)
    )
    private void afterSwapBuffers(boolean advanceGameTime, CallbackInfo ci) {
        CaptureManager.onSwapBuffers();
    }

    @Inject(at = @At("TAIL"), method = "setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V")
    private void setBetterCloudsWorld(ClientLevel world, CallbackInfo ci) {
        OpenGLRenderer renderer = better_clouds$getRenderer();
        if (renderer != null) renderer.setWorld(world);
    }

    @Inject(at = @At("TAIL"), method = "onResourceLoadFinished(Lnet/minecraft/client/GameLoadCookie;)V")
    private void reloadBetterCloudsRenderer(GameLoadCookie gameLoadCookie, CallbackInfo ci) {
        if (!BetterClouds.isInitialized()) return;
        if (glCompat.isIncompatible()) return;
        OpenGLRenderer renderer = better_clouds$getRenderer();
        if (renderer != null) renderer.reload(getResourceManager());
    }

    @Unique
    private OpenGLRenderer better_clouds$getRenderer() {
        return ((WorldRendererDuck) levelRenderer).betterclouds$getRenderer();
    }
}
