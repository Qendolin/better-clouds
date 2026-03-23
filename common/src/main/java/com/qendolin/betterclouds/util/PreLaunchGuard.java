package com.qendolin.betterclouds.util;

import com.qendolin.betterclouds.platform.ModLoader;

import java.lang.reflect.Method;

public interface PreLaunchGuard {

    static void check() {
        if (!ModLoader.isDevelopmentEnvironment())
            return;
        try {
            Class<?> clazz = Class.forName("com.qendolin.betterclouds.platform.fabric.Crash_Is_Not_Caused_By_BetterClouds");
            Method method = clazz.getMethod("isPreLaunchComplete");
            boolean complete = (boolean) method.invoke(null);
            if (!complete) {
                throw new AssertionError("Class loaded before pre launch phase was complete!");
            }
        } catch (ClassNotFoundException ignored) {
            // Not running on Fabric or the prelaunch hook isn't available.
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to verify prelaunch state", e);
        }
    }
}
