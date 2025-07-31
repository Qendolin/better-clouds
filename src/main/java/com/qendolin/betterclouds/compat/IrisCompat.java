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

        boolean coderbotPackage = false;
        try {
            Class.forName("net.coderbot.iris.Iris");
            coderbotPackage = true;
        } catch (ClassNotFoundException ignored) {
        }

        boolean irisshadersPackage = false;
        try {
            Class.forName("net.irisshaders.iris.Iris");
            irisshadersPackage = true;
        } catch (ClassNotFoundException ignored) {
        }

        try {
            if(irisshadersPackage) {
                instance = new IrisCompatImpl();
            } else if(coderbotPackage) {
                //? if =1.20.1 {
                /*BetterCloudsStatic.getLogger().info("Using old net.coderbot.iris package");
                instance = new IrisCoderbotCompatImpl();
                *///?} else {
                throw new RuntimeException("The net.coderbot.iris package is only supported in 1.20.1, please use Iris 1.7.0 or later");
                //?}
            } else {
                throw new RuntimeException("Neither net.coderbot.iris nor net.irisshaders.iris packages found");
            }
        } catch (Throwable e) {
            BetterCloudsStatic.getLogger().error("Iris version not compatible", e);
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
