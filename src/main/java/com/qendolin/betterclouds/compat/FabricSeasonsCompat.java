package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.config.FabricSeasonsConfig;
import com.qendolin.betterclouds.platform.ModLoader;
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
    private static boolean isLoaded = false;


    public static void initialize() {
        if (instance != null) return;

        Main.LOGGER.info("Initializing FabricSeasons compat");

        boolean isLoaded = ModLoader.isModLoaded("seasons");
        if(!isLoaded) {
            Main.LOGGER.info("FabricSeasons not loaded");
        }

        if(isLoaded) {
            try {
                Class.forName("io.github.lucaargolo.seasons.FabricSeasons");
            } catch (ClassNotFoundException e) {
                isLoaded = false;
                Main.LOGGER.error("FabricSeasons version not compatible");
            }
        }

        instance = isLoaded ? new FabricSeasonsCompatImpl() : new FabricSeasonsCompat.Stub();
        FabricSeasonsCompat.isLoaded = isLoaded;
    }

    public static boolean isLoaded() {
        return isLoaded;
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
