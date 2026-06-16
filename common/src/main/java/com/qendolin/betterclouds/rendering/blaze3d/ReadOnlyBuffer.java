package com.qendolin.betterclouds.rendering.blaze3d;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.qendolin.betterclouds.BetterCloudsStatic;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.Consumer;

/**
 * Read-only GPU buffer; its data can only change if the buffers are recreated
 */
public class ReadOnlyBuffer implements AutoCloseable {
    private final String name;
    private GpuBuffer gpuBuffer;

    public ReadOnlyBuffer(String name) {
        this.name = BetterCloudsStatic.MODID + ":" + name;
    }

    public void recreate(int capacity, int usage, Consumer<ByteBuffer> bufferFiller) {
        close();
        ByteBuffer buf = ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
        bufferFiller.accept(buf);
        buf.flip();
        gpuBuffer = RenderSystem.getDevice().createBuffer(() -> name, usage, buf);
    }

    public long size() {
        return gpuBuffer != null ? gpuBuffer.size() : 0;
    }

    public GpuBuffer gpuBuffer() {
        return gpuBuffer;
    }

    @Override
    public void close() {
        if (gpuBuffer != null)
            gpuBuffer.close();
    }
}
