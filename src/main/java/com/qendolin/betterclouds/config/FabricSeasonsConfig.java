package com.qendolin.betterclouds.config;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class FabricSeasonsConfig {

    public FabricSeasonsConfig() {
    }

    public FabricSeasonsConfig(FabricSeasonsConfig other) {
        this.transitionDays = other.transitionDays;
        this.springCloudiness = other.springCloudiness;
        this.summerCloudiness = other.summerCloudiness;
        this.fallCloudiness = other.fallCloudiness;
        this.winterCloudiness = other.winterCloudiness;
    }

    @SerialEntry
    public float transitionDays = 4f;
    @SerialEntry
    public float springCloudiness = 1.0f;
    @SerialEntry
    public float summerCloudiness = 0.7f;
    @SerialEntry
    public float fallCloudiness = 1.0f;
    @SerialEntry
    public float winterCloudiness = 1.4f;
}
