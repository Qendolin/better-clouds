package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.Main;

public abstract class IrisCompat {

    private static IrisCompat instance;
    private static boolean isActive = false;

    public static void initialize() {
        if (instance != null) return;

        if (!ModLoaded.IRIS) {
            Main.LOGGER.info("Iris: not loaded");
            instance = new Stub();
            return;
        }

        Main.LOGGER.info("Iris: initializing compat");


        try {
            instance = new IrisCompatImpl();
        } catch (Throwable e) {
            Main.LOGGER.error("Iris version not compatible", e);
        }

        if (instance == null) {
            instance = new Stub();
        } else {
            IrisCompat.isActive = true;
        }
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
