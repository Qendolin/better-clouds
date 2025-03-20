package com.qendolin.betterclouds.mixin;

import com.qendolin.betterclouds.util.DisableMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

//? if =1.21.3 || =1.21.4 {
import net.minecraft.client.gl.GpuBuffer;
//?}

//? if <=1.21.4 {
import org.spongepowered.asm.mixin.gen.Accessor;
//?}


@DisableMixin(
    /*? if >1.21.4 >>*/ /*true*/
)
@Pseudo
@Mixin(targets = "net.minecraft.client.gl.VertexBuffer")
public interface VertexBufferAccessor {
    //? if >1.21.4 {
    //?} elif >=1.21.3 {
    @Accessor("vertexBuffer")
    GpuBuffer getVertexBuffer();

    @Accessor("indexBuffer")
    GpuBuffer getIndexBuffer();

    @Accessor("vertexArrayId")
    int getVertexArrayId();
    //?} else {
    /*@Accessor("vertexBufferId")
    int getVertexBufferId();

    @Accessor("indexBufferId")
    int getIndexBufferId();

    @Accessor("vertexArrayId")
    int getVertexArrayId();
    *///?}
}