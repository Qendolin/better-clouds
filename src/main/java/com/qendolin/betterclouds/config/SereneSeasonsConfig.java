package com.qendolin.betterclouds.config;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class SereneSeasonsConfig {

    public SereneSeasonsConfig() {}

    public SereneSeasonsConfig(SereneSeasonsConfig other) {
        this.earlySpringCloudiness = other.earlySpringCloudiness;
        this.midSpringCloudiness = other.midSpringCloudiness;
        this.lateSpringCloudiness = other.lateSpringCloudiness;
        this.earlySummerCloudiness = other.earlySummerCloudiness;
        this.midSummerCloudiness = other.midSummerCloudiness;
        this.lateSummerCloudiness = other.lateSummerCloudiness;
        this.earlyAutumnCloudiness = other.earlyAutumnCloudiness;
        this.midAutumnCloudiness = other.midAutumnCloudiness;
        this.lateAutumnCloudiness = other.lateAutumnCloudiness;
        this.earlyWinterCloudiness = other.earlyWinterCloudiness;
        this.midWinterCloudiness = other.midWinterCloudiness;
        this.lateWinterCloudiness = other.lateWinterCloudiness;
    }

    @SerialEntry
    public float earlySpringCloudiness = 1.2f;
    @SerialEntry
    public float midSpringCloudiness = 1.0f;
    @SerialEntry
    public float lateSpringCloudiness = 0.9f;
    @SerialEntry
    public float earlySummerCloudiness = 0.8f;
    @SerialEntry
    public float midSummerCloudiness = 0.7f;
    @SerialEntry
    public float lateSummerCloudiness = 0.8f;
    @SerialEntry
    public float earlyAutumnCloudiness = 0.9f;
    @SerialEntry
    public float midAutumnCloudiness = 1.0f;
    @SerialEntry
    public float lateAutumnCloudiness = 1.2f;
    @SerialEntry
    public float earlyWinterCloudiness = 1.3f;
    @SerialEntry
    public float midWinterCloudiness = 1.4f;
    @SerialEntry
    public float lateWinterCloudiness = 1.3f;
}
