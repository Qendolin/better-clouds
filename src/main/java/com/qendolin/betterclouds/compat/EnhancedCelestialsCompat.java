package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.platform.ModLoader;
import dev.corgitaco.enhancedcelestials.EnhancedCelestials;
import net.minecraft.world.World;
import org.joml.Vector3f;

public abstract class EnhancedCelestialsCompat {

    private static EnhancedCelestialsCompat instance;
    private static boolean isLoaded = false;

    public static void initialize() {
        if (instance != null) return;

        Main.LOGGER.info("Initializing EnhancedCelestials compat");

        final boolean isLoaded = ModLoader.isModLoaded("enhancedcelestials");;
        if(!isLoaded) {
            Main.LOGGER.info("EnhancedCelestials not loaded");
        }

        int version = 0;
        boolean v1devPackage = true;
        if(isLoaded) {
            try {
                Class.forName("dev.corgitaco.enhancedcelestials.lunarevent.EnhancedCelestialsLunarForecastWorldData");
                version = 2;
            } catch (ClassNotFoundException ignored) {}

            if(version == 0) {
                try {
                    Class.forName("corgitaco.enhancedcelestials.EnhancedCelestialsWorldData");
                    version = 1;
                    v1devPackage = false;
                } catch (ClassNotFoundException ignored) {}
            }

            if(version == 0) {
                try {
                    Class.forName("dev.corgitaco.enhancedcelestials.EnhancedCelestialsWorldData");
                    version = 1;
                } catch (ClassNotFoundException ignored) {}
            }
        }

        if(version == 1) {
            Main.LOGGER.info("Using EnhancedCelestials 1 compat");
            instance = new EnhancedCelestials1CompatImpl(v1devPackage);
        } else if(version == 2) {
            Main.LOGGER.info("Using EnhancedCelestials 2 compat");
            instance = new EnhancedCelestials2CompatImpl();
        } else {
            if(isLoaded)
                Main.LOGGER.error("EnhancedCelestials version not compatible");

            instance = new Stub();
        }
        EnhancedCelestialsCompat.isLoaded = !(instance instanceof Stub);

    }

    public static boolean isLoaded() {
        return isLoaded;
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
