package com.qendolin.betterclouds.mixin;

import com.qendolin.betterclouds.platform.ModLoader;
import com.qendolin.betterclouds.test.GameTestEnabled;

import java.util.ArrayList;
import java.util.List;

public class RuntimeMixinPlugin extends MixinPlugin {

    @Override
    public List<String> getMixins() {
        if(!ModLoader.isClientEnvironment()) return null;

        //noinspection MismatchedQueryAndUpdateOfCollection
        List<String> classes = new ArrayList<>();

        //? if <1.21.5 {
        /*classes.add("BufferRendererAccessor");
        classes.add("RenderPhaseAccessor");
        classes.add("VertexBufferAccessor");
        *///?}

        if(GameTestEnabled.ENABLED) {
            classes.add("GameTestClientMixin");
            classes.add("GameTestLevelSummaryMixin");
        }

        if(classes.isEmpty())
            return null;

        return classes;
    }
}
