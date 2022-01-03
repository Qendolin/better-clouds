package com.qendolin.betterclouds.config;

public interface ModConfig {
    String getId();

    /**
     * @return The file name and path without the extension
     */
    default String getFileName() {
        return this.getId();
    }
    int getVersion();
}
