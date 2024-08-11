package com.qendolin.betterclouds.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.compat.SodiumExtraCompat;
import net.minecraft.client.render.DimensionEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionEffects.class)
public abstract class DimensionalEffectsMixin {
    @ModifyReturnValue(method = "getCloudsHeight", at = @At("RETURN"), expect = 0, require = 0)
    private float addCloudsYOffset(float value) {
        //noinspection ConstantValue,EqualsBetweenInconvertibleTypes
        if (!this.getClass().equals(DimensionEffects.Overworld.class)) return value;

        if(Main.getConfig().enabled) {
            value += Main.getConfig().yOffset;
        }
        return value;
    }
}
