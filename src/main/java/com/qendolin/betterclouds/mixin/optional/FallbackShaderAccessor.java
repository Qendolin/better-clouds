package com.qendolin.betterclouds.mixin.optional;

import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.pipeline.newshader.fallback.FallbackShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FallbackShader.class)
public interface FallbackShaderAccessor {
    @Accessor(value = "writingToBeforeTranslucent", remap = false)
    GlFramebuffer getWritingToBeforeTranslucent();

    @Accessor(value = "writingToAfterTranslucent", remap = false)
    GlFramebuffer getWritingToAfterTranslucent();
}
