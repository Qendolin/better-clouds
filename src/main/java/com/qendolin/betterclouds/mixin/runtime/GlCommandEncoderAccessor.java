package com.qendolin.betterclouds.mixin.runtime;

//? if >=1.21.5 {

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gl.ShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@SuppressWarnings("UnusedMixin")
//? if >=1.21.6 {
@Mixin(net.minecraft.client.gl.GlCommandEncoder.class)
//?} else {
/*@Mixin(net.minecraft.client.gl.GlResourceManager.class)
*///?}
public interface GlCommandEncoderAccessor {

    @Accessor("currentProgram")
    void setCurrentProgram(ShaderProgram program);

    @Accessor("currentProgram")
    ShaderProgram getCurrentProgram();

    @Accessor("currentPipeline")
    void setCurrentPipeline(RenderPipeline program);

    @Accessor("currentPipeline")
    RenderPipeline getCurrentPipeline();

}

//?}