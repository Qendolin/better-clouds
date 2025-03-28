package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.Main;

public abstract class IrisCompat {

    private static IrisCompat instance;
    private static boolean isActive = false;

    public static void initialize() {
        if (instance != null) return;

        Main.LOGGER.info("Initializing Iris compat");

        boolean active = ModLoaded.IRIS;
        if(!active) {
            Main.LOGGER.info("Iris not loaded");
        }

        if(active) {
            try {
                Class.forName("net.irisshaders.iris.Iris");
            } catch (ClassNotFoundException e) {
                active = false;
                Main.LOGGER.error("Iris version not compatible");
            }
        }

        instance = active ? new IrisCompatImpl() : new Stub();
        IrisCompat.isActive = active;
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
