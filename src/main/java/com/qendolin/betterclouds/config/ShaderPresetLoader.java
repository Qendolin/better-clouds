package com.qendolin.betterclouds.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qendolin.betterclouds.Main;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

//? if fabric {
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
//?}

//? if <1.21.3
/*import net.minecraft.util.profiler.Profiler;*/

public class ShaderPresetLoader
//? if fabric {
implements IdentifiableResourceReloadListener {
//?} else {
/*implements ResourceReloader {
*///?}
    private static final Gson GSON = new GsonBuilder()
        .setLenient()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(ShaderPresetConfig.class, ShaderPresetConfig.INSTANCE_CREATOR)
        .create();
    public static final Identifier ID = Identifier.of(Main.MODID, "shader_presets");
    public static final Identifier RESOURCE_ID = Identifier.of(Main.MODID, "betterclouds/shader_presets.json");
    public static final ShaderPresetLoader INSTANCE = new ShaderPresetLoader();

    private Map<String, ShaderPresetConfig> presets = null;

    public Map<String, ShaderPresetConfig> presets() {
        if (presets == null) return Map.of();
        return ImmutableMap.copyOf(presets);
    }

    //? if fabric {
    @Override
    public Identifier getFabricId() {
        return ID;
    }
    //?}

    @Override
    public CompletableFuture<Void> reload(ResourceReloader.Synchronizer helper, ResourceManager manager, /*? if <1.21.3 {*/ /*Profiler loadProfiler, Profiler applyProfiler, *//*?}*/ Executor loadExecutor, Executor applyExecutor) {
        return load(manager, loadExecutor).thenCompose(helper::whenPrepared).thenCompose(
            (o) -> apply(o, manager, applyExecutor)
        );
    }

    public CompletableFuture<Map<String, ShaderPresetConfig>> load(ResourceManager manager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, ShaderPresetConfig> mergedPresets = new HashMap<>();
            Type mapType = new TypeToken<Map<String, ShaderPresetConfig>>() {
            }.getType();
            for (Resource resource : manager.getAllResources(RESOURCE_ID)) {
                try (BufferedReader reader = resource.getReader()) {
                    Map<String, ShaderPresetConfig> presets = GSON.fromJson(reader, mapType);
                    if (presets == null) continue;
                    mergedPresets.putAll(presets);
                } catch (Exception exception) {
                    //? if >=1.20.6 {
                    Main.LOGGER.warn("Failed to parse shader presets {} in pack '{}' ({})", RESOURCE_ID, resource.getPack().getInfo().title(), resource.getPack().getId(), exception);
                    //?} else
                    /*Main.LOGGER.warn("Failed to parse shader presets {} in pack '{}'", RESOURCE_ID, resource.getPack().getName(), exception);*/
                }
            }

            mergedPresets.values().removeAll(Collections.singleton(null));

            for (Map.Entry<String, ShaderPresetConfig> entry : mergedPresets.entrySet()) {
                entry.getValue().editable = false;
                entry.getValue().key = entry.getKey();
            }

            return mergedPresets;
        });
    }

    public CompletableFuture<Void> apply(Map<String, ShaderPresetConfig> data, ResourceManager manager, Executor executor) {
        presets = data;
        if (Main.getConfig() != null) {
            Main.getConfig().loadDefaultPresets();
        }
        return CompletableFuture.completedFuture(null);
    }
}
