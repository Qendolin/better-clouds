package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.config.FabricSeasonsConfig;
import net.minecraft.world.World;

import java.util.Map;
import java.util.function.Function;

public abstract class FabricSeasonsCompat {
    public static final Map<String, Function<FabricSeasonsConfig, Float>> SEASON_CLOUDINESS_LOOKUP = Map.ofEntries(
        Map.entry("spring", config -> config.springCloudiness),
        Map.entry("summer", config -> config.summerCloudiness),
        Map.entry("fall", config -> config.fallCloudiness),
        Map.entry("winter", config -> config.winterCloudiness)
    );

    private static FabricSeasonsCompat instance;
    private static boolean isActive = false;


    public static void initialize() {
        if (instance != null) return;

        if (!ModLoaded.FABRIC_SEASONS) {
            Main.LOGGER.info("FabricSeasons: not loaded");
            instance = new Stub();
            return;
        }

        Main.LOGGER.info("FabricSeasons: initializing compat");


        try {
            instance = new FabricSeasonsCompatImpl();
        } catch (Throwable e) {
            Main.LOGGER.error("FabricSeasons version not compatible", e);
        }

        if (instance == null) {
            instance = new Stub();
        } else {
            FabricSeasonsCompat.isActive = true;
        }
    }

    public static boolean isActive() {
        return isActive;
    }

    public static FabricSeasonsCompat instance() {
        return instance;
    }

    public abstract float getCloudinessFactor(World world);

    protected static class Stub extends FabricSeasonsCompat {

        @Override
        public float getCloudinessFactor(World world) {
            return 1.0f;
        }
    }
}
