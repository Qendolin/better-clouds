package com.qendolin.betterclouds.mixin;

import com.qendolin.betterclouds.compat.ModLoaded;

public class OptionalMixinPlugin extends MixinPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("DimensionEffectsOverworldMixin")) {
            return ModLoaded.SODIUM_EXTRA;
        }
        if (mixinClassName.endsWith("BackgroundRendererMixinMixin")) {
            return ModLoaded.SODIUM_EXTRA;
        }
        if (mixinClassName.endsWith("ExtendedShaderAccessor") || mixinClassName.endsWith("FallbackShaderAccessor")) {
            return ModLoaded.IRIS;
        }
        return true;
    }
}
