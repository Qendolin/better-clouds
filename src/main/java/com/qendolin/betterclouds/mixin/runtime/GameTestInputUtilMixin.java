package com.qendolin.betterclouds.mixin.runtime;

import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnusedMixin")
@Mixin(InputUtil.class)
public abstract class GameTestInputUtilMixin {

    @Inject(method = "setCursorParameters", at = @At("HEAD"), cancellable = true)
    private static void preventCursorControl(CallbackInfo ci) {
        ci.cancel();
    }
}
