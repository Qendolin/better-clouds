package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.rendering.TextureWrapper;
import com.seibel.distanthorizons.api.DhApi;
import org.joml.Matrix4f;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class DhCompat {
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static DhCompat instance;

    public static void initialize() {
        if (initialized.getAndSet(true)) return;

        if (!ModLoaded.DISTANT_HORIZONS) {
            BetterCloudsStatic.getLogger().info("DistantHorizons: not loaded");
            instance = new DhCompat() {
            };
            return;
        }

        BetterCloudsStatic.getLogger().info("DistantHorizons: initializing compat");
        BetterCloudsStatic.getLogger().info("DistantHorizons API version is {}.{}.{}", DhApi.getApiMajorVersion(), DhApi.getApiMinorVersion(), DhApi.getApiPatchVersion());

        instance = new DhCompatImpl();
    }

    public static DhCompat instance() {
        return instance;
    }

    public boolean isReady() {
        return false;
    }

    public boolean isEnabled() {
        return false;
    }

    public Matrix4f getProjectionMatrix() {
        return null;
    }

    public Matrix4f getModelViewMatrix() {
        return null;
    }

    public Optional<Integer> getDepthTextureId() {
        return Optional.empty();
    }

    public TextureWrapper getDepthTexture() {
        return null;
    }

    public void disableLodClouds() {

    }

    public boolean isTextureCreateFlagSet() {
        return false;
    }

    public void resetTextureCreateFlag() {

    }

    public boolean isZNeg1To1() {
        return false;
    }
}
