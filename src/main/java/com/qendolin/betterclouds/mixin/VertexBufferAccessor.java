package com.qendolin.betterclouds.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

//? if =1.21.3 || =1.21.4 {
import net.minecraft.client.gl.GpuBuffer;
//?}

//? if <=1.21.4 {
import org.spongepowered.asm.mixin.gen.Accessor;
//?} else {
/*import com.qendolin.betterclouds.util.DisableMixin;
 *///?}

//? if >1.21.4
/*@DisableMixin*/
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