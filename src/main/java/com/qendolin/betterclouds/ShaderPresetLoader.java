package com.qendolin.betterclouds;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ShaderPresetLoader implements SimpleResourceReloadListener<Map<String, Config.ShaderConfigPreset>> {
    private static final Gson GSON = new GsonBuilder()
        .setLenient()
        .registerTypeAdapter(Config.ShaderConfigPreset.class, (InstanceCreator<Config.ShaderConfigPreset>) type -> new Config.ShaderConfigPreset(""))
        .create();
    public static final Identifier ID = new Identifier(Main.MODID, "shader_presets");
    public static final Identifier RESOURCE_ID = new Identifier(Main.MODID, "betterclouds/shader_presets.json");
    public static final ShaderPresetLoader INSTANCE = new ShaderPresetLoader();

    private Map<String, Config.ShaderConfigPreset> presets = null;

    public Map<String, Config.ShaderConfigPreset> presets() {
        if(presets == null) return Map.of();
        return ImmutableMap.copyOf(presets);
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public CompletableFuture<Map<String, Config.ShaderConfigPreset>> load(ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Config.ShaderConfigPreset> mergedPresets = new HashMap<>();
            Type mapType = new TypeToken<Map<String, Config.ShaderConfigPreset>>() {}.getType();
            for (Resource resource : manager.getAllResources(RESOURCE_ID)) {
                try (BufferedReader reader = resource.getReader()) {
                    Map<String, Config.ShaderConfigPreset> presets = GSON.fromJson(reader, mapType);
                    if(presets == null) continue;
                    mergedPresets.putAll(presets);
                }
                catch (Exception exception) {
                    Main.LOGGER.warn("Failed to parse shader presets {} in pack {}", RESOURCE_ID, resource.getResourcePackName(), exception);
                }
            }

            mergedPresets.values().removeAll(Collections.singleton(null));

            for (Map.Entry<String, Config.ShaderConfigPreset> entry : mergedPresets.entrySet()) {
                entry.getValue().editable = false;
                entry.getValue().key = entry.getKey();
            }

            return mergedPresets;
        });
    }

    @Override
    public CompletableFuture<Void> apply(Map<String, Config.ShaderConfigPreset> data, ResourceManager manager, Profiler profiler, Executor executor) {
        presets = data;
        if(Main.getConfig() != null) {
            Main.getConfig().loadDefaultPresets();
        }
        return CompletableFuture.completedFuture(null);
    }
}
