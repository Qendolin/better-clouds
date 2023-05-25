package com.qendolin.betterclouds;

import dev.isxander.yacl.config.ConfigEntry;
import net.minecraft.client.MinecraftClient;

public class Config {

    public Config() {}

    public Config(Config other) {
        this.distance = other.distance;
        this.opacity = other.opacity;
        this.jitter = other.jitter;
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
        this.chunkSize = other.chunkSize;
        this.samplingScale = other.samplingScale;
        this.scaleFalloffMin = other.scaleFalloffMin;
        this.fadeEdge = other.fadeEdge;
        this.usePersistentBuffers = other.usePersistentBuffers;
        this.writeDepth = other.writeDepth;
        this.highQualityDepth = other.highQualityDepth;
        this.irisSupport = other.irisSupport;
        this.enabled = other.enabled;
        this.cloudOverride = other.cloudOverride;
        this.useIrisFBO = other.useIrisFBO;
        this.sunPathAngle = other.sunPathAngle;
        this.gamma = other.gamma;
        this.dayBrightness = other.dayBrightness;
        this.nightBrightness = other.nightBrightness;
        this.alphaFactor = other.alphaFactor;
        this.saturation = other.saturation;
        this.tintRed = other.tintRed;
        this.tintGreen = other.tintGreen;
        this.tintBlue = other.tintBlue;
    }

    @ConfigEntry
    public boolean enabled = true;
    @ConfigEntry
    public float distance = 4;
    @ConfigEntry
    public float opacity = 0.2f;
    @ConfigEntry
    public float jitter = 1.0f;
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
    public boolean highQualityDepth = false;
    @ConfigEntry
    public boolean irisSupport = false;
    @ConfigEntry
    public boolean cloudOverride = false;
    @ConfigEntry
    public boolean useIrisFBO = true;
    @ConfigEntry
    public float gamma = 1f;
    @ConfigEntry
    public float sunPathAngle = 0f;
    @ConfigEntry
    public float dayBrightness = 1f;
    @ConfigEntry
    public float nightBrightness = 1f;
    @ConfigEntry
    public float saturation = 1f;
    @ConfigEntry
    public float alphaFactor = 1f;
    @ConfigEntry
    public float tintRed = 1f;
    @ConfigEntry
    public float tintGreen = 1f;
    @ConfigEntry
    public float tintBlue = 1f;

    public int blockDistance() {
        return (int) (this.distance * MinecraftClient.getInstance().options.getViewDistance().getValue() * 16);
    }

    public float gamma() {
        if(gamma > 0) {
            return gamma;
        } else {
            return -1/gamma;
        }
    }
}
