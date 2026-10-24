package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.rendering.TextureWrapper;

public abstract class VoxyCompat {
    public static VoxyCompat instance;

    public static void initialize() {
        if (instance != null) return;
        instance = ModLoaded.VOXY ? new VoxyCompatImpl() : new VoxyCompat() {
        };
    }

    public void putProjMat(float[] values) {

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
