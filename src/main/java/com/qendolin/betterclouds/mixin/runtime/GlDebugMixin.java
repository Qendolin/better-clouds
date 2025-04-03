package com.qendolin.betterclouds.mixin.runtime;

import net.minecraft.client.gl.GlDebug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnusedMixin")
@Mixin(GlDebug.class)
public class GlDebugMixin {
    @Inject(method = "onDebugMessage", at = @At("TAIL"))
    private void onDebugMessage(int source, int type, int id, int severity, int length, long message, long l, CallbackInfo ci) {
        new Exception("Debug Message Stacktrace").printStackTrace();
    }
}
