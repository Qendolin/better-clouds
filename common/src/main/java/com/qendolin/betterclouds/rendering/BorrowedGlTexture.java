package com.qendolin.betterclouds.rendering;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.GpuTexture;

public class BorrowedGlTexture extends GlTexture {

    public BorrowedGlTexture(int id, int width, int height) {
        super(
                GpuTexture.USAGE_TEXTURE_BINDING,
                "voxy_opaque_depth",
                GpuFormat.D24_UNORM_S8_UINT,
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
        // don't close depth texture owned by another mod
    }
}