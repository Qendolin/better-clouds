package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.rendering.TextureWrapper;
import org.joml.Matrix4f;

public abstract class VoxyCompat {
    public static VoxyCompat instance;

    public static void initialize() {
        if (instance != null) return;
        instance = ModLoaded.VOXY ? new VoxyCompatImpl() : new Stub();
    }

    public abstract Matrix4f getProjectionMatrix();

    public abstract TextureWrapper getOpaqueDepthTexture();

    public abstract boolean isEnabled();

    /**
     * Voxy also falls back to [-1, 1] when clip control is not supported
     */
    public abstract boolean isZNeg1To1();

    public static class Stub extends VoxyCompat {
        @Override
        public Matrix4f getProjectionMatrix() {
            return null;
        }

        @Override
        public TextureWrapper getOpaqueDepthTexture() {
            return null;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public boolean isZNeg1To1() {
            return false;
        }
    }
}
