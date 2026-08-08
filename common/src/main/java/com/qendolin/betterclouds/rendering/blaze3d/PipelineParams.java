package com.qendolin.betterclouds.rendering.blaze3d;

import com.qendolin.betterclouds.compat.*;
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.config.ConfigManager;

public record PipelineParams(boolean celestialBodyHalo, boolean nearCloudFade, boolean iris,
                             boolean distantHorizons, boolean voxy, boolean faceCulling) {
    public static PipelineParams prevParams;

    public static PipelineParams getParameters() {
        Config options = ConfigManager.instance();
        return new PipelineParams(
                options.celestialBodyHalo, options.nearCloudFade,
                IrisCompat.instance().isShadersEnabled(),
                DistantHorizonsCompat.instance().getDepthTexture() != null,
                VoxyCompat.instance.getOpaqueDepthTexture() != null,
                Blaze3DRenderer.isCloudsOpaque());
    }

    public static boolean paramsChanged() {
        PipelineParams currentParams = PipelineParams.getParameters();
        boolean changed = !currentParams.equals(prevParams);
        prevParams = currentParams;
        return changed;
    }
}