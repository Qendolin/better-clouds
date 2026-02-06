package com.qendolin.betterclouds.util;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.GlTexture;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL32;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public abstract class RenderHelper {

    private static Matrix4f projectionMatrix = new Matrix4f().identity();
    private static Matrix4f viewMatrix = new Matrix4f().identity();

    public record FogDataAndColor(net.minecraft.client.render.fog.FogData fogData, org.joml.Vector4f color) {
    }

    private static FogDataAndColor fogDataAndColor = null;
    private static int savedShaderId = 0;

    private static boolean savedColorMaskRed = true;
    private static boolean savedColorMaskGreen = true;
    private static boolean savedColorMaskBlue = true;
    private static boolean savedColorMaskAlpha = true;
    private static boolean savedDepthMask = true;
    private static final ByteBuffer colorMaskBuffer = ByteBuffer.allocateDirect(4);

    public static int getTextureId(AbstractTexture texture) {
        return getTextureId(texture.getGlTexture());
    }

    public static int getTextureId(GpuTexture texture) {
        texture = unwrapValidationTexture(texture);
        if (texture instanceof GlTexture glTexture) {
            return glTexture.getGlId();
        }
        throw new IllegalStateException("Texture is not a GlTexture");
    }

    public static void bindTexture(AbstractTexture texture) {
        bindTexture(getTextureId(texture));
    }

    public static void bindTexture(GpuTexture texture) {
        bindTexture(getTextureId(texture));
    }

    public static void bindTexture(int id) {
        GlStateManager._bindTexture(id);
    }

    public static void saveShader() {
        savedShaderId = GL32.glGetInteger(GL32.GL_CURRENT_PROGRAM);
    }

    public static void restoreShader() {
        GL32.glUseProgram(savedShaderId);
    }

    public static void unbindShader() {
        // No-op on 1.21.6+
    }

    public static void colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        GL32.glColorMask(red, green, blue, alpha);
    }

    public static void saveColorMask() {
        colorMaskBuffer.clear();
        GL32.glGetBooleanv(GL32.GL_COLOR_WRITEMASK, colorMaskBuffer);
        savedColorMaskRed = colorMaskBuffer.get(0) == GL32.GL_TRUE;
        savedColorMaskGreen = colorMaskBuffer.get(1) == GL32.GL_TRUE;
        savedColorMaskBlue = colorMaskBuffer.get(2) == GL32.GL_TRUE;
        savedColorMaskAlpha = colorMaskBuffer.get(3) == GL32.GL_TRUE;
    }

    public static void restoreColorMask() {
        colorMask(savedColorMaskRed, savedColorMaskGreen, savedColorMaskBlue, savedColorMaskAlpha);
    }

    public static void depthMask(boolean flag) {
        GL32.glDepthMask(flag);
    }

    public static void saveDepthMask() {
        savedDepthMask = GL32.glGetBoolean(GL32.GL_DEPTH_WRITEMASK);
    }

    public static void restoreDepthMask() {
        depthMask(savedDepthMask);
    }

    public static Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public static Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public static void setProjectionMatrix(Matrix4f matrix) {
        projectionMatrix = matrix;
    }

    public static void setViewMatrix(Matrix4f matrix) {
        viewMatrix = matrix;
    }

    public static void setFogDataAndColor(FogDataAndColor data) {
        fogDataAndColor = data;
    }

    public static FogDataAndColor getFogDataAndColor() {
        return fogDataAndColor;
    }

    private static GpuTexture unwrapValidationTexture(GpuTexture texture) {
        if (texture == null) {
            return null;
        }
        if (!"net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuTexture".equals(texture.getClass().getName())) {
            return texture;
        }
        try {
            Method method = texture.getClass().getMethod("getRealTexture");
            Object result = method.invoke(texture);
            if (result instanceof GpuTexture gpuTexture) {
                return gpuTexture;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return texture;
    }
}
