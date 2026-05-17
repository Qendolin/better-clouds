package com.qendolin.betterclouds.config;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPresetConfig {
    @SerialEntry
    public String title = "";
    @SerialEntry
    @Nullable
    public String key = "";
    @SerialEntry
    public String description = "";

    @SerialEntry
    public boolean editable = true;

    public abstract AbstractPresetConfig getEmptyPreset();

    public void markAsCopy() {
        editable = true;
        key = null;
    }

    // Can't override the Object#equals method since it causes an issue with indexOf in the GUI
    public boolean isEqualTo(AbstractPresetConfig other) {
        if (!(other instanceof AbstractPresetConfig))
            return false;
        return Configs.equal(this, other);
    }
}
