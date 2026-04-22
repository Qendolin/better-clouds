package com.qendolin.betterclouds.config;

import com.google.gson.InstanceCreator;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import org.jetbrains.annotations.Nullable;

public class NoisePresetConfig {
    public static final InstanceCreator<NoisePresetConfig> INSTANCE_CREATOR = _ -> new NoisePresetConfig();
    protected static final NoisePresetConfig EMPTY_PRESET = new NoisePresetConfig();

    @SerialEntry
    public String title;
    @SerialEntry
    @Nullable
    public String key;
    @SerialEntry
    public boolean editable = true;

    public NoisePresetConfig() {
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public NoisePresetConfig(NoisePresetConfig other) {
        Configs.copy(this, other);
    }

    // Can't override the Object#equals method since it causes an issue with indexOf in the GUI
    public boolean isEqualTo(NoisePresetConfig other) {
        if (!(other instanceof NoisePresetConfig))
            return false;
        return Configs.equal(this, other);
    }
}
