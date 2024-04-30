package com.qendolin.betterclouds.mixin;

import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExtendedShader.class)
public interface ExtendedShaderAccessor {
    @Accessor(value = "writingToBeforeTranslucent", remap = false)
    GlFramebuffer getWritingToBeforeTranslucent();

    @Accessor(value = "writingToAfterTranslucent", remap = false)
    GlFramebuffer getWritingToAfterTranslucent();
}
