package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.BetterCloudsStatic;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.world.level.Level;

public abstract class EnhancedCelestialsCompat {

    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static EnhancedCelestialsCompat instance;
    private static boolean isActive = false;

    public static void initialize() {
        if (initialized.getAndSet(true)) return;

        if (!ModLoaded.ENHANCED_CELESTIALS) {
            BetterCloudsStatic.getLogger().info("EnhancedCelestials: not loaded");
            instance = new Stub();
            return;
        }

        BetterCloudsStatic.getLogger().info("EnhancedCelestials: initializing compat");

        BetterCloudsStatic.getLogger().warn("EnhancedCelestials compat is temporarily disabled on Minecraft 26.1 until an official-mappings build is available");
        instance = new Stub();
    }

    public static boolean isActive() {
        return isActive;
    }

    public static EnhancedCelestialsCompat instance() {
        return instance;
    }

    public abstract Vector3f getEventTint(Level world);

    public abstract boolean isEventActive(Level world);

    public abstract float getMoonSize(Level world);

    private static class Stub extends EnhancedCelestialsCompat {
        static {
            EnhancedCelestialsCompat.instance = new Stub();
        }
        
        @Override
        public Vector3f getEventTint(Level world) {
            return new Vector3f(1.0f, 1.0f, 1.0f);
        }

        @Override
        public boolean isEventActive(Level world) {
            return false;
        }

        @Override
        public float getMoonSize(Level world) {
            return 1;
        }
    }

}
