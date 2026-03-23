package com.qendolin.betterclouds.platform.fabric;

import com.qendolin.betterclouds.platform.ModLoader;
import com.qendolin.betterclouds.platform.ModVersion;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public final class ModLoaderImpl implements ModLoader.Backend {
    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public boolean isClientEnvironment() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public ModVersion getModVersion(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
            .map(container -> (ModVersion) new ModVersionImpl(container.getMetadata().getVersion()))
            .orElse(ModVersion.NONE);
    }
}
