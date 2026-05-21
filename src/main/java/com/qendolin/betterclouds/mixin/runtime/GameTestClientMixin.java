package com.qendolin.betterclouds.mixin.runtime;

import com.qendolin.betterclouds.test.GameTest;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if =1.20.1 || =1.20.4 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.slf4j.Logger;
*///?}

import org.objectweb.asm.Opcodes;

@SuppressWarnings("UnusedMixin")
@Mixin(MinecraftClient.class)
public abstract class GameTestClientMixin {

    @Shadow @Final private Window window;

    @Inject(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/MinecraftClient;window:Lnet/minecraft/client/util/Window;",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        )
    )
    private void atWindowCreation(CallbackInfo ci) {
        GLFW.glfwHideWindow(window.getHandle());
    }

    @Inject(method = "run", at = @At("HEAD"))
    private void atEntry(CallbackInfo ci) {
        GLFW.glfwHideWindow(window.getHandle());
    }

    @Inject(method = "run", at = @At("TAIL"))
    private void atExit(CallbackInfo ci) {
        GameTest.onClientExit();
    }

    // This prevents intellij from marking the run as failed
    //? if =1.20.4 {
    /*@WrapOperation(method = "method_55608", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Throwable;)V", remap = false))
    private void dontPrintException(Logger instance, String s, Throwable throwable, Operation<Void> original) {
        instance.error(s);
    }
    *///?}

    // This prevents intellij from marking the run as failed
    //? if =1.20.1 {
    /*@WrapOperation(method = "createUserApiService", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Throwable;)V", remap = false))
    private void dontPrintException(Logger instance, String s, Throwable throwable, Operation<Void> original) {
        instance.error(s);
    }
    *///?}
}