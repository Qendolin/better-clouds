package com.qendolin.betterclouds.mixin.optional;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.qendolin.betterclouds.Main;
import net.minecraft.client.render.DimensionEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DimensionEffects.Overworld.class, priority = 1100)
public abstract class DimensionEffectsOverworldMixin extends DimensionEffects {
    public DimensionEffectsOverworldMixin(float cloudsHeight, boolean alternateSkyColor, SkyType skyType, boolean brightenLighting, boolean darkened) {
        super(cloudsHeight, alternateSkyColor, skyType, brightenLighting, darkened);
    }

    @SuppressWarnings({"UnresolvedMixinReference", "MixinAnnotationTarget"})
    @ModifyReturnValue(method = "getCloudsHeight", at = @At("RETURN"), expect = 0, require = 0)
    public float addCloudsYOffset(float value) {
        if(Main.getConfig().enabled) {
            value += Main.getConfig().yOffset;
        }
        return value;
    }
}
