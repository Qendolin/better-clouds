package com.qendolin.betterclouds.mixin.runtime;

import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("UnusedMixin")
@Mixin(LevelSummary.class)
public abstract class GameTestLevelSummaryMixin {

    //? if >1.20.1 {
    @Inject(method = "shouldPromptBackup", at = @At("HEAD"), cancellable = true)
    private void disableBackupPrompt(final CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
    //?}

    @Inject(method = "isExperimental", at = @At("HEAD"), cancellable = true)
    private void disableExperimentalPrompt(final CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }


    @Inject(method = "getConversionWarning", at = @At("HEAD"), cancellable = true)
    private void disableConversionWarning(final CallbackInfoReturnable<LevelSummary.ConversionWarning> cir) {
        cir.setReturnValue(LevelSummary.ConversionWarning.NONE);
    }
}