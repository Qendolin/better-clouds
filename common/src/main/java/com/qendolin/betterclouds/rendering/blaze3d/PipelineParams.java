package com.qendolin.betterclouds.rendering.blaze3d;

import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.compat.*;
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.config.ConfigManager;

public record PipelineParams(boolean celestialBodyHalo, boolean nearCloudFade, boolean iris,
                             boolean distantHorizons, boolean voxy, boolean reverseZ, boolean zNeg1To1,
                             boolean cloudsOpaque) {
    private static PipelineParams prevParams;

    public static PipelineParams get() {
        if (prevParams == null) {
            prevParams = newParameters();
            BetterCloudsStatic.getLogger().info("Initial pipeline parameters: {}", prevParams);
        }
        return prevParams;
    }

    private static PipelineParams newParameters() {
        Config options = ConfigManager.instance();
        return new PipelineParams(
                options.celestialBodyHalo, options.nearCloudFade,
                IrisCompat.instance.isShadersEnabled(),
                DhCompat.instance.getDepthTexture() != null,
                VoxyCompat.instance.getOpaqueDepthTexture() != null,
                Blaze3DRenderer.isReverseZ(),
                DhCompat.instance.getDepthTexture() != null ? DhCompat.instance.isZNeg1To1() : VoxyCompat.instance.isZNeg1To1(),
                Blaze3DRenderer.isCloudsOpaque()
        );
    }

    public static boolean paramsChanged() {
        PipelineParams currentParams = newParameters();
        boolean changed = !currentParams.equals(prevParams);
        prevParams = currentParams;
        return changed;
    }
}