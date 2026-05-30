package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.config.compat.FabricSeasonsConfig;
import com.qendolin.betterclouds.platform.ModLoader;
import com.qendolin.betterclouds.platform.ModVersion;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public abstract class FabricSeasonsCompat {
    public static final Map<String, Function<FabricSeasonsConfig, Float>> SEASON_CLOUDINESS_LOOKUP = Map.ofEntries(
            Map.entry("spring", config -> config.springCloudiness),
            Map.entry("summer", config -> config.summerCloudiness),
            Map.entry("fall", config -> config.fallCloudiness),
            Map.entry("winter", config -> config.winterCloudiness)
    );

    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final ModVersion.SemVer MINIMUM_VERSION = new ModVersion.SemVer(2, 4, 0);
    private static final boolean isActive = false;
    private static FabricSeasonsCompat instance;

    public static void initialize() {
        if (initialized.getAndSet(true)) return;

        if (!ModLoaded.FABRIC_SEASONS) {
            BetterCloudsStatic.getLogger().info("FabricSeasons: not loaded");
            instance = new Stub();
            return;
        }

        BetterCloudsStatic.getLogger().info("FabricSeasons: initializing compat");

        if (!ModLoader.getModVersion("seasons").asSemVer().map(version -> version.compareTo(MINIMUM_VERSION) >= 0).orElse(false)) {
            BetterCloudsStatic.getLogger().error("FabricSeasons version not compatible, minimum required is {}", MINIMUM_VERSION);
            instance = new Stub();
            return;
        }

        BetterCloudsStatic.getLogger().warn("FabricSeasons compat is temporarily disabled on Minecraft 26.2 until an official-mappings build is available");
        instance = new Stub();
    }

    public static boolean isActive() {
        return isActive;
    }

    public static FabricSeasonsCompat instance() {
        return instance;
    }

    public abstract float getCloudinessFactor(Level world);

    protected static class Stub extends FabricSeasonsCompat {

        static {
            FabricSeasonsCompat.instance = new Stub();
        }

        @Override
        public float getCloudinessFactor(Level world) {
            return 1.0f;
        }
    }
}
