package com.qendolin.betterclouds.mixin.runtime;

//? if <1.21.6 {
/*import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.qendolin.betterclouds.BetterClouds;
import com.qendolin.betterclouds.config.ConfigManager;
import net.minecraft.client.render.DimensionEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// This mixin exists for compat with sodium extras
@Mixin(value = DimensionEffects.Overworld.class, priority = 1100)
public abstract class DimensionEffectsOverworldMixin extends DimensionEffects {

    public DimensionEffectsOverworldMixin(float cloudsHeight, boolean alternateSkyColor, SkyType skyType, boolean brightenLighting, boolean darkened) {
        super(cloudsHeight, alternateSkyColor, skyType, brightenLighting, darkened);
    }

    // Note: getCloudsHeight doesn't exist at compile time. Because of that, the full descriptor is required.
    // Need yarn, mojmap and intermediary name with remap=false (Thanks @Bawnorton)
    @SuppressWarnings({"UnresolvedMixinReference", "MixinAnnotationTarget", "target"})
    @ModifyReturnValue(
        method = {"getCloudsHeight()F", "getCloudHeight()F", "method_28108()F"},
        remap = false,
        at = @At("RETURN"), expect = 0, require = 0)
    private float addCloudsYOffset(float value) {
        if (!BetterClouds.isEnabled())
            return value;
        return value + ConfigManager.instance().yOffset;
    }
}
*///?}