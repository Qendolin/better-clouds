package com.qendolin.betterclouds.mixin;

import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.compat.ModLoaded;
import com.qendolin.betterclouds.platform.ModLoader;
import com.qendolin.betterclouds.test.GameTestEnabled;

import java.util.ArrayList;
import java.util.List;

public class RuntimeMixinPlugin extends MixinPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("DimensionEffectsOverworldMixin")) {
            return ModLoaded.SODIUM_EXTRA;
        }
        return super.shouldApplyMixin(targetClassName, mixinClassName);
    }

    @Override
    public List<String> getMixins() {
        if (!ModLoader.isClientEnvironment()) return null;

        //noinspection MismatchedQueryAndUpdateOfCollection
        List<String> classes = new ArrayList<>();

        //? if <1.21.5 {
        /*classes.add("BufferRendererAccessor");
        classes.add("RenderPhaseAccessor");
        classes.add("VertexBufferAccessor");
        *///?}

        //? if >=1.21.5 {
        classes.add("GlBackendAccessor");
        classes.add("GlCommandEncoderAccessor");
        //?}

        //? if <1.21.6 {
        /*classes.add("BackgroundRendererMixinMixin");
        classes.add("DimensionEffectsMixin");
        classes.add("DimensionEffectsOverworldMixin");
        *///?}

        //? if >=1.21.6 {
        classes.add("FogRendererMixin");
        classes.add("DimensionTypeMixin");
        //?}

        if(BetterCloudsStatic.IS_DEV) {
            classes.add("GlDebugMixin");
        }

        if (GameTestEnabled.ENABLED) {
            classes.add("GameTestClientMixin");
            classes.add("GameTestInputUtilMixin");
            classes.add("GameTestLevelSummaryMixin");
            classes.add("GameTestWindowMixin");
        }

        if (classes.isEmpty())
            return null;

        return classes;
    }
}
