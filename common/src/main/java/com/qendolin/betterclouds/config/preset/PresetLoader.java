package com.qendolin.betterclouds.config.preset;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.config.compat.ShaderPresetConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.*;
import org.jspecify.annotations.NonNull;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class PresetLoader<T extends AbstractPresetConfig> implements PreparableReloadListener {
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

    public final Identifier id;
    public final Identifier resourceId;
    private final Gson GSON = new GsonBuilder()
            .setStrictness(Strictness.LENIENT)
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(ShaderPresetConfig.class, ShaderPresetConfig.INSTANCE_CREATOR)
            .registerTypeAdapter(NoisePresetConfig.class, NoisePresetConfig.INSTANCE_CREATOR)
            .create();
    private final Type gsonType;
    private Map<String, T> presets = null;

    public PresetLoader(String id, String resourceId, Type gsonType) {
        this.id = Identifier.fromNamespaceAndPath(BetterCloudsStatic.MODID, id);
        this.resourceId = Identifier.fromNamespaceAndPath(BetterCloudsStatic.MODID, resourceId);
        this.gsonType = gsonType;
    }

    public Map<String, T> presets() {
        if (presets == null) return Map.of();
        return Map.copyOf(presets);
    }

    @Override
    public @NonNull CompletableFuture<Void> reload(SharedState store, @NonNull Executor loadExecutor, PreparationBarrier helper, @NonNull Executor applyExecutor) {
        ResourceManager manager = store.resourceManager();
        return load(manager, loadExecutor).thenCompose(helper::wait).thenCompose(this::apply);
    }

    public CompletableFuture<Map<String, T>> load(ResourceManager manager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, T> mergedPresets = new HashMap<>();
            for (Resource resource : manager.getResourceStack(resourceId)) {
                try (BufferedReader reader = resource.openAsReader()) {
                    Map<String, T> presets = GSON.fromJson(reader, gsonType);
                    if (presets == null) continue;
                    mergedPresets.putAll(presets);
                } catch (Exception exception) {
                    try (PackResources source = resource.source()) {
                        BetterCloudsStatic.getLogger().warn(
                                "Failed to parse {} in {} in pack '{}' ({})",
                                id,
                                resourceId,
                                source.location().title(),
                                source.packId(),
                                exception
                        );
                    }
                }
            }

            mergedPresets.values().removeAll(Collections.singleton(null));

            for (Map.Entry<String, T> entry : mergedPresets.entrySet()) {
                entry.getValue().editable = false;
                entry.getValue().key = entry.getKey();
            }

            try {
                // make default preset editable
                mergedPresets.get("default").editable = true;
            } catch (NullPointerException e) {
                BetterCloudsStatic.getLogger().warn("No default preset loaded?");
            }

            return mergedPresets;
        }, executor);
    }

    public CompletableFuture<Void> apply(Map<String, T> data) {
        presets = data;
        if (ConfigManager.instance() != null) {
            ConfigManager.instance().loadDefaultPresets();
        }
        return CompletableFuture.completedFuture(null);
    }
}
