package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.BetterCloudsStatic;
import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.common.render.blaze.wrappers.texture.BlazeTextureWrapper;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class DistantHorizonsCompat {
    // Used when DH is enabled, but some other issue prevents it from working as intended
    // The matrix just maps everything to the near plane
    public static final Matrix4f NOOP_MATRIX = new Matrix4f(
            new Vector4f(0, 0, 0, 0),
            new Vector4f(0, 0, 0, 0),
            new Vector4f(0, 0, 0, 0),
            new Vector4f(0, 0, -1, 1)
    );
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static DistantHorizonsCompat instance;
    private static boolean isActive = false;

    public static void initialize() {
        if (initialized.getAndSet(true)) return;

        if (!ModLoaded.DISTANT_HORIZONS) {
            BetterCloudsStatic.getLogger().info("DistantHorizons: not loaded");
            instance = new Stub();
            return;
        }

        BetterCloudsStatic.getLogger().info("DistantHorizons: initializing compat");

        int apiVersion = 0;
        try {
            Class.forName("com.seibel.distanthorizons.api.DhApi");
            apiVersion = DhApi.getApiMajorVersion();
            BetterCloudsStatic.getLogger().info("DistantHorizons API version is {}.{}.{}", DhApi.getApiMajorVersion(), DhApi.getApiMinorVersion(), DhApi.getApiPatchVersion());
        } catch (ClassNotFoundException ignored) {
        }

        try {
            switch (apiVersion) {
                case 3 -> {
                    BetterCloudsStatic.getLogger().info("Using DistantHorizons 3 compat");
                    instance = new DistantHorizons3CompatImpl();
                }
                case 2 -> {
                    BetterCloudsStatic.getLogger().info("Using DistantHorizons 2 compat");
                    instance = new DistantHorizons2CompatImpl();
                }
                default -> {
                    BetterCloudsStatic.getLogger().info("Using Distant Horizons 3 compat for version " + apiVersion + ", hopefully it still works! ;-;");
                    instance = new DistantHorizons3CompatImpl();
                }
            }
        } catch (Throwable e) {
            BetterCloudsStatic.getLogger().error("DistantHorizons version not compatible", e);
        }

        if (instance == null) {
            instance = new Stub();
        } else {
            DistantHorizonsCompat.isActive = true;
        }
    }

    public static boolean isActive() {
        return isActive;
    }

    public static DistantHorizonsCompat instance() {
        return instance;
    }

    public abstract boolean isReady();

    public abstract boolean isEnabled();

    public abstract Matrix4f getProjectionMatrix();

    public abstract Optional<Integer> getDepthTextureId();

    public abstract BlazeTextureWrapper getDepthTexture();

    public abstract void disableLodClouds();

    public abstract boolean isTextureCreateFlagSet();

    public abstract void resetTextureCreateFlag();

    private static class Stub extends DistantHorizonsCompat {

        static {
            DistantHorizonsCompat.instance = new Stub();
        }

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
        public BlazeTextureWrapper getDepthTexture() {
            return null;
        }

        @Override
        public void disableLodClouds() {

        }

        @Override
        public boolean isTextureCreateFlagSet() {
            return false;
        }

        @Override
        public void resetTextureCreateFlag() {

        }
    }
}
