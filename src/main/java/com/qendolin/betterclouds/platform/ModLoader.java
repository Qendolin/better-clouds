package com.qendolin.betterclouds.platform;

import java.nio.file.Path;
import java.util.Optional;

//? if fabric {
/*import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.ModContainer;
import com.qendolin.betterclouds.platform.fabric.ModVersionImpl;

public final class ModLoader {
    public static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static boolean isClientEnvironment() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static ModVersion getModVersion(String modId) {
        ModList modList = ModList.get();
        if(modList != null) {
            Main.LOGGER.warn("getModVersion called before the mod list is initialized.");
            return ModVersion.NONE;
        }
        Optional<ModContainer> mod = = FabricLoader.getInstance().getModContainer(MODID).orElse(null);
        if(mod.isEmpty()) return ModVersion.NONE;
        return new ModVersionImpl(mod.getMetadata().getVersion());
    }
}*///?} elif neoforge {
import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.platform.neoforge.ModVersionImpl;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;

public final class ModLoader {
    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    public static boolean isModLoaded(String modId) {
        ModList modList = ModList.get();
        if(modList != null) {
            return modList.isLoaded(modId);
        }
        LoadingModList loadingModList = LoadingModList.get();
        if(loadingModList != null) {
            return loadingModList.getModFileById(modId) != null;
        }
        return false;
    }

    public static boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    public static boolean isClientEnvironment() {
        return FMLLoader.getDist().isClient();
    }

    public static ModVersion getModVersion(String modId) {
        ModList modList = ModList.get();
        if(modList == null) {
            Main.LOGGER.warn("getModVersion called before the mod list is initialized.");
            return ModVersion.NONE;
        }
        Optional<? extends ModContainer> mod = modList.getModContainerById(modId);
        if(mod.isEmpty()) return ModVersion.NONE;
        return new ModVersionImpl(mod.get().getModInfo().getVersion());
    }
}
//?}