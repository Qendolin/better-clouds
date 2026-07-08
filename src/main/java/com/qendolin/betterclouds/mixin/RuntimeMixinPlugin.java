package com.qendolin.betterclouds.mixin;

import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.compat.ModLoaded;
import com.qendolin.betterclouds.platform.ModLoader;
import com.qendolin.betterclouds.platform.ModVersion;
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

        //? if >=1.21.6 && <1.21.11 {
        /*classes.add("FogRendererMixin");
        classes.add("DimensionTypeMixin");
        classes.add("SimpleOptionAccessor");
        *///?} elif >=1.21.11 {
        classes.add("FogRendererMixin");
        classes.add("WorldEnvironmentAttributeAccessMixin");
        classes.add("SimpleOptionAccessor");
        //?}

        // Don't load YACLCompat class
        boolean isYacl3_8_0 = ModLoader.getModVersion("yet_another_config_lib_v3").asSemVer()
            .map(v -> v.compareTo(new ModVersion.SemVer(3, 8, 0)) >= 0).orElse(false);
        if (isYacl3_8_0) {
            //? if <1.21 {
            /*throw com.qendolin.betterclouds.compat.ReflectAccess.IncompatibleModDependencyException.of("YACL", "For versions <1.21 please use YACL 3.7 or lower");
            *///?} else {
            classes.add("yacl.OptionListGroupSeparatorEntryMixin");
            classes.add("yacl.OptionListOptionEntryMixin");
            //?}
        } else {
            classes.add("yacl.OldOptionListGroupSeparatorEntryMixin");
            classes.add("yacl.OldOptionListOptionEntryMixin");
        }

        if(BetterCloudsStatic.IS_DEV) {
            classes.add("GlDebugMixin");
        }

        if (GameTestEnabled.ENABLED) {
            classes.add("GameTestClientMixin");
            classes.add("GameTestInputUtilMixin");
            classes.add("GameTestLevelSummaryMixin");
            classes.add("GameTestWindowMixin");
        }

        //noinspection ConstantValue
        if (classes.isEmpty())
            return null;

        return classes;
    }
}
