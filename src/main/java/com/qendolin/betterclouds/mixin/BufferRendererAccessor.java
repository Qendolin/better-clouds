package com.qendolin.betterclouds.mixin;

import com.qendolin.betterclouds.util.DisableMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

//? if <1.21.5 {
/*import net.minecraft.client.gl.VertexBuffer;
import org.spongepowered.asm.mixin.gen.Accessor;
*///?}

@DisableMixin(
    /*? if >=1.21.5 >>*/ true
)
@Pseudo
@Mixin(targets = "net.minecraft.client.render.BufferRenderer")
public interface BufferRendererAccessor {
    //? if <1.21.5 {
    /*@Accessor("currentVertexBuffer")
    static VertexBuffer getCurrentVertexBuffer() {
        return null; // During hot reload this may get called
    }

    @Accessor("currentVertexBuffer")
    static void setCurrentVertexBuffer(VertexBuffer buffer) {
    }
    *///?}
}
