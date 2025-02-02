package com.qendolin.betterclouds.mixin;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BufferRenderer.class)
public interface BufferRendererAccessor {
    @Accessor("currentVertexBuffer")
    static VertexBuffer getCurrentVertexBuffer() {
        // can be called when hor reloading
        return null;
    }

    @Accessor("currentVertexBuffer")
    static void setCurrentVertexBuffer(VertexBuffer buffer) {
        throw new AssertionError();
    }

}
