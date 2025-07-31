package com.qendolin.betterclouds.mixin.optional;

import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.pipeline.programs.FallbackShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = FallbackShader.class, remap = false)
public interface FallbackShaderAccessor {
    @Accessor(value = "writingToBeforeTranslucent")
    GlFramebuffer getWritingToBeforeTranslucent();

    @Accessor(value = "writingToAfterTranslucent")
    GlFramebuffer getWritingToAfterTranslucent();
}
