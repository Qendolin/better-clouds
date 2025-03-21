package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.platform.ModLoader;
import net.minecraft.world.World;
import org.joml.Vector3f;

public abstract class EnhancedCelestialsCompat {

    public static final boolean IS_LOADED = ModLoader.isModLoaded("enhancedcelestials");

    private static EnhancedCelestialsCompat instance;

    public static void initialize() {
        if (instance != null) return;

        Main.LOGGER.info("Initializing EnhancedCelestials compat");

        instance = IS_LOADED ? new EnhancedCelestialsCompatImpl() : new Stub();
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
