package com.qendolin.betterclouds.clouds.shaders;

import net.minecraft.client.CloudStatus;

public record ShaderParameters(
    CloudStatus cloudRenderMode,
    int blockViewDistance,
    float configSizeXZ,
    float configSizeY,
    boolean configCelestialBodyHalo,
    boolean useDepthWriteFallback,
    boolean useStencilTextureFallback,
    boolean useDistantHorizonsCompat,
    int worldCurvatureSize
) {
}
