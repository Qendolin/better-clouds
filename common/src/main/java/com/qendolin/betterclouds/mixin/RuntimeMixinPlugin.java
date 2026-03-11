package com.qendolin.betterclouds.mixin;

import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.compat.ModLoaded;
import com.qendolin.betterclouds.platform.ModLoader;

import java.util.ArrayList;
import java.util.List;

public class RuntimeMixinPlugin extends MixinPlugin {

    @Override
    public List<String> getMixins() {
        if (!ModLoader.isClientEnvironment()) return null;

        //noinspection MismatchedQueryAndUpdateOfCollection
        List<String> classes = new ArrayList<>();

        classes.add("FogRendererMixin");
        classes.add("SimpleOptionAccessor");

        if (ModLoaded.SODIUM) {
            classes.add("SodiumGameOptionPagesMixin");
            classes.add("SodiumOptionGroupBuilderAccessor");
        }

        classes.add("yacl.OptionListGroupSeparatorEntryMixin");
        classes.add("yacl.OptionListOptionEntryMixin");

        if (BetterCloudsStatic.IS_DEV) {
            classes.add("GlDebugMixin");
        }

        //noinspection ConstantValue
        if (classes.isEmpty())
            return null;

        return classes;
    }
}
