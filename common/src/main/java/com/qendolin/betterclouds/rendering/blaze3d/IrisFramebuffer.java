package com.qendolin.betterclouds.rendering.blaze3d;

import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.compat.IrisCompat;

import java.util.ArrayDeque;
import java.util.Deque;

public class IrisFramebuffer {
    private static final Deque<Runnable> bindings = new ArrayDeque<>();

    private IrisFramebuffer() {
    }

    public static void begin() {
        begin(() -> IrisCompat.instance().bindFramebuffer());
    }

    public static void begin(Runnable bindFramebuffer) {
        bindings.push(bindFramebuffer);
    }

    public static void bind() {
        bindings.element().run();
    }

    public static void end() {
        if (bindings.isEmpty()) {
            BetterCloudsStatic.getLogger().error("there are more draw ends than begins. UH OH FORCING TO ZERO");
            return;
        }

        bindings.pop();
    }

    public static boolean isActive() {
        return !bindings.isEmpty();
    }
}
