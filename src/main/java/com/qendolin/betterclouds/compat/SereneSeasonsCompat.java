package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.config.SereneSeasonsConfig;
import com.qendolin.betterclouds.platform.ModLoader;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class SereneSeasonsCompat {
    public static final boolean IS_LOADED = ModLoader.isModLoaded("sereneseasons");

    public static final Map<String, Function<SereneSeasonsConfig, Float>> SUB_SEASON_CLOUDINESS_VALUES = Map.ofEntries(
        Map.entry("early_spring", config -> config.earlySpringCloudiness),
        Map.entry("mid_spring", config -> config.midSpringCloudiness),
        Map.entry("late_spring", config -> config.lateSpringCloudiness),
        Map.entry("early_summer", config -> config.earlySummerCloudiness),
        Map.entry("mid_summer", config -> config.midSummerCloudiness),
        Map.entry("late_summer", config -> config.lateSummerCloudiness),
        Map.entry("early_autumn", config -> config.earlyAutumnCloudiness),
        Map.entry("mid_autumn", config -> config.midAutumnCloudiness),
        Map.entry("late_autumn", config -> config.lateAutumnCloudiness),
        Map.entry("early_winter", config -> config.earlyWinterCloudiness),
        Map.entry("mid_winter", config -> config.midWinterCloudiness),
        Map.entry("late_winter", config -> config.lateWinterCloudiness)
    );

    private static SereneSeasonsCompat instance;

    public static void initialize() {
        if (instance != null) return;

        Main.LOGGER.info("Initializing SereneSeasons compat");

        boolean isLoaded = IS_LOADED;
        try {
            Class.forName("sereneseasons.api.season.SeasonHelper");
        } catch (ClassNotFoundException e) {
            isLoaded = false;
        }

        instance = isLoaded ? new SereneSeasonsCompatImpl() : new Stub();
    }

    public static SereneSeasonsCompat instance() {
        return instance;
    }

    public abstract float getCloudinessFactor(World world);

    private static class Stub extends SereneSeasonsCompat {
        @Override
        public float getCloudinessFactor(World world) {
            return 1.0f;
        }
    }

}
