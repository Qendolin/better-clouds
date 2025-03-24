package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.platform.ModLoader;

public abstract class IrisCompat {

    private static IrisCompat instance;
    private static boolean isLoaded = false;

    public static void initialize() {
        if (instance != null) return;

        Main.LOGGER.info("Initializing Iris compat");

        boolean isLoaded = ModLoader.isModLoaded("iris");
        if(!isLoaded) {
            Main.LOGGER.info("Iris not loaded");
        }

        if(isLoaded) {
            try {
                Class.forName("net.irisshaders.iris.Iris");
            } catch (ClassNotFoundException e) {
                isLoaded = false;
                Main.LOGGER.error("Iris version not compatible");
            }
        }

        instance = isLoaded ? new IrisCompatImpl() : new Stub();
        IrisCompat.isLoaded = isLoaded;
    }

    public static boolean isLoaded() {
        return isLoaded;
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
