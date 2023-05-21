package com.qendolin.betterclouds;

import com.qendolin.betterclouds.config.Entry;
import com.qendolin.betterclouds.config.ModConfig;
import net.minecraft.client.MinecraftClient;

public class Config implements ModConfig {
    public Config() {}
    public Config(Config other) {
        super();
        // TODO: keep up to date
        this.distance = other.distance;
        this.chunkSize = other.chunkSize;
        this.opacity = other.opacity;
        this.jitter = other.jitter;
        this.spacing = other.spacing;
        this.sparsity = other.sparsity;
        this.spreadY = other.spreadY;
        this.usePersistentBuffers = other.usePersistentBuffers;
        this.windSpeed = other.windSpeed;
        this.fuzziness = other.fuzziness;
        this.enableExperimentalIrisSupport = other.enableExperimentalIrisSupport;
        this.writeDepth = other.writeDepth;
        this.depthTest = other.depthTest;
        this.fakeScaleFalloffMin = other.fakeScaleFalloffMin;
        this.windFactor = other.windFactor;
    }
    @Entry.FloatRange(min = 1, max = 4, stringer = "times")
    public float distance = 4;
    @Entry.FloatRange(min = 0, max = 1, step=0.01f, stringer = "percent")
    public float opacity = 0.2f;
    @Entry.FloatRange(min = 0, max = 1, stringer = "percent")
    public float jitter = 1.0f;
    @Entry.FloatRange(min = 0, max = 1, stringer = "percent")
    public float fuzziness = 1.0f;
    @Entry.FloatRange(min = 0, max = 128, step = 0.5f)
    public float spreadY = 64f;
    @Entry.FloatRange(min = 0, max = 1, step = 0.01f)
    public float sparsity = 0f;
    @Entry.FloatRange(min = 2, max = 32, step = 0.25f)
    public float spacing = 5.25f;
    @Entry.FloatRange(min = 2, max = 64, step = 0.25f)
    public float sizeXZ = 16f;
    @Entry.FloatRange(min = 1, max = 32, step = 0.25f)
    public float sizeY = 8f;
    @Entry.FloatRange(min = 0, max = 0.1f, step = 0.005f, stringer = "blocksPerSecond")
    public float windSpeed = 0.03f;
    @Entry.FloatRange()
    public float windFactor = 1.0f;
    @Entry.IntRange(min = 16, max = 64)
    public int chunkSize = 32;
    @Entry.FloatRange(min = 0.5f, max = 2)
    // TODO: implement
    public float samplingScale = 1;
    @Entry.FloatRange(min = 0.0f, max = 1, step = 0.05f)
    public float fakeScaleFalloffMin = 0.25f;
    @Entry.FloatRange(min = 0.1f, max = 0.5f, step = 0.01f)
    public float fadeEdge = 0.15f;
    @Entry.ToggleButton
    public boolean usePersistentBuffers = true;
    @Entry.ToggleButton
    public boolean writeDepth = false;
    @Entry.ToggleButton
    public boolean depthTest = true;
    @Entry.ToggleButton
    public boolean enableExperimentalIrisSupport = true;
    @Entry.ToggleButton
    public boolean useVanillaClouds = false;
    @Entry.ToggleButton
    public boolean enableExperimentalCloudOverride = false;
    @Entry.ToggleButton
    public boolean useIrisFBO = true;
    @Entry.FloatRange(min = -5, max = 5, step = 0.01f)
    public float gamma = 2.2f;
    @Entry.FloatRange(min = 0, max = 5, step = 0.01f)
    public float brightness = 1f;
    @Entry.FloatRange(min = 0, max = 1, step = 0.01f)
    public float alphaFactor = 1f;
    @Entry.FloatRange(min = 0, max = 1, step = 0.01f)
    public float red = 1f;
    @Entry.FloatRange(min = 0, max = 1, step = 0.01f)
    public float green = 1f;
    @Entry.FloatRange(min = 0, max = 1, step = 0.01f)
    public float blue = 1f;
    @Entry.FloatRange(min = 0, max = 1, step = 0.01f)
    public float gradientPos = 0f;

    public transient boolean hasChanged = false;

    public int blockDistance() {
        return (int) (this.distance * MinecraftClient.getInstance().options.getViewDistance().getValue() * 16);
    }

    private String blocksPerSecond(float v) {
        return String.format("%.1f", v*20) + " b/s";
    }

    private String times(float v) {
        return String.format("%.1f", v)+"x";
    }

    private String percent(float v) {
        return ((int) (v*100)) + "%";
    }

    @Override
    public String getId() {
        return Main.MODID;
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
