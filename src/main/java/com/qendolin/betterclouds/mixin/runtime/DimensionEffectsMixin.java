package com.qendolin.betterclouds.mixin.runtime;

//? if <1.21.6 {
/*import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.qendolin.betterclouds.BetterClouds;
import com.qendolin.betterclouds.compat.ModLoaded;
import com.qendolin.betterclouds.config.ConfigManager;
import net.minecraft.client.render.DimensionEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("UnusedMixin")
@Mixin(DimensionEffects.class)
public abstract class DimensionEffectsMixin {

    // This doesn't work with sodium extras see DimensionEffectsOverworldMixin

    @SuppressWarnings("UnresolvedMixinReference")
    @ModifyReturnValue(method = {"getCloudsHeight", "getCloudHeight", "method_28108", "m_108871_"}, remap = false, at = @At("RETURN"))
    public float addCloudsYOffset(float value) {
        if (!BetterClouds.isEnabled())
            return value;
        if (ModLoaded.SODIUM_EXTRA) return value;
        //noinspection ConstantValue,EqualsBetweenInconvertibleTypes
        if (!this.getClass().equals(DimensionEffects.Overworld.class)) return value;

        return value + ConfigManager.instance().yOffset;
    }
}
*///?}