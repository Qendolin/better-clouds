package com.qendolin.betterclouds.compat;

import net.fabricmc.loader.api.FabricLoader;
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

    public static void initialize() {
        if(instance != null) return;

        boolean isLoaded = FabricLoader.getInstance().isModLoaded("distanthorizons");
        try {
            Class.forName("com.seibel.distanthorizons.api.DhApi");
        } catch (ClassNotFoundException e) {
            isLoaded = false;
        }

        if(isLoaded) {
            instance = new DistantHorizonsCompatImpl();
        } else {
            instance = new DistantHorizonsCompatStub();
        }
    }

    public static DistantHorizonsCompat instance() {
        return instance;
    }


    public abstract boolean isReady();

    public abstract boolean isEnabled();

    public abstract Matrix4f getProjectionMatrix();

    public abstract Optional<Integer> getDepthTextureId();
}
