package com.qendolin.betterclouds.config;

import com.google.gson.*;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.compat.BigGlobeCompat;
import com.qendolin.betterclouds.compat.MiddleEarthCompat;
import com.qendolin.betterclouds.config.compat.*;
import com.qendolin.betterclouds.config.preset.*;
import com.qendolin.betterclouds.util.PreLaunchGuard;
import dev.isxander.yacl3.api.NameableEnum;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.IdentifierException;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.*;

public class Config {
    public static final String DEFAULT_PRESET_KEY = "default";
    public static final InstanceCreator<Config> INSTANCE_CREATOR = _ -> new Config();
    public static final RegistryKeySerializer REGISTRY_KEY_SERIALIZER = new RegistryKeySerializer();

    static {
        PreLaunchGuard.check();
    }

    @SerialEntry
    public int migrationVersion = 0;
    @SerialEntry
    public boolean enabled = true;
    @SerialEntry
    public float randomPlacement = 1.0f;
    @SerialEntry
    public float fuzziness = 1.0f;
    @SerialEntry
    public float yRange = 64f;
    @SerialEntry
    public float yOffset = 0f;
    @SerialEntry
    public float bottomSparsity = 0f;
    @SerialEntry
    public float sparsity = 0f;
    @SerialEntry
    public float spacing = 5.25f;
    @SerialEntry
    public float sizeXZ = 16f;
    @SerialEntry
    public float sizeY = 8f;
    @SerialEntry
    public float travelSpeed = 0.03f;
    @SerialEntry
    public float windEffectFactor = 1.0f;
    @SerialEntry
    public float windSpeedFactor = 0.8f;
    @SerialEntry
    public float colorVariationFactor = 0.8f;
    @SerialEntry
    public boolean celestialBodyHalo = true;
    @SerialEntry
    public int chunkSize = 32;
    @SerialEntry
    public float samplingScale = 1;
    @SerialEntry
    public float scaleFalloffMin = 0.25f;
    @SerialEntry
    public float fogRangeFactor = 1f;
    @SerialEntry
    public float fogEndFactor = 1f;
    @SerialEntry
    public TimeSource timeSource = TimeSource.WORLD;
    @SerialEntry
    public boolean usePersistentBuffers = true;
    @SerialEntry
    public boolean useFrustumCulling = true;
    @SerialEntry
    public boolean useSamplerCaching = true;
    @SerialEntry
    public boolean irisSupport = true;
    @SerialEntry
    public boolean cloudOverride = true;
    @SerialEntry
    public boolean useIrisFBO = true;

    @SerialEntry
    public int selectedPreset = 0;
    @SerialEntry
    public List<ShaderPresetConfig> presets = new ArrayList<>();

    @SerialEntry
    public int selectedNoisePreset = 0;
    @SerialEntry
    public List<NoisePresetConfig> noisePresets = new ArrayList<>();

    @SerialEntry
    public boolean gpuIncompatibleMessageEnabled = true;
    @SerialEntry
    public List<ResourceKey<DimensionType>> enabledDimensions = new ArrayList<>(getDefaultDimensions());
    @SerialEntry
    public SereneSeasonsConfig sereneSeasonsConfig = new SereneSeasonsConfig();
    @SerialEntry
    public FabricSeasonsConfig fabricSeasonsConfig = new FabricSeasonsConfig();

    @SuppressWarnings("unused")
    public Config() {
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public Config(Config other) {
        Configs.copy(this, other);
    }

    public static List<ResourceKey<DimensionType>> getDefaultDimensions() {
        return List.of(
                BuiltinDimensionTypes.OVERWORLD,
                BigGlobeCompat.DIMENSION_KEY,
                MiddleEarthCompat.DIMENSION_KEY);
    }

    public void loadDefaultPresets() {
        for (PresetLoader<?> presetLoader : PresetLoader.ALL_PRESETS) {
            if (presetLoader.presets().isEmpty()) {
                // Not initialized yet, wait for other preset file to be loaded
                BetterCloudsStatic.getLogger().info("{} not loaded yet, waiting for next resource load", presetLoader.id);
                return;
            }
        }

        BetterCloudsStatic.getLogger().info("All preset resources loaded, initializing preset config");
        loadDefaultPreset(PresetLoader.SHADER, presets);
        loadDefaultPreset(PresetLoader.NOISE, noisePresets);
        sortShaderPresets(false);
        sortNoisePresets(false);
    }

    public <T extends AbstractPresetConfig> void loadDefaultPreset(
            PresetLoader<T> loader,
            List<T> presets
    ) {
        assert !loader.presets().isEmpty();

        // Remember which default preset was selected, if any
        Map<String, T> defaults = loader.presets();     // map is copied dw
        presets.removeIf(preset -> preset.key != null && !preset.editable && defaults.containsKey(preset.key));
        presets.addAll(defaults.values());
    }

    @NotNull
    public ShaderPresetConfig shaderPreset() {
        if (presets.isEmpty())
            return PresetLoader.SHADER.presets().getOrDefault(DEFAULT_PRESET_KEY, ShaderPresetConfig.EMPTY_PRESET);
        selectedPreset = Mth.clamp(selectedPreset, 0, presets.size() - 1);
        return presets.get(selectedPreset);
    }

    public void sortShaderPresets() {
        sortShaderPresets(true);
    }

    public void sortShaderPresets(boolean updateSelectedIndex) {
        ShaderPresetConfig selected = shaderPreset();
        Comparator<ShaderPresetConfig> comparator = Comparator.
                <ShaderPresetConfig, Boolean>comparing(preset -> !preset.editable)
                .thenComparing(preset -> !DEFAULT_PRESET_KEY.equals(preset.key))
                .thenComparing(preset -> preset.title);
        presets.sort(comparator);
        if (updateSelectedIndex)
            selectedPreset = presets.indexOf(selected);
    }

    @NotNull
    public NoisePresetConfig noisePreset() {
        if (noisePresets.isEmpty())
            return PresetLoader.NOISE.presets().getOrDefault(DEFAULT_PRESET_KEY, NoisePresetConfig.EMPTY_PRESET);
        selectedNoisePreset = Mth.clamp(selectedNoisePreset, 0, noisePresets.size() - 1);
        return noisePresets.get(selectedNoisePreset);
    }

    public void sortNoisePresets() {
        sortNoisePresets(true);
    }

    public void sortNoisePresets(boolean updateSelectedIndex) {
        NoisePresetConfig selected = noisePreset();
        Comparator<NoisePresetConfig> comparator = Comparator.
                <NoisePresetConfig, Boolean>comparing(preset -> !preset.editable)
                .thenComparing(preset -> !DEFAULT_PRESET_KEY.equals(preset.key))
                .thenComparing(preset -> preset.title);
        noisePresets.sort(comparator);

        if (updateSelectedIndex)
            selectedNoisePreset = noisePresets.indexOf(selected);
    }

    public long getCloudTicks(Minecraft client, int rendererTicks) {
        if (client.level == null) return rendererTicks;
        return switch (timeSource) {
            case WORLD -> client.level.getOverworldClockTime();
            case PLAYTIME -> client.level.getGameTime();
            case RENDERER -> rendererTicks;
        };
    }

    public int blockDistance() {
        return Minecraft.getInstance().options.cloudRange().get() * 16;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Config))
            return false;
        return Configs.equal(this, obj);
    }

    @Override
    public int hashCode() {
        return Configs.hashCode(this);
    }

    public enum TimeSource implements NameableEnum {
        WORLD, PLAYTIME, RENDERER;

        @Override
        public Component getDisplayName() {
            return Component.translatable("betterclouds.config.entry.timeSource.option." + name().toLowerCase());
        }
    }

    public static class RegistryKeySerializer implements JsonSerializer<ResourceKey<DimensionType>>, JsonDeserializer<ResourceKey<DimensionType>> {
        private RegistryKeySerializer() {
        }

        @Override
        public ResourceKey<DimensionType> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isString())
                throw new JsonParseException("RegistryKey must be a string");
            try {
                Identifier id = Identifier.parse(json.getAsString());
                return ResourceKey.create(Registries.DIMENSION_TYPE, id);
            } catch (IdentifierException e) {
                throw new JsonParseException("Invalid RegistryKey: " + e.getMessage());
            }
        }

        @Override
        public JsonElement serialize(ResourceKey<DimensionType> src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.identifier().toString());
        }
    }

}
