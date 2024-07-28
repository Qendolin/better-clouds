package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.platform.ModLoader;

public abstract class IrisCompat {
    private static IrisCompat instance;

    public static void initialize() {
        if (instance != null) return;

        boolean isLoaded = ModLoader.isModLoaded("iris");
        try {
            Class.forName("net.irisshaders.iris.Iris");
        } catch (ClassNotFoundException e) {
            isLoaded = false;
        }

        if (isLoaded) {
            instance = new IrisCompatImpl();
        } else {
            instance = new IrisCompatStub();
        }
    }

    public static IrisCompat instance() {
        return instance;
    }

    public abstract boolean isShadersEnabled();

    public abstract void bindFramebuffer();
}
