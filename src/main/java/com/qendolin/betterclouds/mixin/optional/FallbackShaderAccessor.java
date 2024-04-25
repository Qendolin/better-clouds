package com.qendolin.betterclouds.mixin.optional;

import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.pipeline.programs.FallbackShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FallbackShader.class)
public interface FallbackShaderAccessor {
    @Accessor(value = "writingToBeforeTranslucent", remap = false)
    GlFramebuffer getWritingToBeforeTranslucent();

    @Accessor(value = "writingToAfterTranslucent", remap = false)
    GlFramebuffer getWritingToAfterTranslucent();
}
