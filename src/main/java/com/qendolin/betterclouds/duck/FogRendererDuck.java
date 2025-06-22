package com.qendolin.betterclouds.duck;

//? if =1.21.6 {
import com.mojang.blaze3d.buffers.GpuBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.world.ClientWorld;
import org.joml.Vector4f;

import java.nio.ByteBuffer;

public interface FogRendererDuck {
    FogApplyResult betterclouds$applyFog(Camera camera, int viewDistance, boolean thick, RenderTickCounter tickCounter, float skyDarkness, ClientWorld world);

    record FogApplyResult(FogData fogData, Vector4f color) {}

    class DummyBuffer implements GpuBuffer.MappedView {

        private final ByteBuffer buffer;

        public DummyBuffer(int size) {
            this.buffer = ByteBuffer.allocate(size);
        }

        public void reset() {
            buffer.clear();
        }

        @Override
        public ByteBuffer data() {
            return buffer;
        }

        @Override
        public void close() {
        }
    }
}
//?}