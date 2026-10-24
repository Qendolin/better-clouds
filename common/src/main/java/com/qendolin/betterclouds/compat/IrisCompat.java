package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.BetterCloudsStatic;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;

public abstract class IrisCompat {

    private static IrisCompat instance;
    private static boolean isActive = false;

    public static void initialize() {
        if (instance != null) return;

        if (!ModLoaded.IRIS) {
            BetterCloudsStatic.getLogger().info("Iris: not loaded");
            instance = new IrisCompat() {
            };
            return;
        }

        BetterCloudsStatic.getLogger().info("Iris: initializing compat");

        boolean irisshadersPackage = false;
        try {
            Class.forName("net.irisshaders.iris.Iris");
            irisshadersPackage = true;
        } catch (ClassNotFoundException ignored) {
        }

        try {
            if (irisshadersPackage) {
                instance = new IrisCompatImpl();
            } else {
                throw new RuntimeException("net.irisshaders.iris package not found");
            }
        } catch (Throwable e) {
            BetterCloudsStatic.getLogger().error("Iris version not compatible", e);
        }

        if (instance == null) {
            instance = new IrisCompat() {
            };
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

    public boolean isShadersEnabled() {
        return false;
    }

    public boolean isFrustumCullingDisabled() {
        return false;
    }

    public WorldRenderingPipeline getPipeline() {
        return null;
    }

    public void bindFramebuffer() {
    }
}
