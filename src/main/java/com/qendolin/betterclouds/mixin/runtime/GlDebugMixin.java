package com.qendolin.betterclouds.mixin.runtime;

import net.minecraft.client.gl.GlDebug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnusedMixin")
@Mixin(GlDebug.class)
public class GlDebugMixin {
    @Inject(
        method =
            /*? if >=1.21.5 >>*/ /*"onDebugMessage",*/
            /*? if <1.21.5 >>*/ "info",
        at = @At("TAIL")
    )
    private /*? if <1.21.5 >>*/ static void onDebugMessage(
        int source,
        int type,
        int id,
        int severity,
        int length,
        long message,
        long l,
        CallbackInfo ci) {

        new Exception("Debug Message Stacktrace").printStackTrace();
    }
}
