package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.config.compat.SereneSeasonsConfig;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
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

    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static SereneSeasonsCompat instance;
    private static boolean isActive = false;


    public static void initialize() {
        if (initialized.getAndSet(true)) return;

        if (!ModLoaded.SERENE_SEASONS) {
            BetterCloudsStatic.getLogger().info("SereneSeasons: not loaded");
            instance = new Stub();
            return;
        }

        BetterCloudsStatic.getLogger().info("SereneSeasons: initializing compat");


        try {
            instance = new SereneSeasonsCompatImpl();
        } catch (Throwable e) {
            BetterCloudsStatic.getLogger().error("SereneSeasons version not compatible", e);
        }

        if (instance == null) {
            instance = new Stub();
        } else {
            SereneSeasonsCompat.isActive = true;
        }
    }

    public static boolean isActive() {
        return isActive;
    }

    public static SereneSeasonsCompat instance() {
        return instance;
    }

    public abstract float getCloudinessFactor(Level world);

    private static class Stub extends SereneSeasonsCompat {

        static {
            SereneSeasonsCompat.instance = new Stub();
        }

        @Override
        public float getCloudinessFactor(Level world) {
            return 1.0f;
        }
    }

}
