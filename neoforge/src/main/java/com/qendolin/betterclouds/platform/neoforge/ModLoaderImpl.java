package com.qendolin.betterclouds.platform.neoforge;

import com.qendolin.betterclouds.platform.ModLoader;
import com.qendolin.betterclouds.platform.ModVersion;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public final class ModLoaderImpl implements ModLoader.Backend {
    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public boolean isModLoaded(String modId) {
        ModList modList = ModList.get();
        if (modList != null) {
            return modList.isLoaded(modId);
        }
        LoadingModList loadingModList = FMLLoader.getCurrent().getLoadingModList();
        if (loadingModList != null) {
            return loadingModList.getModFileById(modId) != null;
        }
        return false;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
    public boolean isClientEnvironment() {
        return FMLLoader.getCurrent().getDist().isClient();
    }

    @Override
    public ModVersion getModVersion(String modId) {
        ModList modList = ModList.get();
        if (modList == null) {
            LoadingModList loadingModList = FMLLoader.getCurrent().getLoadingModList();
            for (ModInfo mod : loadingModList.getMods()) {
                if (Objects.equals(mod.getModId(), modId)) {
                    return new ModVersionImpl(mod.getVersion());
                }
            }
            return ModVersion.NONE;
        }
        Optional<? extends ModContainer> mod = modList.getModContainerById(modId);
        if (mod.isEmpty()) return ModVersion.NONE;
        return new ModVersionImpl(mod.get().getModInfo().getVersion());
    }
}
