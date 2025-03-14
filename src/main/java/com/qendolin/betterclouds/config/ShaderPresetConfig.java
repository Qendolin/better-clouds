package com.qendolin.betterclouds.config;

import com.google.common.base.Objects;
import com.google.gson.InstanceCreator;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import org.jetbrains.annotations.Nullable;

public class ShaderPresetConfig {

    public static final InstanceCreator<ShaderPresetConfig> INSTANCE_CREATOR = type -> new ShaderPresetConfig();
    protected static final ShaderPresetConfig EMPTY_PRESET = new ShaderPresetConfig();

    public ShaderPresetConfig() {
        this("");
    }

    public ShaderPresetConfig(String title) {
        this.title = title;
    }

    public ShaderPresetConfig(ShaderPresetConfig other) {
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
        this.worldCurvatureSize = other.worldCurvatureSize;

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
    @SerialEntry
    public int worldCurvatureSize = 0;


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

    // Can't override the Object#equals method since it causes an issue with indexOf in the GUI
    public boolean isEqualTo(ShaderPresetConfig other) {
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
            Objects.equal(key, other.key) &&
            worldCurvatureSize == other.worldCurvatureSize;
    }
}
