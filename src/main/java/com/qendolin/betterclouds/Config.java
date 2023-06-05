package com.qendolin.betterclouds;

import com.google.common.base.Objects;
import dev.isxander.yacl.config.ConfigEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Config {

    public static final String DEFAULT_PRESET_KEY = "default";
    @SuppressWarnings("unused")
    public Config() {}

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
        this.windFactor = other.windFactor;
        this.colorVariationFactor = other.colorVariationFactor;
        this.chunkSize = other.chunkSize;
        this.samplingScale = other.samplingScale;
        this.scaleFalloffMin = other.scaleFalloffMin;
        this.fadeEdge = other.fadeEdge;
        this.usePersistentBuffers = other.usePersistentBuffers;
        this.writeDepth = other.writeDepth;
        this.irisSupport = other.irisSupport;
        this.enabled = other.enabled;
        this.cloudOverride = other.cloudOverride;
        this.useIrisFBO = other.useIrisFBO;
        this.selectedPreset = other.selectedPreset;
        this.presets = other.presets;
        this.presets.replaceAll(ShaderConfigPreset::new);
        this.lastTelemetryVersion = other.lastTelemetryVersion;
        this.gpuIncompatibleMessageEnabled = other.gpuIncompatibleMessageEnabled;
    }

    @ConfigEntry
    public boolean enabled = true;
    @ConfigEntry
    public float distance = 4;
    @ConfigEntry
    public float randomPlacement = 1.0f;
    @ConfigEntry
    public float fuzziness = 1.0f;
    @ConfigEntry
    public boolean shuffle = false;
    @ConfigEntry
    public float yRange = 64f;
    @ConfigEntry
    public float yOffset = 0f;
    @ConfigEntry
    public float sparsity = 0f;
    @ConfigEntry
    public float spacing = 5.25f;
    @ConfigEntry
    public float sizeXZ = 16f;
    @ConfigEntry
    public float sizeY = 8f;
    @ConfigEntry
    public float travelSpeed = 0.03f;
    @ConfigEntry
    public float windFactor = 1.0f;
    @ConfigEntry
    public float colorVariationFactor = 1.0f;
    @ConfigEntry
    public int chunkSize = 32;
    @ConfigEntry
    public float samplingScale = 1;
    @ConfigEntry
    public float scaleFalloffMin = 0.25f;
    @ConfigEntry
    public float fadeEdge = 0.15f;
    @ConfigEntry
    public boolean usePersistentBuffers = true;
    @ConfigEntry
    public boolean writeDepth = false;
    @ConfigEntry
    public boolean irisSupport = false;
    @ConfigEntry
    public boolean cloudOverride = true;
    @ConfigEntry
    public boolean useIrisFBO = true;
    @ConfigEntry
    public int selectedPreset = 0;
    @ConfigEntry
    public List<ShaderConfigPreset> presets = new ArrayList<>();
    @ConfigEntry
    public int lastTelemetryVersion = 0;
    @ConfigEntry
    public boolean gpuIncompatibleMessageEnabled = true;

    public void addFirstPreset() {
        if(presets.size() != 0) return;
        presets.add(new ShaderConfigPreset(""));
    }

    public void loadDefaultPresets() {
        // Remember which default preset was selected, if any
        String selectedDefaultPreset = preset().key;
        Map<String, ShaderConfigPreset> defaults = new HashMap<>(ShaderPresetLoader.INSTANCE.presets());
        boolean missingDefault = presets.stream().noneMatch(preset -> DEFAULT_PRESET_KEY.equals(preset.key));
        presets.removeIf(preset -> preset.key != null && !preset.editable && defaults.containsKey(preset.key));
        presets.addAll(defaults.values());

        if(selectedDefaultPreset != null) {
            // Restore the selected default preset
            presets.stream()
                .filter(preset -> selectedDefaultPreset.equals(preset.key)).findFirst()
                .ifPresentOrElse(prevSelectedPreset -> selectedPreset = presets.indexOf(prevSelectedPreset), () -> selectedPreset = 0);
        }

        if(missingDefault) {
            // No preset with the key 'default' was present,
            // so it is assumed that the presets are not initialized
            presets.removeIf(Config::isPresetEqualToEmpty);
            ShaderConfigPreset defaultPreset = defaults.get(DEFAULT_PRESET_KEY);
            if(defaultPreset != null) {
                ShaderConfigPreset defaultCopy = new ShaderConfigPreset(defaultPreset);
                defaultCopy.markAsCopy();
                presets.add(defaultCopy);
                selectedPreset = presets.indexOf(defaultCopy);
            }
        }
        sortPresets();
    }

    private static boolean isPresetEqualToEmpty(ShaderConfigPreset preset) {
        if(preset == null) return true;
        String title = preset.title;
        // The title does not matter
        preset.title = ShaderConfigPreset.EMPTY_PRESET.title;
        boolean equal = preset.equals(ShaderConfigPreset.EMPTY_PRESET);
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

    public static class ShaderConfigPreset {

        protected static final ShaderConfigPreset EMPTY_PRESET = new ShaderConfigPreset("");

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
            this.tintRed = other.tintRed;
            this.tintGreen = other.tintGreen;
            this.tintBlue = other.tintBlue;

            // NOTE: Don't forget to update `equals`
        }

        @ConfigEntry
        public String title;
        @ConfigEntry
        @Nullable
        public String key;
        @ConfigEntry
        public boolean editable = true;
        @ConfigEntry
        public float upscaleResolutionFactor = 1f;
        @ConfigEntry
        public float gamma = 1f;
        @ConfigEntry
        public float sunPathAngle = 0f;
        @ConfigEntry
        public int sunriseStartTime = -785;
        @ConfigEntry
        public int sunriseEndTime = 1163;
        @ConfigEntry
        public int sunsetStartTime = 10837;
        @ConfigEntry
        public int sunsetEndTime = 12785;
        @ConfigEntry
        public float dayBrightness = 1f;
        @ConfigEntry
        public float nightBrightness = 1f;
        @ConfigEntry
        public float saturation = 1f;
        @ConfigEntry
        public float opacity = 0.2f;
        @ConfigEntry
        public float opacityFactor = 1f;
        @ConfigEntry
        public float tintRed = 1f;
        @ConfigEntry
        public float tintGreen = 1f;
        @ConfigEntry
        public float tintBlue = 1f;

        public float gamma() {
            if(gamma > 0) {
                return gamma;
            } else {
                return -1/gamma;
            }
        }

        public void markAsCopy() {
            editable = true;
            key = null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ShaderConfigPreset preset = (ShaderConfigPreset) o;
            return editable == preset.editable &&
                Float.compare(preset.upscaleResolutionFactor, upscaleResolutionFactor) == 0 &&
                Float.compare(preset.gamma, gamma) == 0 &&
                Float.compare(preset.sunPathAngle, sunPathAngle) == 0 &&
                sunriseStartTime == preset.sunriseStartTime &&
                sunriseEndTime == preset.sunriseEndTime &&
                sunsetStartTime == preset.sunsetStartTime &&
                sunsetEndTime == preset.sunsetEndTime &&
                Float.compare(preset.dayBrightness, dayBrightness) == 0 &&
                Float.compare(preset.nightBrightness, nightBrightness) == 0 &&
                Float.compare(preset.saturation, saturation) == 0 &&
                Float.compare(preset.opacity, opacity) == 0 &&
                Float.compare(preset.opacityFactor, opacityFactor) == 0 &&
                Float.compare(preset.tintRed, tintRed) == 0 &&
                Float.compare(preset.tintGreen, tintGreen) == 0 &&
                Float.compare(preset.tintBlue, tintBlue) == 0 &&
                Objects.equal(title, preset.title) &&
                Objects.equal(key, preset.key);
        }
    }

    @NotNull
    public ShaderConfigPreset preset() {
        if(presets.size() == 0) {
            addFirstPreset();
        }
        selectedPreset = MathHelper.clamp(selectedPreset, 0, presets.size()-1);
        return presets.get(selectedPreset);
    }

    public int blockDistance() {
        return (int) (this.distance * MinecraftClient.getInstance().options.getViewDistance().getValue() * 16);
    }
}
