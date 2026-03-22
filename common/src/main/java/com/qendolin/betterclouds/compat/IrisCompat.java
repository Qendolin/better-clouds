package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.BetterCloudsStatic;

public abstract class IrisCompat {

    private static IrisCompat instance;
    private static boolean isActive = false;

    public static void initialize() {
        if (instance != null) return;

        if (!ModLoaded.IRIS) {
            BetterCloudsStatic.getLogger().info("Iris: not loaded");
            instance = new Stub();
            return;
        }

        BetterCloudsStatic.getLogger().info("Iris: initializing compat");

        BetterCloudsStatic.getLogger().warn("Iris compat is temporarily disabled on Minecraft 26.1 until an official-mappings build is available");
        instance = new Stub();
    }

    public static boolean isActive() {
        return isActive;
    }

    public static IrisCompat instance() {
        return instance;
    }

    public abstract boolean isShadersEnabled();

    public abstract boolean isFrustumCullingDisabled();

    public abstract void bindFramebuffer();

    private static class Stub extends IrisCompat {
        @Override
        public boolean isShadersEnabled() {
            return false;
        }

        @Override
        public boolean isFrustumCullingDisabled() {
            return false;
        }

        @Override
        public void bindFramebuffer() {

        }
    }
}
