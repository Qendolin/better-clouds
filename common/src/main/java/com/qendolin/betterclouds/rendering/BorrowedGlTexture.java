package com.qendolin.betterclouds.rendering;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import com.mojang.renderpearl.api.textures.GpuTexture;

public class BorrowedGlTexture extends GlTexture {

    public BorrowedGlTexture(int id, int width, int height) {
        this(GpuTexture.USAGE_TEXTURE_BINDING, "voxy_opaque_depth", GpuFormat.D24_UNORM_S8_UINT, id, width, height);
    }

    public BorrowedGlTexture(int usage, String label, GpuFormat format, int id, int width, int height) {
        super(
                usage,
                label,
                format,
                width,
                height,
                1,
                1,
                id,
                null    // won't be used as the texture is never closed
        );
    }

    @Override
    public void close() {
        // don't close texture owned by another mod
    }
}
