package com.qendolin.betterclouds;

import com.qendolin.betterclouds.platform.ModLoader;
import com.qendolin.betterclouds.platform.ModVersion;
import com.qendolin.betterclouds.util.NamedLogger;
import org.apache.logging.log4j.LogManager;

import java.nio.file.Path;

// This class is safe to be accessed pre-launch
public abstract class BetterCloudsStatic {
    public static final String MODID = "betterclouds";
    public static final boolean IS_DEV = ModLoader.isDevelopmentEnvironment();
    public static final boolean IS_CLIENT = ModLoader.isClientEnvironment();

    protected static NamedLogger logger = new NamedLogger(LogManager.getLogger("BetterClouds/PreLaunch"), !IS_DEV);
    protected static ModVersion version = null;
    protected static boolean initialized = false;
    protected static boolean initializedEarly = false;

    public static boolean isInitialized() {
        return initialized;
    }

    public static ModVersion getVersion() {
        return version;
    }

    public static NamedLogger getLogger() {
        return logger;
    }

    public static Path getDataDirectory() {
        return ModLoader.getGameDir().resolve("data/betterclouds");
    }
}
