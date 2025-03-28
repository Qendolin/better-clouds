package com.qendolin.betterclouds.mixin.runtime;

//? if fabric {

import com.qendolin.betterclouds.test.GameTest;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnusedMixin")
@Mixin(MinecraftClient.class)
public class GameTestClientMixin {

    @Inject(method = "run", at = @At("TAIL"))
    private void atExit(CallbackInfo ci) {
        GameTest.onClientExit();
    }
}

//?}