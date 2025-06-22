package com.qendolin.betterclouds.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.texture.AbstractTexture;
import org.joml.Matrix4f;

//? if >=1.21.5 {
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.textures.GpuTexture;
import com.qendolin.betterclouds.mixin.runtime.GlBackendAccessor;
import com.qendolin.betterclouds.mixin.runtime.GlCommandEncoderAccessor;
import net.minecraft.client.texture.GlTexture;
import org.joml.Matrix4fc;
//?} else {
/*import com.mojang.blaze3d.platform.GlStateManager;
 *///?}


public abstract class RenderHelper {

    private static Matrix4f projectionMatrix = new Matrix4f().identity();
    private static Matrix4f viewMatrix = new Matrix4f().identity();

    public static int getTextureId(AbstractTexture texture) {
        //? if >=1.21.5 {
        if (texture.getGlTexture() instanceof GlTexture glTexture) {
            return glTexture.getGlId();
        } else {
            throw new IllegalStateException("Texture is not a GlTexture");
        }
        //?} else {
        /*return texture.getGlId();
         *///?}
    }

    //? if >=1.21.5 {
    public static int getTextureId(GpuTexture texture) {
        if (texture instanceof GlTexture glTexture) {
            return glTexture.getGlId();
        } else {
            throw new IllegalStateException("Texture is not a GlTexture");
        }
    }
    //?}

    public static void bindTexture(AbstractTexture texture) {
        bindTexture(getTextureId(texture));
    }

    //? if >=1.21.5 {
    public static void bindTexture(GpuTexture texture) {
        bindTexture(getTextureId(texture));
    }
    //?}

    public static void bindTexture(int id) {
        GlStateManager._bindTexture(id);
    }

    public static void unbindShader() {
        //? if >=1.21.5 {
        var backend = (GlBackendAccessor) RenderSystem.getDevice();
        var commandEncoder = (GlCommandEncoderAccessor) backend.getCommandEncoder();
        var current = commandEncoder.getCurrentProgram();
        //? if =1.21.5 {
        /*if (current != null)
            current.unbind();
        *///?}
        commandEncoder.setCurrentProgram(null);
        commandEncoder.setCurrentPipeline(null);
        //?} else if >=1.21.3 {
        /*var current = RenderSystem.getShader();
        if(current != null)
            current.unbind();
        RenderSystem.setShader((net.minecraft.client.gl.ShaderProgram) null);
        
        *///?} else {
        /*var current = RenderSystem.getShader();
        if(current != null)
            current.unbind();
        RenderSystem.setShader(() -> null);
        *///?}
    }

    public static Matrix4f getProjectionMatrix() {
        //? if >=1.21.6 {
        return projectionMatrix;
        //?} else {
        /*return RenderSystem.getProjectionMatrix();
         *///?}
    }

    public static Matrix4f getViewMatrix() {
        //? if >=1.21.6 {
        return viewMatrix;
        //?} else {
        /*return RenderSystem.getProjectionMatrix();
         *///?}
    }

    //? if >=1.21.6 {
    public static void setProjectionMatrix(Matrix4f matrix) {
        projectionMatrix = matrix;
    }

    public static void setViewMatrix(Matrix4f matrix) {
        viewMatrix = matrix;
    }
    //?}
}