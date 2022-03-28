package com.qendolin.betterclouds;

import com.qendolin.betterclouds.config.Entry;
import com.qendolin.betterclouds.config.ModConfig;
import net.minecraft.client.MinecraftClient;

public class Config implements ModConfig {
    public Config() {}
    public Config(Config other) {
        super();
        this.distance = other.distance;
        this.async = other.async;
        this.chunkSize = other.chunkSize;
        this.opacity = other.opacity;
        this.jitter = other.jitter;
        this.spacing = other.spacing;
        this.spreadY = other.spreadY;
        this.usePersistentBuffers = other.usePersistentBuffers;
        this.windSpeed = other.windSpeed;
        this.fuzziness = other.fuzziness;
        this.enableExperimentalIrisSupport = other.enableExperimentalIrisSupport;
        this.writeDepth = other.writeDepth;
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
    @Entry.FloatRange(min = 2, max = 32, step = 0.25f)
    public float spacing = 5.25f;
    @Entry.FloatRange(min = 2, max = 64, step = 0.25f)
    public float sizeX = 16f;
    @Entry.FloatRange(min = 1, max = 32, step = 0.25f)
    public float sizeY = 8f;
    @Entry.FloatRange(min = 0, max = 0.1f, step = 0.005f, stringer = "blocksPerSecond")
    public float windSpeed = 0.03f;
    @Entry.IntRange(min = 16, max = 64)
    public int chunkSize = 32;
    @Entry.IntRange(min = 1, max = 64)
    public int fadeEdge = 32;
    @Entry.ToggleButton
    public boolean usePersistentBuffers = true;
    @Entry.ToggleButton
    public boolean async = true;
    @Entry.ToggleButton
    public boolean writeDepth = false;
    @Entry.ToggleButton
    public boolean enableExperimentalIrisSupport = false;
    public transient boolean hasChanged = false;

    public int blockDistance() {
        return (int) (this.distance * MinecraftClient.getInstance().options.viewDistance * 16);
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
