package com.qendolin.betterclouds.util;

import net.minecraft.client.texture.AbstractTexture;
import org.joml.Matrix4f;
import java.nio.ByteBuffer;

//? if <1.21.6 {
/*import com.mojang.blaze3d.systems.RenderSystem;
*///?}

//? if =1.21.5 {
/*import com.qendolin.betterclouds.mixin.runtime.GlBackendAccessor;
import com.qendolin.betterclouds.mixin.runtime.GlCommandEncoderAccessor;
*///?}

//? if >=1.21.5 {
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.texture.GlTexture;
import org.lwjgl.opengl.GL32;

//?} else {
/*import com.mojang.blaze3d.platform.GlStateManager;
*///?}


public abstract class RenderHelper {

    private static Matrix4f projectionMatrix = new Matrix4f().identity();
    private static Matrix4f viewMatrix = new Matrix4f().identity();

    //? if >=1.21.6 {
    private static int savedShaderId = 0;
    //?}

    private static boolean savedColorMaskRed = true;
    private static boolean savedColorMaskGreen = true;
    private static boolean savedColorMaskBlue = true;
    private static boolean savedColorMaskAlpha = true;
    private static boolean savedDepthMask = true;
    private static final ByteBuffer colorMaskBuffer = ByteBuffer.allocateDirect(4);

    public static int getTextureId(AbstractTexture texture) {
        //? if >=1.21.5 {
        var gpuTexture = texture.getGlTexture();
        return getTextureId(gpuTexture);
        //?} else {
        /*return texture.getGlId();
        *///?}
    }

    //? if >=1.21.5 {
    public static int getTextureId(GpuTexture texture) {
        //? if neoforge && >=1.21.6 {
        /*if (texture instanceof net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuTexture validationGpuTexture) {
            texture = validationGpuTexture.getRealTexture();
        }
        *///?}
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

    public static void saveShader() {
        //? if >=1.21.6 {
        savedShaderId = GL32.glGetInteger(GL32.GL_CURRENT_PROGRAM);
        //?}
    }

    public static void restoreShader() {
        //? if >=1.21.6 {
        GL32.glUseProgram(savedShaderId);
        //?}
    }

    public static void unbindShader() {
        //? if >=1.21.6 {
        //?} else if =1.21.5 {
        /*var backend = (GlBackendAccessor) RenderSystem.getDevice();
        var commandEncoder = (GlCommandEncoderAccessor) backend.getCommandEncoder();
        var current = commandEncoder.getCurrentProgram();
        //? if =1.21.5 {
        /^if (current != null)
            current.unbind();
        ^///?}
        commandEncoder.setCurrentProgram(null);
        commandEncoder.setCurrentPipeline(null);
        *///?} else if >=1.21.3 {
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

    public static void colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        //? if >=1.21.6 {
        GL32.glColorMask(red, green, blue, alpha);
        //?} else {
        /*GlStateManager._colorMask(red, green, blue, alpha);
        *///?}
    }

    public static void saveColorMask() {
        //? if >=1.21.6 {
        colorMaskBuffer.clear();
        GL32.glGetBooleanv(GL32.GL_COLOR_WRITEMASK, colorMaskBuffer);
        savedColorMaskRed = colorMaskBuffer.get(0) == GL32.GL_TRUE;
        savedColorMaskGreen = colorMaskBuffer.get(1) == GL32.GL_TRUE;
        savedColorMaskBlue = colorMaskBuffer.get(2) == GL32.GL_TRUE;
        savedColorMaskAlpha = colorMaskBuffer.get(3) == GL32.GL_TRUE;
        //?}
    }

    public static void restoreColorMask() {
        //? if >=1.21.6 {
        colorMask(savedColorMaskRed, savedColorMaskGreen, savedColorMaskBlue, savedColorMaskAlpha);
        //?}
    }

    public static void depthMask(boolean flag) {
        //? if >=1.21.6 {
        GL32.glDepthMask(flag);
        //?} else {
        /*GlStateManager._depthMask(flag);
        *///?}
    }

    public static void saveDepthMask() {
        //? if >=1.21.6 {
        savedDepthMask = GL32.glGetBoolean(GL32.GL_DEPTH_WRITEMASK);
        //?}
    }

    public static void restoreDepthMask() {
        //? if >=1.21.6 {
        depthMask(savedDepthMask);
        //?}
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
        /*return RenderSystem.getModelViewMatrix();
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