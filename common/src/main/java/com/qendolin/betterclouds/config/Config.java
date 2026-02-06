package com.qendolin.betterclouds.config;

import com.google.gson.*;
import com.qendolin.betterclouds.compat.BigGlobeCompat;
import com.qendolin.betterclouds.compat.MiddleEarthCompat;
import com.qendolin.betterclouds.util.PreLaunchGuard;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.*;

public class Config {

    public static final String DEFAULT_PRESET_KEY = "default";
    public static final InstanceCreator<Config> INSTANCE_CREATOR = type -> new Config();
    public static final RegistryKeySerializer REGISTRY_KEY_SERIALIZER = new RegistryKeySerializer();

    static {
        PreLaunchGuard.check();
    }

    @SuppressWarnings("unused")
    public Config() {
    }

    public Config(Config other) {
        this.migrationVersion = other.migrationVersion;
        this.randomPlacement = other.randomPlacement;
        this.fuzziness = other.fuzziness;
        this.shuffle = other.shuffle;
        this.yRange = other.yRange;
        this.yOffset = other.yOffset;
        this.sparsity = other.sparsity;
        this.spacing = other.spacing;
        this.sizeXZ = other.sizeXZ;
        this.sizeY = other.sizeY;
        this.travelSpeed = other.travelSpeed;
        this.windEffectFactor = other.windEffectFactor;
        this.windSpeedFactor = other.windSpeedFactor;
        this.colorVariationFactor = other.colorVariationFactor;
        this.chunkSize = other.chunkSize;
        this.samplingScale = other.samplingScale;
        this.scaleFalloffMin = other.scaleFalloffMin;
        this.fogRangeFactor = other.fogRangeFactor;
        this.fogEndFactor = other.fogEndFactor;
        this.usePersistentBuffers = other.usePersistentBuffers;
        this.irisSupport = other.irisSupport;
        this.enabled = other.enabled;
        this.cloudOverride = other.cloudOverride;
        this.useIrisFBO = other.useIrisFBO;
        this.selectedPreset = other.selectedPreset;
        //noinspection IncompleteCopyConstructor
        this.presets = other.presets == null ? new ArrayList<>() : new ArrayList<>(other.presets);
        this.presets.replaceAll(ShaderPresetConfig::new);
        this.lastTelemetryVersion = other.lastTelemetryVersion;
        this.gpuIncompatibleMessageEnabled = other.gpuIncompatibleMessageEnabled;
        this.issueReportEnabled = other.issueReportEnabled;
        //noinspection IncompleteCopyConstructor
        this.enabledDimensions = other.enabledDimensions == null ? new ArrayList<>() : new ArrayList<>(other.enabledDimensions);
        this.celestialBodyHalo = other.celestialBodyHalo;
        this.useFrustumCulling = other.useFrustumCulling;
        this.lunarSucksMessageEnabled = other.lunarSucksMessageEnabled;
        //noinspection IncompleteCopyConstructor
        this.sereneSeasonsConfig = new SereneSeasonsConfig(other.sereneSeasonsConfig);
        //noinspection IncompleteCopyConstructor
        this.fabricSeasonsConfig = new FabricSeasonsConfig(other.fabricSeasonsConfig);
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
    public boolean shuffle = false;
    @SerialEntry
    public float yRange = 64f;
    @SerialEntry
    public float yOffset = 0f;
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
    public boolean usePersistentBuffers = true;
    @SerialEntry
    public boolean useFrustumCulling = true;
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
    public int lastTelemetryVersion = 0;
    @SerialEntry
    public boolean gpuIncompatibleMessageEnabled = true;
    @SerialEntry
    public boolean issueReportEnabled = true;
    @SerialEntry
    public boolean lunarSucksMessageEnabled = true;
    @SerialEntry
    public List<RegistryKey<DimensionType>> enabledDimensions = new ArrayList<>(getDefaultDimensions());
    @SerialEntry
    public SereneSeasonsConfig sereneSeasonsConfig = new SereneSeasonsConfig();
    @SerialEntry
    public FabricSeasonsConfig fabricSeasonsConfig = new FabricSeasonsConfig();

    public void loadDefaultPresets() {
        // Remember which default preset was selected, if any
        String selectedDefaultPreset = preset().key;
        Map<String, ShaderPresetConfig> defaults = new HashMap<>(ShaderPresetLoader.INSTANCE.presets());
        boolean missingDefault = presets.stream().noneMatch(preset -> DEFAULT_PRESET_KEY.equals(preset.key));
        presets.removeIf(preset -> preset.key != null && !preset.editable && defaults.containsKey(preset.key));
        presets.addAll(defaults.values());

        if (selectedDefaultPreset != null) {
            // Restore the selected default preset
            presets.stream()
                .filter(preset -> selectedDefaultPreset.equals(preset.key)).findFirst()
                .ifPresentOrElse(prevSelectedPreset -> selectedPreset = presets.indexOf(prevSelectedPreset), () -> selectedPreset = 0);
        }

        if (missingDefault) {
            // No preset with the key 'default' was present,
            // so it is assumed that the presets are not initialized
            presets.removeIf(Config::isPresetEqualToEmpty);
            ShaderPresetConfig defaultPreset = defaults.get(DEFAULT_PRESET_KEY);
            if (defaultPreset != null) {
                ShaderPresetConfig defaultCopy = new ShaderPresetConfig(defaultPreset);
                defaultCopy.markAsCopy();
                presets.add(defaultCopy);
                selectedPreset = presets.indexOf(defaultCopy);
            }
        }
        sortPresets();
    }

    @NotNull
    public ShaderPresetConfig preset() {
        if (presets == null || presets.isEmpty()) {
            addFirstPreset();
        }
        selectedPreset = MathHelper.clamp(selectedPreset, 0, presets.size() - 1);
        return presets.get(selectedPreset);
    }

    private static boolean isPresetEqualToEmpty(ShaderPresetConfig preset) {
        if (preset == null) return true;
        String title = preset.title;
        // The title does not matter
        preset.title = ShaderPresetConfig.EMPTY_PRESET.title;
        boolean equal = preset.isEqualTo(ShaderPresetConfig.EMPTY_PRESET);
        preset.title = title;
        return equal;
    }

    public void sortPresets() {
        ShaderPresetConfig selected = preset();
        Comparator<ShaderPresetConfig> comparator = Comparator.
            <ShaderPresetConfig, Boolean>comparing(preset -> !preset.editable)
            .thenComparing(preset -> !DEFAULT_PRESET_KEY.equals(preset.key))
            .thenComparing(preset -> preset.title);
        presets.sort(comparator);
        selectedPreset = presets.indexOf(selected);
    }

    public void addFirstPreset() {
        if (presets == null) presets = new ArrayList<>();
        if (!presets.isEmpty()) return;
        presets.add(new ShaderPresetConfig());
    }

    public int blockDistance() {
        return MinecraftClient.getInstance().options.getCloudRenderDistance().getValue() * 16;
    }

    public static List<RegistryKey<DimensionType>> getDefaultDimensions() {
        return List.of(
            DimensionTypes.OVERWORLD,
            BigGlobeCompat.DIMENSION_KEY,
            MiddleEarthCompat.DIMENSION_KEY);
    }

    public static class RegistryKeySerializer implements JsonSerializer<RegistryKey<DimensionType>>, JsonDeserializer<RegistryKey<DimensionType>> {
        private RegistryKeySerializer() {
        }

        @Override
        public RegistryKey<DimensionType> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isString())
                throw new JsonParseException("RegistryKey must be a string");
            try {
                Identifier id = Identifier.of(json.getAsString());
                return RegistryKey.of(RegistryKeys.DIMENSION_TYPE, id);
            } catch (InvalidIdentifierException e) {
                throw new JsonParseException("Invalid RegistryKey: " + e.getMessage());
            }
        }

        @Override
        public JsonElement serialize(RegistryKey<DimensionType> src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getValue().toString());
        }
    }

}
