package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.platform.ModLoader;
import com.seibel.distanthorizons.api.DhApi;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Optional;

public abstract class DistantHorizonsCompat {
    // Used when DH is enabled, but some other issue prevents it from working as intended
    // The matrix just maps everything to the near plane
    public static final Matrix4f NOOP_MATRIX = new Matrix4f(
        new Vector4f(0, 0, 0, 0),
        new Vector4f(0, 0, 0, 0),
        new Vector4f(0, 0, 0, 0),
        new Vector4f(0, 0, -1, 1)
    );

    private static DistantHorizonsCompat instance;
    private static boolean isLoaded = false;

    public static void initialize() {
        if (instance != null) return;

        Main.LOGGER.info("Initializing DistantHorizons compat");

        final boolean isLoaded = ModLoader.isModLoaded("distanthorizons");

        if(!isLoaded) {
            Main.LOGGER.info("DistantHorizons not loaded");
        }

        int apiVersion = 0;
        if(isLoaded) {
            try {
                Class.forName("com.seibel.distanthorizons.api.DhApi");
                apiVersion = DhApi.getApiMajorVersion();
                Main.LOGGER.info("DistantHorizons API version is {}.{}.{}", DhApi.getApiMajorVersion(), DhApi.getApiMinorVersion(), DhApi.getApiPatchVersion());
            } catch (ClassNotFoundException e) {
                Main.LOGGER.error("DistantHorizons version not compatible");
            }
        }

        if (apiVersion == 4) {
            Main.LOGGER.warn("Using EXPERIMENTAL DistantHorizons 4 compat. The game might crash!");
            instance = new DistantHorizons4CompatImpl();
        } else if (apiVersion == 3) {
            Main.LOGGER.info("Using DistantHorizons 3 compat");
            instance = new DistantHorizons3CompatImpl();
        } else if (apiVersion == 2) {
            Main.LOGGER.info("Using DistantHorizons 2 compat");
            instance = new DistantHorizons2CompatImpl();
        } else {
            if(isLoaded)
                Main.LOGGER.error("DistantHorizons version not compatible");

            instance = new Stub();
        }
        DistantHorizonsCompat.isLoaded = !(instance instanceof Stub);
    }

    public static boolean isLoaded() {
        return isLoaded;
    }


    public static DistantHorizonsCompat instance() {
        return instance;
    }


    public abstract boolean isReady();

    public abstract boolean isEnabled();

    public abstract Matrix4f getProjectionMatrix();

    public abstract Optional<Integer> getDepthTextureId();

    public abstract void disableLodClouds();

    private static class Stub extends DistantHorizonsCompat {
        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public Matrix4f getProjectionMatrix() {
            return NOOP_MATRIX;
        }

        @Override
        public Optional<Integer> getDepthTextureId() {
            return Optional.empty();
        }

        @Override
        public void disableLodClouds() {

        }
    }
}
