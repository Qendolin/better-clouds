package com.qendolin.betterclouds.platform;

import com.qendolin.betterclouds.platform.neoforge.ModVersionImpl;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public final class ModLoader {
    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    public static boolean isModLoaded(String modId) {
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

    public static boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }

    public static boolean isClientEnvironment() {
        return FMLLoader.getCurrent().getDist().isClient();
    }

    public static ModVersion getModVersion(String modId) {
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
