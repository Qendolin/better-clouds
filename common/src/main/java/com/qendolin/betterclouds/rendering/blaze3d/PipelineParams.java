package com.qendolin.betterclouds.rendering.blaze3d;

import com.qendolin.betterclouds.compat.*;
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.rendering.CloudRenderCoordinator;

public record PipelineParams(boolean celestialBodyHalo, boolean nearCloudFade, boolean iris,
                             boolean distantHorizons, boolean voxy, boolean zNeg1To1, boolean cloudsOpaque) {
    public static PipelineParams prevParams;

    public static PipelineParams getParameters() {
        Config options = ConfigManager.instance();
        return new PipelineParams(
                options.celestialBodyHalo, options.nearCloudFade,
                IrisCompat.instance().isShadersEnabled(),
                DhCompat.instance().getDepthTexture() != null,
                VoxyCompat.instance.getOpaqueDepthTexture() != null,
                DhCompat.instance().isZNeg1To1(),
                Blaze3DRenderer.isCloudsOpaque());
    }

    public static boolean paramsChanged() {
        if (CloudRenderCoordinator.instance.clientTicks % 5 != 0) return false;
        PipelineParams currentParams = PipelineParams.getParameters();
        boolean changed = !currentParams.equals(prevParams);
        prevParams = currentParams;
        return changed;
    }
}