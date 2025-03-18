package com.qendolin.betterclouds.util;

import net.minecraft.client.texture.AbstractTexture;

//? if >1.21.4 {
/*import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.texture.GlTexture;
*///?} else {
import com.mojang.blaze3d.platform.GlStateManager;
//?}


public interface RenderHelper {


    static int getTextureId(AbstractTexture texture) {
        //? if >1.21.4 {
        /*if(texture.getGlTexture() instanceof GlTexture glTexture) {
            return glTexture.getGlId();
        } else {
            throw new IllegalStateException("Texture is not a GlTexture");
        }
        *///?} else {
        return texture.getGlId();
        //?}
    }

    //? if >1.21.4 {
    /*static int getTextureId(GpuTexture texture) {
        if(texture instanceof GlTexture glTexture) {
            return glTexture.getGlId();
        } else {
            throw new IllegalStateException("Texture is not a GlTexture");
        }
    }
    *///?}

    static void bindTexture(AbstractTexture texture) {
        bindTexture(getTextureId(texture));
    }

    //? if >1.21.4 {
    /*static void bindTexture(GpuTexture texture) {
        bindTexture(getTextureId(texture));
    }
    *///?}

    static void bindTexture(int id) {
        GlStateManager._bindTexture(id);
    }
}