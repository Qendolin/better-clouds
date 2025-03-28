package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.config.SereneSeasonsConfig;
import net.minecraft.world.World;

import java.util.Map;
import java.util.function.Function;

public abstract class SereneSeasonsCompat {
    public static final Map<String, Function<SereneSeasonsConfig, Float>> SUB_SEASON_CLOUDINESS_LOOKUP = Map.ofEntries(
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
    private static boolean isActive = false;


    public static void initialize() {
        if (instance != null) return;

        Main.LOGGER.info("Initializing SereneSeasons compat");

        boolean active = ModLoaded.SERENE_SEASONS;
        if(!active) {
            Main.LOGGER.info("SereneSeasons not loaded");
        }

        if(active) {
            try {
                Class.forName("sereneseasons.api.season.SeasonHelper");
            } catch (ClassNotFoundException e) {
                active = false;
                Main.LOGGER.error("SereneSeasons version not compatible");
            }
        }

        instance = active ? new SereneSeasonsCompatImpl() : new Stub();
        SereneSeasonsCompat.isActive = active;
    }

    public static boolean isActive() {
        return isActive;
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
