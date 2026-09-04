package com.qendolin.betterclouds.config.preset;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPresetConfig {
    @SerialEntry
    public String title = "Default";
    @SerialEntry
    @Nullable
    public String key = null;
    @SerialEntry
    public String description = "";

    @SerialEntry
    public boolean editable = true;

    public void markAsCopy() {
        editable = true;
        key = null;
    }
}
