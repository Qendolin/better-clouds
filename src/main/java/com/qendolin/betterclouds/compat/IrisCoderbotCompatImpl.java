package com.qendolin.betterclouds.compat;

//? if =1.20.1 {
/*import com.qendolin.betterclouds.mixin.optional.ExtendedShaderCoderbotAccessor;
import com.qendolin.betterclouds.mixin.optional.FallbackShaderCoderbotAccessor;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.ExtendedShader;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.ShaderKey;
import net.coderbot.iris.pipeline.newshader.fallback.FallbackShader;
import net.minecraft.client.gl.ShaderProgram;

public class IrisCoderbotCompatImpl extends IrisCompat {
    private static final String INCOMPATIBLE_ERROR = "Incompatible Iris version for Better Clouds, please report this issue to Better Clouds. Details: ";

    public boolean isShadersEnabled() {
        return Iris.getIrisConfig().areShadersEnabled() && Iris.getCurrentPack().isPresent();
    }

    @Override
    public boolean isFrustumCullingDisabled() {
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
        if (pipeline == null) return false;
        return pipeline.shouldDisableFrustumCulling();
    }

    public void bindFramebuffer() {
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
        if (!(pipeline instanceof NewWorldRenderingPipeline corePipeline)) {
            return;
        }

        ShaderProgram program = corePipeline.getShaderMap().getShader(ShaderKey.CLOUDS);
        GlFramebuffer before, after;
        if (program instanceof ExtendedShader extended) {
            ExtendedShaderCoderbotAccessor access = (ExtendedShaderCoderbotAccessor) extended;
            before = access.getWritingToBeforeTranslucent();
            after = access.getWritingToAfterTranslucent();
        } else if (program instanceof FallbackShader fallback) {
            FallbackShaderCoderbotAccessor access = (FallbackShaderCoderbotAccessor) fallback;
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
*///?}