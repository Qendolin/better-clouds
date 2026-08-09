package com.qendolin.betterclouds.rendering.opengl;

import com.mojang.blaze3d.opengl.*;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33C;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public abstract class RenderHelper {

    private static final ByteBuffer colorMaskBuffer = ByteBuffer.allocateDirect(4);
    private static FogDataAndColor fogDataAndColor = null;
    private static int savedShaderId = 0;

    private static boolean savedColorMaskRed = true;
    private static boolean savedColorMaskGreen = true;
    private static boolean savedColorMaskBlue = true;
    private static boolean savedColorMaskAlpha = true;
    private static boolean savedDepthMask = true;

    public static int getTextureId(AbstractTexture texture) {
        return getTextureId(texture.getTexture());
    }

    public static int getTextureId(GpuTexture texture) {
        texture = unwrapValidationTexture(texture);
        if (texture instanceof GlTexture glTexture) {
            return glTexture.glId();
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

    public static int getSamplerId(GpuSampler sampler) {
        if (sampler instanceof GlSampler glSampler) {
                return glSampler.getId();
            }
        throw new IllegalStateException("Sampler is not a GlSampler: " + sampler.getClass().getName());
    }

    public static void bindSampler(int unit, GpuSampler sampler) {
        bindSampler(unit, getSamplerId(sampler));
    }

    public static void bindSampler(int unit, int id) {
        GL33C.glBindSampler(unit, id);
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

    public static FogDataAndColor getFogDataAndColor() {
        return fogDataAndColor;
    }

    public static void setFogDataAndColor(FogDataAndColor data) {
        fogDataAndColor = data;
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

    public record FogDataAndColor(net.minecraft.client.renderer.fog.FogData fogData, org.joml.Vector4f color) {
    }
}
