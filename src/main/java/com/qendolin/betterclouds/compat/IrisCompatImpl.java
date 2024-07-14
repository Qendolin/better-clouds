package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.mixin.optional.ExtendedShaderAccessor;
import com.qendolin.betterclouds.mixin.optional.FallbackShaderAccessor;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.fallback.FallbackShader;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.ExtendedShader;
import net.coderbot.iris.pipeline.newshader.ShaderKey;
import net.minecraft.client.gl.ShaderProgram;

public class IrisCompatImpl extends IrisCompat {
    private static final String INCOMPATIBLE_ERROR = "Incompatible Iris version for Better Clouds, please report this issue to Better Clouds. Details: ";

    public boolean isShadersEnabled() {
        return Iris.getIrisConfig().areShadersEnabled() && Iris.getCurrentPack().isPresent();
    }

    public void bindFramebuffer() {
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
        if (!(pipeline instanceof NewWorldRenderingPipeline corePipeline)) {
            return;
        }

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
