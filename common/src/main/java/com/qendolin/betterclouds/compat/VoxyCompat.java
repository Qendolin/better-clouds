package com.qendolin.betterclouds.compat;

import com.mojang.blaze3d.textures.GpuTextureView;
import org.joml.Matrix4f;

public abstract class VoxyCompat {
    public static VoxyCompat instance;

    public static void initialize() {
        if (instance != null) return;
        instance = ModLoaded.VOXY ? new VoxyCompatImpl() : new Stub();
    }

    public abstract Matrix4f getProjectionMatrix();
    public abstract GpuTextureView getOpaqueDepthTexture();
    public abstract boolean isEnabled();

    public static class Stub extends VoxyCompat {
        @Override
        public Matrix4f getProjectionMatrix() {
            return null;
        }

        @Override
        public GpuTextureView getOpaqueDepthTexture() {
            return null;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }
}
