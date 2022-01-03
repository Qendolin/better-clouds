package com.qendolin.betterclouds.mixin;

import net.minecraft.client.render.BufferRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BufferRenderer.class)
public interface BufferRendererAccessor {
    @Accessor("currentVertexArray")
    public static void setCurrentVertexArray(int id) {
        throw new AssertionError();
    }
    @Accessor("currentVertexArray")
    public static int getCurrentVertexArray() {
        throw new AssertionError();
    }
    @Accessor("currentVertexBuffer")
    public static void setCurrentVertexBuffer(int id) {
        throw new AssertionError();
    }
    @Accessor("currentVertexBuffer")
    public static int getCurrentVertexBuffer() {
        throw new AssertionError();
    }

}
