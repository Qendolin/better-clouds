package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.rendering.TextureWrapper;
import org.joml.Matrix4f;

public abstract class VoxyCompat {
    public static VoxyCompat instance;

    public static void initialize() {
        if (instance != null) return;
        instance = ModLoaded.VOXY ? new VoxyCompatImpl() : new VoxyCompat() {
        };
    }

    public Matrix4f getProjectionMatrix() {
        return null;
    }

    public TextureWrapper getOpaqueDepthTexture() {
        return null;
    }

    public boolean isEnabled() {
        return false;
    }

    public boolean isZNeg1To1() {
        return false;
    }
}
