package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.config.FabricSeasonsConfig;
import com.qendolin.betterclouds.platform.ModLoader;
import net.minecraft.world.World;

import java.util.Map;
import java.util.function.Function;

public abstract class FabricSeasonsCompat {
    public static final boolean IS_LOADED = ModLoader.isModLoaded("seasons");

    public static final Map<String, Function<FabricSeasonsConfig, Float>> SEASON_CLOUDINESS_LOOKUP = Map.ofEntries(
        Map.entry("spring", config -> config.springCloudiness),
        Map.entry("summer", config -> config.summerCloudiness),
        Map.entry("fall", config -> config.fallCloudiness),
        Map.entry("winter", config -> config.winterCloudiness)
    );

    private static FabricSeasonsCompat instance;

    public static void initialize() {
        if (instance != null) return;

        Main.LOGGER.info("Initializing FabricSeasons compat");

        boolean isLoaded = IS_LOADED;
        try {
            Class.forName("io.github.lucaargolo.seasons.FabricSeasons");
        } catch (ClassNotFoundException e) {
            isLoaded = false;
        }

        instance = isLoaded ? new FabricSeasonsCompatImpl() : new FabricSeasonsCompat.Stub();
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
