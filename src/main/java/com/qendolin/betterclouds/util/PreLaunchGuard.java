package com.qendolin.betterclouds.util;

import com.qendolin.betterclouds.platform.ModLoader;
import com.qendolin.betterclouds.test.GameTestEnabled;

public interface PreLaunchGuard {

    static void check() {
        if(!(ModLoader.isDevelopmentEnvironment() || GameTestEnabled.ENABLED))
            return;
        //? fabric {
        // Check if this class is loaded to early
        if (!com.qendolin.betterclouds.platform.fabric.Crash_Is_Not_Caused_By_BetterClouds.isPreLaunchComplete()) {
            throw new AssertionError("Class loaded before pre launch phase was complete!");
        }
        //?}
    }
}
