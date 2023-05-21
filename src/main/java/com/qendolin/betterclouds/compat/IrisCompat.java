package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.mixin.ExtendedShaderAccessor;
import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.ShaderKey;
import net.fabricmc.loader.api.FabricLoader;

public class IrisCompat {
    public static final boolean IS_LOADED = FabricLoader.getInstance().isModLoaded("iris");

    public static boolean isShadersEnabled() {
        return IS_LOADED && Iris.getIrisConfig().areShadersEnabled() && Iris.getCurrentPack().isPresent();
    }

    public static void bindFramebuffer() {
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
        if(pipeline instanceof NewWorldRenderingPipeline corePipeline) {
            ExtendedShaderAccessor irisShader = (ExtendedShaderAccessor) corePipeline.getShaderMap().getShader(ShaderKey.CLOUDS);
            if(corePipeline.isBeforeTranslucent) {
                irisShader.getWritingToBeforeTranslucent().bindAsDrawBuffer();
            } else {
                irisShader.getWritingToAfterTranslucent().bindAsDrawBuffer();
            }
        }
    }
}
