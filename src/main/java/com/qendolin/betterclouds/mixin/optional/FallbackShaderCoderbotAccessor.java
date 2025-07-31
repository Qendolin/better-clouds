package com.qendolin.betterclouds.mixin.optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
//? if =1.20.1 {
/*import org.spongepowered.asm.mixin.gen.Accessor;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
*///?}

@Pseudo
@Mixin(targets = "net.coderbot.iris.pipeline.newshader.fallback.FallbackShader", remap = false)
public interface FallbackShaderCoderbotAccessor {
    //? if =1.20.1 {
    /*@Accessor(value = "writingToBeforeTranslucent")
    GlFramebuffer getWritingToBeforeTranslucent();

    @Accessor(value = "writingToAfterTranslucent")
    GlFramebuffer getWritingToAfterTranslucent();
    *///?}
}
