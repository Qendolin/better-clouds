package com.qendolin.betterclouds.clouds.shaders;

import net.minecraft.client.option.CloudRenderMode;

public record ShaderParameters(
    CloudRenderMode cloudRenderMode,
    int blockViewDistance,
    float configSizeXZ,
    float configSizeY,
    boolean configCelestialBodyHalo,
    boolean useDepthWriteFallback,
    boolean useStencilTextureFallback,
    boolean useDistantHorizonsCompat,
    boolean isReverseZ,
    boolean isZNeg1To1,
    int worldCurvatureSize
) {
}
