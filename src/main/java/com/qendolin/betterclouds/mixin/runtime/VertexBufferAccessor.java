package com.qendolin.betterclouds.mixin.runtime;

//? if <1.21.5 {

import net.minecraft.client.gl.VertexBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if >=1.21.3 {
import net.minecraft.client.gl.GpuBuffer;
//?}


@SuppressWarnings("UnusedMixin")
@Mixin(VertexBuffer.class)
public interface VertexBufferAccessor {
    //? if >=1.21.3 {
    @Accessor("vertexBuffer")
    GpuBuffer getVertexBuffer();

    @Accessor("indexBuffer")
    GpuBuffer getIndexBuffer();
    //?} else {
    /*@Accessor("vertexBufferId")
    int getVertexBufferId();

    @Accessor("indexBufferId")
    int getIndexBufferId();
    *///?}

    @Accessor("vertexArrayId")
    int getVertexArrayId();
}
//?}