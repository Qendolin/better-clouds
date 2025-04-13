package com.qendolin.betterclouds.mixin.runtime;

//? if >=1.21.5 {

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gl.GlResourceManager;
import net.minecraft.client.gl.ShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@SuppressWarnings("UnusedMixin")
@Mixin(GlResourceManager.class)
public interface GlResourceManagerAccessor {

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