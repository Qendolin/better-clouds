package com.qendolin.betterclouds.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qendolin.betterclouds.BetterCloudsStatic;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

//? if fabric {
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
//?}

//? if <1.21.3
//import net.minecraft.util.profiler.Profiler;

public class PresetLoader<T extends AbstractPresetConfig>
//? if fabric {
implements IdentifiableResourceReloadListener {
//?} else {
/*implements ResourceReloader {
*///?}
    public static final PresetLoader<ShaderPresetConfig> SHADER = new PresetLoader<>(
        "shader_presets",
        "betterclouds/shader_presets.json",
        new TypeToken<Map<String, ShaderPresetConfig>>() {
        }.getType()
    );
    public static final PresetLoader<NoisePresetConfig> NOISE = new PresetLoader<>(
        "noise_presets",
        "betterclouds/noise_presets.json",
        new TypeToken<Map<String, NoisePresetConfig>>() {
        }.getType()
    );
    public static final List<PresetLoader<?>> ALL_PRESETS = List.of(SHADER, NOISE);

    private static final Gson GSON = new GsonBuilder()
        .setLenient()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(ShaderPresetConfig.class, ShaderPresetConfig.INSTANCE_CREATOR)
        .registerTypeAdapter(NoisePresetConfig.class, NoisePresetConfig.INSTANCE_CREATOR)
        .create();

    public final Identifier id;
    public final Identifier resourceId;
    private final Type gsonType;
    private Map<String, T> presets = null;

    public PresetLoader(String id, String resourceId, Type gsonType) {
        this.id = Identifier.of(BetterCloudsStatic.MODID, id);
        this.resourceId = Identifier.of(BetterCloudsStatic.MODID, resourceId);
        this.gsonType = gsonType;
    }

    public Map<String, T> presets() {
        if (presets == null) return Map.of();
        return ImmutableMap.copyOf(presets);
    }

    //? if fabric {
    @Override
    public Identifier getFabricId() {
        return id;
    }
    //?}

    @Override
    //? if >=1.21.9 {
    public CompletableFuture<Void> reload(Store store, Executor loadExecutor, Synchronizer helper, Executor applyExecutor) {
        ResourceManager manager = store.getResourceManager();
    //?} else if >=1.21.3 {
    /*public CompletableFuture<Void> reload(ResourceReloader.Synchronizer helper, ResourceManager manager, Executor loadExecutor, Executor applyExecutor) {
    *///?} else {
    /*public CompletableFuture<Void> reload(ResourceReloader.Synchronizer helper, ResourceManager manager, Profiler loadProfiler, Profiler applyProfiler, Executor loadExecutor, Executor applyExecutor) {
    *///?}
        return load(manager, loadExecutor).thenCompose(helper::whenPrepared).thenCompose(
            data -> apply(data, manager, applyExecutor)
        );
    }

    public CompletableFuture<Map<String, T>> load(ResourceManager manager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, T> mergedPresets = new HashMap<>();
            for (Resource resource : manager.getAllResources(resourceId)) {
                try (BufferedReader reader = resource.getReader()) {
                    Map<String, T> loadedPresets = GSON.fromJson(reader, gsonType);
                    if (loadedPresets == null) continue;
                    mergedPresets.putAll(loadedPresets);
                } catch (Exception exception) {
                    //? if >=1.20.6 {
                    BetterCloudsStatic.getLogger().warn("Failed to parse preset resource {} in pack '{}' ({})", resourceId, resource.getPack().getInfo().title(), resource.getPack().getId(), exception);
                    //?} else
                    //BetterCloudsStatic.getLogger().warn("Failed to parse preset resource {} in pack '{}'", resourceId, resource.getPack().getName(), exception);
                }
            }

            mergedPresets.values().removeAll(Collections.singleton(null));

            for (Map.Entry<String, T> entry : mergedPresets.entrySet()) {
                entry.getValue().editable = false;
                entry.getValue().key = entry.getKey();
            }

            return mergedPresets;
        }, executor);
    }

    public CompletableFuture<Void> apply(Map<String, T> data, ResourceManager manager, Executor executor) {
        presets = data;
        if (ConfigManager.instance() != null) {
            ConfigManager.instance().loadDefaultPresets();
        }
        return CompletableFuture.completedFuture(null);
    }
}
