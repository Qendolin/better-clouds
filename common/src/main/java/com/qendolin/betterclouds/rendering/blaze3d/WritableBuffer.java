package com.qendolin.betterclouds.rendering.blaze3d;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.qendolin.betterclouds.BetterCloudsStatic;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.Consumer;

/**
 * Writable GPU buffer; data can change without recreating buffers, but data must always be a fixed size
 */
public class WritableBuffer implements AutoCloseable {
    private final ByteBuffer buffer;
    private final GpuBuffer gpuBuffer;

    public WritableBuffer(String name, int capacity, int usage) {
        String nameWithModId = BetterCloudsStatic.MODID + ":" + name;
        this.buffer = ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
        this.gpuBuffer = RenderSystem.getDevice().createBuffer(
                () -> nameWithModId,
                usage | GpuBuffer.USAGE_COPY_DST,
                capacity
        );
    }

    public GpuBuffer gpuBuffer() {
        return gpuBuffer;
    }

    public void write(Consumer<ByteBuffer> bufferFiller) {
        buffer.clear();
        bufferFiller.accept(buffer);
        buffer.flip();
        RenderSystem.getDevice().createCommandEncoder().writeToBuffer(gpuBuffer.slice(), buffer);
    }

    @Override
    public void close() {
        gpuBuffer.close();
    }
}
