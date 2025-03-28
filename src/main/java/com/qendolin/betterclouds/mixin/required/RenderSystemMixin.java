package com.qendolin.betterclouds.mixin.required;

import com.mojang.blaze3d.systems.RenderSystem;
import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.renderdoc.CaptureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=1.21.3 {
import net.minecraft.client.util.tracy.TracyFrameCapturer;
//?}

//? if >=1.21.5 {
import com.mojang.blaze3d.shaders.ShaderType;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;
//?}

@Mixin(RenderSystem.class)
public abstract class RenderSystemMixin {

    @Inject(method = "initRenderer", at = @At("TAIL"))
    //? if >=1.21.5 {
    private static void afterInitRenderer(long windowHandle, int debugVerbosity, boolean sync, BiFunction<Identifier, ShaderType, String> shaderSourceGetter, boolean renderDebugLabels, CallbackInfo ci) {
    //?} else {
    /*private static void afterInitRenderer(int debugVerbosity, boolean debugSync, CallbackInfo ci) {
    *///?}
        Main.initGlCompat();
    }

    @Inject(method = "flipFrame", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapBuffers(J)V", shift = At.Shift.AFTER, remap = false))
    //? if >=1.21.3 {
    private static void afterSwapBuffers(long window, TracyFrameCapturer capturer, CallbackInfo ci) {
    //?} else {
     /*private static void afterSwapBuffers(long window, CallbackInfo ci) {
    *///?}
        CaptureManager.onSwapBuffers();
    }
}
