package com.qendolin.betterclouds.platform.fabric;

import com.qendolin.betterclouds.platform.ModLoader;
import com.qendolin.betterclouds.platform.ModVersion;

import java.nio.file.Path;

public final class ModLoaderImpl implements ModLoader.Backend {
    @Override
    public Path getConfigDir() {
        return Path.of(".");
    }

    @Override
    public Path getGameDir() {
        return Path.of(".");
    }

    @Override
    public boolean isModLoaded(String modId) {
        return false;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return false;
    }

    @Override
    public boolean isClientEnvironment() {
        return false;
    }

    @Override
    public ModVersion getModVersion(String modId) {
        return ModVersion.NONE;
    }
}
