package com.qendolin.betterclouds.mixin.optional;

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

    // TODO: See if this works
//    @SuppressWarnings({"UnresolvedMixinReference", "MixinAnnotationTarget"})
//    @Inject(method = "getCloudsHeight", at = @At("RETURN"), cancellable = true, expect = 0, require = 0)
//    public void addCloudsYOffset(CallbackInfoReturnable<Float> cir) {
//        cir.setReturnValue(cir.getReturnValue() + Main.getConfig().yOffset);
//    }

    @Override
    public float getCloudsHeight() {
        return super.getCloudsHeight() + Main.getConfig().yOffset;
    }
}
