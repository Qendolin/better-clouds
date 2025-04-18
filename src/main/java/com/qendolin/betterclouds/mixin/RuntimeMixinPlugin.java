package com.qendolin.betterclouds.mixin;

import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.platform.ModLoader;
import com.qendolin.betterclouds.test.GameTestEnabled;

import java.util.ArrayList;
import java.util.List;

public class RuntimeMixinPlugin extends MixinPlugin {

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
        classes.add("GlResourceManagerAccessor");
        classes.add("GlBackendAccessor");
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
