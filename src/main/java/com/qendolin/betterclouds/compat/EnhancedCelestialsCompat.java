package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.Main;
import net.minecraft.world.World;
import org.joml.Vector3f;

public abstract class EnhancedCelestialsCompat {

    private static EnhancedCelestialsCompat instance;
    private static boolean isActive = false;

    public static void initialize() {
        if (instance != null) return;

        if (!ModLoaded.ENHANCED_CELESTIALS) {
            Main.LOGGER.info("EnhancedCelestials: not loaded");
            instance = new Stub();
            return;
        }

        Main.LOGGER.info("EnhancedCelestials: initializing compat");

        int version = 0;
        boolean v1devPackage = true;
        try {
            Class.forName("dev.corgitaco.enhancedcelestials.lunarevent.EnhancedCelestialsLunarForecastWorldData");
            version = 2;
        } catch (ClassNotFoundException ignored) {
        }

        if (version == 0) {
            try {
                Class.forName("corgitaco.enhancedcelestials.EnhancedCelestialsWorldData");
                version = 1;
                v1devPackage = false;
            } catch (ClassNotFoundException ignored) {
            }
        }

        if (version == 0) {
            try {
                Class.forName("dev.corgitaco.enhancedcelestials.EnhancedCelestialsWorldData");
                version = 1;
            } catch (ClassNotFoundException ignored) {
            }
        }

        try {
            if (version == 1) {
                Main.LOGGER.info("Using EnhancedCelestials 1 compat");
                instance = new EnhancedCelestials1CompatImpl(v1devPackage);
            } else if (version == 2) {
                Main.LOGGER.info("Using EnhancedCelestials 2 compat");
                instance = new EnhancedCelestials2CompatImpl();
            } else {
                Main.LOGGER.error("EnhancedCelestials version not compatible");
            }
        } catch (Throwable e) {
            Main.LOGGER.error("EnhancedCelestials version not compatible", e);
        }

        if (instance == null) {
            instance = new Stub();
        } else {
            EnhancedCelestialsCompat.isActive = true;
        }
    }

    public static boolean isActive() {
        return isActive;
    }

    public static EnhancedCelestialsCompat instance() {
        return instance;
    }

    public abstract Vector3f getEventTint(World world);

    public abstract boolean isEventActive(World world);

    public abstract float getMoonSize(World world);

    private static class Stub extends EnhancedCelestialsCompat {
        @Override
        public Vector3f getEventTint(World world) {
            return new Vector3f(1.0f, 1.0f, 1.0f);
        }

        @Override
        public boolean isEventActive(World world) {
            return false;
        }

        @Override
        public float getMoonSize(World world) {
            return 1;
        }
    }

}
