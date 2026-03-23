package com.qendolin.betterclouds.platform;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

public final class ModLoader {

    private static final Backend BACKEND = loadBackend();

    private ModLoader() {
    }

    public static Path getConfigDir() {
        return BACKEND.getConfigDir();
    }

    public static Path getGameDir() {
        return BACKEND.getGameDir();
    }

    public static boolean isModLoaded(String modId) {
        return BACKEND.isModLoaded(modId);
    }

    public static boolean isDevelopmentEnvironment() {
        return BACKEND.isDevelopmentEnvironment();
    }

    public static boolean isClientEnvironment() {
        return BACKEND.isClientEnvironment();
    }

    public static ModVersion getModVersion(String modId) {
        return BACKEND.getModVersion(modId);
    }

    private static Backend loadBackend() {
        return instantiate("com.qendolin.betterclouds.platform.fabric.ModLoaderImpl")
            .or(() -> instantiate("com.qendolin.betterclouds.platform.neoforge.ModLoaderImpl"))
            .orElseThrow(() -> new IllegalStateException("No platform ModLoader implementation found"));
    }

    private static java.util.Optional<Backend> instantiate(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return java.util.Optional.of((Backend) clazz.getDeclaredConstructor().newInstance());
        } catch (ClassNotFoundException e) {
            return java.util.Optional.empty();
        } catch (ClassCastException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException("Failed to initialize platform ModLoader backend: " + className, e);
        }
    }

    public interface Backend {
        Path getConfigDir();

        Path getGameDir();

        boolean isModLoaded(String modId);

        boolean isDevelopmentEnvironment();

        boolean isClientEnvironment();

        ModVersion getModVersion(String modId);
    }
}
