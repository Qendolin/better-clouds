package com.qendolin.betterclouds;

import com.google.common.base.Objects;
import com.google.gson.*;
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
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;

public class Config {

    public static final String DEFAULT_PRESET_KEY = "default";
    public static final InstanceCreator<Config> INSTANCE_CREATOR = type -> new Config();
    public static final RegistryKeySerializer REGISTRY_KEY_SERIALIZER = new RegistryKeySerializer();


    @SuppressWarnings("unused")
    public Config() {
    }

    public Config(Config other) {
        this.distance = other.distance;
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
        this.fadeEdge = other.fadeEdge;
        this.usePersistentBuffers = other.usePersistentBuffers;
        this.irisSupport = other.irisSupport;
        this.enabled = other.enabled;
        this.cloudOverride = other.cloudOverride;
        this.useIrisFBO = other.useIrisFBO;
        this.selectedPreset = other.selectedPreset;
        this.presets = other.presets;
        this.presets.replaceAll(ShaderConfigPreset::new);
        this.lastTelemetryVersion = other.lastTelemetryVersion;
        this.gpuIncompatibleMessageEnabled = other.gpuIncompatibleMessageEnabled;
        this.enabledDimensions = other.enabledDimensions;
        this.celestialBodyHalo = other.celestialBodyHalo;
    }

    @SerialEntry
    public boolean enabled = true;
    @SerialEntry
    public float distance = 4;
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
    public float windSpeedFactor = 1.0f;
    @SerialEntry
    public float colorVariationFactor = 1.0f;
    @SerialEntry
    public boolean celestialBodyHalo = true;
    @SerialEntry
    public int chunkSize = 32;
    @SerialEntry
    public float samplingScale = 1;
    @SerialEntry
    public float scaleFalloffMin = 0.25f;
    @SerialEntry
    public float fadeEdge = 0.15f;
    @SerialEntry
    public boolean usePersistentBuffers = true;
    @SerialEntry
    public boolean irisSupport = true;
    @SerialEntry
    public boolean cloudOverride = true;
    @SerialEntry
    public boolean useIrisFBO = true;
    @SerialEntry
    public int selectedPreset = 0;
    @SerialEntry
    public List<ShaderConfigPreset> presets = new ArrayList<>();
    @SerialEntry
    public int lastTelemetryVersion = 0;
    @SerialEntry
    public boolean gpuIncompatibleMessageEnabled = true;
    @SerialEntry
    public List<RegistryKey<DimensionType>> enabledDimensions = new ArrayList<>(List.of(DimensionTypes.OVERWORLD));

    public void loadDefaultPresets() {
        // Remember which default preset was selected, if any
        String selectedDefaultPreset = preset().key;
        Map<String, ShaderConfigPreset> defaults = new HashMap<>(ShaderPresetLoader.INSTANCE.presets());
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
            ShaderConfigPreset defaultPreset = defaults.get(DEFAULT_PRESET_KEY);
            if (defaultPreset != null) {
                ShaderConfigPreset defaultCopy = new ShaderConfigPreset(defaultPreset);
                defaultCopy.markAsCopy();
                presets.add(defaultCopy);
                selectedPreset = presets.indexOf(defaultCopy);
            }
        }
        sortPresets();
    }

    @NotNull
    public ShaderConfigPreset preset() {
        if (presets.isEmpty()) {
            addFirstPreset();
        }
        selectedPreset = MathHelper.clamp(selectedPreset, 0, presets.size() - 1);
        return presets.get(selectedPreset);
    }

    private static boolean isPresetEqualToEmpty(ShaderConfigPreset preset) {
        if (preset == null) return true;
        String title = preset.title;
        // The title does not matter
        preset.title = ShaderConfigPreset.EMPTY_PRESET.title;
        boolean equal = preset.isEqualTo(ShaderConfigPreset.EMPTY_PRESET);
        preset.title = title;
        return equal;
    }

    public void sortPresets() {
        ShaderConfigPreset selected = preset();
        Comparator<ShaderConfigPreset> comparator = Comparator.
            <ShaderConfigPreset, Boolean>comparing(preset -> !preset.editable)
            .thenComparing(preset -> !DEFAULT_PRESET_KEY.equals(preset.key))
            .thenComparing(preset -> preset.title);
        presets.sort(comparator);
        selectedPreset = presets.indexOf(selected);
    }

    public void addFirstPreset() {
        if (!presets.isEmpty()) return;
        presets.add(new ShaderConfigPreset());
    }

    public int blockDistance() {
        return (int) (this.distance * MinecraftClient.getInstance().options.getViewDistance().getValue() * 16);
    }

    public static class ShaderConfigPreset {

        public static final InstanceCreator<ShaderConfigPreset> INSTANCE_CREATOR = type -> new ShaderConfigPreset();
        protected static final ShaderConfigPreset EMPTY_PRESET = new ShaderConfigPreset();

        public ShaderConfigPreset() {
            this("");
        }

        public ShaderConfigPreset(String title) {
            this.title = title;
        }

        public ShaderConfigPreset(ShaderConfigPreset other) {
            this.title = other.title;
            this.key = other.key;
            this.editable = other.editable;
            this.upscaleResolutionFactor = other.upscaleResolutionFactor;
            this.gamma = other.gamma;
            this.sunPathAngle = other.sunPathAngle;
            this.dayBrightness = other.dayBrightness;
            this.nightBrightness = other.nightBrightness;
            this.sunriseStartTime = other.sunriseStartTime;
            this.sunriseEndTime = other.sunriseEndTime;
            this.sunsetStartTime = other.sunsetStartTime;
            this.sunsetEndTime = other.sunsetEndTime;
            this.saturation = other.saturation;
            this.opacity = other.opacity;
            this.opacityFactor = other.opacityFactor;
            this.opacityExponent = other.opacityExponent;
            this.tintRed = other.tintRed;
            this.tintGreen = other.tintGreen;
            this.tintBlue = other.tintBlue;

            //!! NOTE: Don't forget to update `isEqualTo` when adding fields
        }

        @SerialEntry
        public String title;
        @SerialEntry
        @Nullable
        public String key;
        @SerialEntry
        public boolean editable = true;
        @SerialEntry
        public float upscaleResolutionFactor = 1f;
        @SerialEntry
        public float gamma = 1f;
        @SerialEntry
        public float sunPathAngle = 0f;
        @SerialEntry
        public int sunriseStartTime = -785;
        @SerialEntry
        public int sunriseEndTime = 1163;
        @SerialEntry
        public int sunsetStartTime = 10837;
        @SerialEntry
        public int sunsetEndTime = 12785;
        @SerialEntry
        public float dayBrightness = 1f;
        @SerialEntry
        public float nightBrightness = 1f;
        @SerialEntry
        public float saturation = 1f;
        @SerialEntry
        public float opacity = 0.2f;
        @SerialEntry
        public float opacityFactor = 1f;
        @SerialEntry
        public float opacityExponent = 1.5f;
        @SerialEntry
        public float tintRed = 1f;
        @SerialEntry
        public float tintGreen = 1f;
        @SerialEntry
        public float tintBlue = 1f;

        public float gamma() {
            if (gamma > 0) {
                return gamma;
            } else {
                return -1 / gamma;
            }
        }

        public void markAsCopy() {
            editable = true;
            key = null;
        }

        public boolean isEqualTo(ShaderConfigPreset other) {
            if (this == other) return true;
            if (other == null) return false;
            return editable == other.editable &&
                Float.compare(other.upscaleResolutionFactor, upscaleResolutionFactor) == 0 &&
                Float.compare(other.gamma, gamma) == 0 &&
                Float.compare(other.sunPathAngle, sunPathAngle) == 0 &&
                sunriseStartTime == other.sunriseStartTime &&
                sunriseEndTime == other.sunriseEndTime &&
                sunsetStartTime == other.sunsetStartTime &&
                sunsetEndTime == other.sunsetEndTime &&
                Float.compare(other.dayBrightness, dayBrightness) == 0 &&
                Float.compare(other.nightBrightness, nightBrightness) == 0 &&
                Float.compare(other.saturation, saturation) == 0 &&
                Float.compare(other.opacity, opacity) == 0 &&
                Float.compare(other.opacityFactor, opacityFactor) == 0 &&
                Float.compare(other.opacityExponent, opacityExponent) == 0 &&
                Float.compare(other.tintRed, tintRed) == 0 &&
                Float.compare(other.tintGreen, tintGreen) == 0 &&
                Float.compare(other.tintBlue, tintBlue) == 0 &&
                Objects.equal(title, other.title) &&
                Objects.equal(key, other.key);
        }
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
