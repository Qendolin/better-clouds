package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.mixin.optional.ExtendedShaderAccessor;
import com.qendolin.betterclouds.mixin.optional.FallbackShaderAccessor;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.pipeline.programs.FallbackShader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gl.ShaderProgram;

public class IrisCompat {
    public static final boolean IS_LOADED = FabricLoader.getInstance().isModLoaded("iris");
    private static final String INCOMPATIBLE_ERROR = "Incompatible Iris version for Better Clouds, please report this issue to Better Clouds. Details: ";

    public static boolean isShadersEnabled() {
        return IS_LOADED && Iris.getIrisConfig().areShadersEnabled() && Iris.getCurrentPack().isPresent();
    }

    public static void bindFramebuffer() {
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
        if (pipeline instanceof IrisRenderingPipeline corePipeline) {
            ShaderProgram program = corePipeline.getShaderMap().getShader(ShaderKey.CLOUDS);
            GlFramebuffer before = null, after = null;
            if (program instanceof ExtendedShader extended) {
                ExtendedShaderAccessor access = (ExtendedShaderAccessor) extended;
                before = access.getWritingToBeforeTranslucent();
                after = access.getWritingToAfterTranslucent();
            } else if (program instanceof FallbackShader fallback) {
                FallbackShaderAccessor access = (FallbackShaderAccessor) fallback;
                before = access.getWritingToBeforeTranslucent();
                after = access.getWritingToAfterTranslucent();
            } else {
                throw new RuntimeException(INCOMPATIBLE_ERROR + "Shader is of type " + program.getClass() + ", Iris Version: " + Iris.getVersion());
            }

            GlFramebuffer required;
            if (corePipeline.isBeforeTranslucent) {
                required = before;
            } else {
                required = after;
            }

            if (required == null) {
                throw new RuntimeException(INCOMPATIBLE_ERROR + "Required framebuffer is null, Iris Version: " + Iris.getVersion());
            }

            if (corePipeline.isBeforeTranslucent) {
                before.bindAsDrawBuffer();
            } else {
                after.bindAsDrawBuffer();
            }
        }
    }
}
