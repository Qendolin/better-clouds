package com.qendolin.betterclouds.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.texture.AbstractTexture;

//? if >=1.21.5 {
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.texture.GlTexture;
//?} else {
/*import com.mojang.blaze3d.platform.GlStateManager;
*///?}


public interface RenderHelper {

    static int getTextureId(AbstractTexture texture) {
        //? if >=1.21.5 {
        if(texture.getGlTexture() instanceof GlTexture glTexture) {
            // yarn name is getGlId, but there is a conflict with iris
            return glTexture.glId();
        } else {
            throw new IllegalStateException("Texture is not a GlTexture");
        }
        //?} else {
        /*return texture.getGlId();
        *///?}
    }

    //? if >=1.21.5 {
    static int getTextureId(GpuTexture texture) {
        if(texture instanceof GlTexture glTexture) {
            return glTexture.glId();
        } else {
            throw new IllegalStateException("Texture is not a GlTexture");
        }
    }
    //?}

    static void bindTexture(AbstractTexture texture) {
        bindTexture(getTextureId(texture));
    }

    //? if >=1.21.5 {
    static void bindTexture(GpuTexture texture) {
        bindTexture(getTextureId(texture));
    }
    //?}

    static void bindTexture(int id) {
        GlStateManager._bindTexture(id);
    }


    static Fog getFog() {
        //? if >=1.21.3 {
        var fog = RenderSystem.getShaderFog();
        return new Fog(fog);
        //?} else {
        /*float[] color = RenderSystem.getShaderFogColor();
        return new Fog(RenderSystem.getShaderFogStart(), RenderSystem.getShaderFogEnd(), RenderSystem.getShaderFogShape(), color[0], color[1], color[2], color[3]);
        *///?}
    }

    record Fog(float start, float end, FogShape shape, float red, float green, float blue, float alpha) {
        public void apply() {
            //? if >=1.21.3 {
            RenderSystem.setShaderFog(new net.minecraft.client.render.Fog(start, end, shape, red, green, blue, alpha));
            //?} else {
            /*RenderSystem.setShaderFogStart(start);
            RenderSystem.setShaderFogEnd(end);
            RenderSystem.setShaderFogShape(shape);
            RenderSystem.setShaderFogColor(red, green, blue, alpha);
            *///?}
        }
        //? if >=1.21.3 {
        public Fog(net.minecraft.client.render.Fog fog) {
            this(fog.start(), fog.end(), fog.shape(), fog.red(), fog.green(), fog.blue(), fog.alpha());
        }
        //?}
    }
}