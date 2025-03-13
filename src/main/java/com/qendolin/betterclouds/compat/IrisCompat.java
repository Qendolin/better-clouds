package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.platform.ModLoader;

public abstract class IrisCompat {

    public static final boolean IS_LOADED = ModLoader.isModLoaded("iris");

    private static IrisCompat instance;

    public static void initialize() {
        if (instance != null) return;

        Main.LOGGER.info("Initializing Iris compat");

        boolean isLoaded = IS_LOADED;
        try {
            Class.forName("net.irisshaders.iris.Iris");
        } catch (ClassNotFoundException e) {
            isLoaded = false;
        }

        instance = isLoaded ? new IrisCompatImpl() : new Stub();
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
