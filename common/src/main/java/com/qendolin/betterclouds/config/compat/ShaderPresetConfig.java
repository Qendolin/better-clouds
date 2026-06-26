package com.qendolin.betterclouds.config.compat;

import com.google.gson.InstanceCreator;
import com.qendolin.betterclouds.config.Configs;
import com.qendolin.betterclouds.config.preset.AbstractPresetConfig;
import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class ShaderPresetConfig extends AbstractPresetConfig {
    public static final InstanceCreator<ShaderPresetConfig> INSTANCE_CREATOR = _ -> new ShaderPresetConfig();
    public static final ShaderPresetConfig EMPTY_PRESET = new ShaderPresetConfig();

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
    @SerialEntry
    public float bottomTintRed = 0.75f;
    @SerialEntry
    public float bottomTintGreen = 0.75f;
    @SerialEntry
    public float bottomTintBlue = 0.75f;
    @SerialEntry
    public float bottomTransitionRange = 0.5f;
    @SerialEntry
    public int worldCurvatureSize = 0;

    public ShaderPresetConfig() {
        this("");
    }

    public ShaderPresetConfig(String title) {
        this.title = title;
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public ShaderPresetConfig(ShaderPresetConfig other) {
        Configs.copy(this, other);
    }

    public float gamma() {
        if (gamma > 0) {
            return gamma;
        } else {
            return -1 / gamma;
        }
    }

    @Override
    public AbstractPresetConfig getEmptyPreset() {
        return EMPTY_PRESET;
    }
}
