package com.qendolin.betterclouds.compat;

import com.seibel.distanthorizons.api.DhApi;
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
        if (instance != null) return;

        boolean isLoaded = FabricLoader.getInstance().isModLoaded("distanthorizons");
        boolean isVersion2 = false;
        boolean isVersion3 = false;
        try {
            Class.forName("com.seibel.distanthorizons.api.DhApi");
            isVersion2 = DhApi.getModVersion().startsWith("2.");
            isVersion3 = DhApi.getModVersion().startsWith("3.");
        } catch (ClassNotFoundException e) {
            isLoaded = false;
        }

        if (isLoaded && isVersion3) {
            instance = new DistantHorizons3CompatImpl();
        } else if (isLoaded && isVersion2) {
            instance = new DistantHorizons2CompatImpl();
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

    public abstract void disableLodClouds();
}
