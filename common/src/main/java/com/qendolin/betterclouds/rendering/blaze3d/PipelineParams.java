package com.qendolin.betterclouds.rendering.blaze3d;

import com.qendolin.betterclouds.compat.*;
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.rendering.CloudRenderCoordinator;

public record PipelineParams(boolean celestialBodyHalo, boolean nearCloudFade, boolean iris,
                             boolean distantHorizons, boolean voxy, boolean zNeg1To1, boolean cloudsOpaque) {
    private static PipelineParams prevParams;

    public static PipelineParams get() {
        if (prevParams == null)
            prevParams = newParameters();
        return prevParams;
    }

    private static PipelineParams newParameters() {
        Config options = ConfigManager.instance();
        return new PipelineParams(
                options.celestialBodyHalo, options.nearCloudFade,
                IrisCompat.instance().isShadersEnabled(),
                DhCompat.instance().getDepthTexture() != null,
                VoxyCompat.instance.getOpaqueDepthTexture() != null,
                DhCompat.instance().isNativeRenderer(), // DH depth is [-1, 1] for ogl renderer but [0, 1] for b3d renderer
                Blaze3DRenderer.isCloudsOpaque()
        );
    }

    public static boolean paramsChanged() {
        if (CloudRenderCoordinator.instance.clientTicks % 5 != 0) return false;
        PipelineParams currentParams = newParameters();
        boolean changed = !currentParams.equals(prevParams);
        prevParams = currentParams;
        return changed;
    }
}