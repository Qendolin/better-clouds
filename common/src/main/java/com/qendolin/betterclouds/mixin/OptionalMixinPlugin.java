package com.qendolin.betterclouds.mixin;

import com.qendolin.betterclouds.compat.ModLoaded;

public class OptionalMixinPlugin extends MixinPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("ExtendedShaderAccessor") || mixinClassName.endsWith("FallbackShaderAccessor") || mixinClassName.endsWith("GlCommandEncoderMixin")) {
            return ModLoaded.IRIS;
        }
        return true;
    }
}
