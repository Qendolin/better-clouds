package com.qendolin.betterclouds.mixin.runtime;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.util.Window;
//? if !forge {
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?}

@SuppressWarnings("UnusedMixin")
@Mixin(Window.class)
public abstract class GameTestWindowMixin {

    //? if !forge {
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwDefaultWindowHints()V", shift = At.Shift.AFTER, remap = false))
    private void onInit(CallbackInfo ci) {
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
    }
    //?}

}
