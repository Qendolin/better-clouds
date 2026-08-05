package com.qendolin.betterclouds.rendering.blaze3d;

import com.qendolin.betterclouds.BetterCloudsStatic;

public class IrisFramebuffer {
    private static int activeDraws;

    private IrisFramebuffer() {
    }

    public static void begin() {
        activeDraws++;
    }

    public static void end() {
        if (activeDraws <= 0) {
            BetterCloudsStatic.getLogger().error("there are more draw ends than begins. UH OH FORCING TO ZERO");
            activeDraws = 1;
        }

        activeDraws--;
    }

    public static boolean isActive() {
        return activeDraws > 0;
    }
}
