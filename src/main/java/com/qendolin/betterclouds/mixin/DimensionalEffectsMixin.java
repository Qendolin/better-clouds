package com.qendolin.betterclouds.mixin;

import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.compat.SodiumExtraCompat;
import net.minecraft.client.render.DimensionEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionEffects.class)
public abstract class DimensionalEffectsMixin {
    @Inject(method = "getCloudsHeight", at = @At("RETURN"), cancellable = true)
    public void addCloudsYOffset(CallbackInfoReturnable<Float> cir) {
        //noinspection ConstantValue,EqualsBetweenInconvertibleTypes
        if(!this.getClass().equals(DimensionEffects.Overworld.class)) return;
        // This case is handled in DimensionEffectsOverworldMixin
        if(SodiumExtraCompat.IS_LOADED) return;

        cir.setReturnValue(cir.getReturnValue() + Main.getConfig().yOffset);
    }
}
