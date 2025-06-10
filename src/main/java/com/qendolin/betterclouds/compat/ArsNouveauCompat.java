package com.qendolin.betterclouds.compat;

public class ArsNouveauCompat {
    public static final ThreadLocal<Boolean> IS_SKY_TEXTURE_CLOUDS_RENDERING = ThreadLocal.withInitial(() -> false);

    public static boolean isSkyTextureCloudsRendering() {
        if (!ModLoaded.ARS_NOUVEAU) {
            return false;
        }
        return IS_SKY_TEXTURE_CLOUDS_RENDERING.get();
    }
}
