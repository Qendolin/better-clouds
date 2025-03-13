package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.platform.ModLoader;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public abstract class SereneSeasonsCompat {
    public static final boolean IS_LOADED = ModLoader.isModLoaded("sereneseasons");

    public static final Map<String, Float> SUB_SEASON_CLOUDINESS_VALUES = new HashMap<>();

    // FIXME: These static values are temporary
    static {
        SUB_SEASON_CLOUDINESS_VALUES.put("early_spring", 1.2f);
        SUB_SEASON_CLOUDINESS_VALUES.put("mid_spring", 1.0f);
        SUB_SEASON_CLOUDINESS_VALUES.put("late_spring", 0.9f);
        SUB_SEASON_CLOUDINESS_VALUES.put("early_summer", 0.8f);
        SUB_SEASON_CLOUDINESS_VALUES.put("mid_summer", 0.7f);
        SUB_SEASON_CLOUDINESS_VALUES.put("late_summer", 0.8f);
        SUB_SEASON_CLOUDINESS_VALUES.put("early_autumn", 0.9f);
        SUB_SEASON_CLOUDINESS_VALUES.put("mid_autumn", 1.0f);
        SUB_SEASON_CLOUDINESS_VALUES.put("late_autumn", 1.2f);
        SUB_SEASON_CLOUDINESS_VALUES.put("early_winter", 1.3f);
        SUB_SEASON_CLOUDINESS_VALUES.put("mid_winter", 1.4f);
        SUB_SEASON_CLOUDINESS_VALUES.put("late_winter", 1.3f);
    }

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
