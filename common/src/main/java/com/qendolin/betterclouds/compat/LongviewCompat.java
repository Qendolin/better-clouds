package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.BetterCloudsStatic;

public abstract class LongviewCompat {
    public static LongviewCompat instance;

    public static void initialize() {
        if (instance != null) return;

        if (!ModLoaded.LONGVIEW) {
            BetterCloudsStatic.getLogger().info("Longview is not loaded");
            instance = new Stub();
            return;
        }

        instance = new LongviewCompatImpl();
    }

    public abstract boolean isReverseZ();

    public abstract boolean isZClipped();

    public static class Stub extends LongviewCompat {
        @Override
        public boolean isReverseZ() {
            return false;
        }

        @Override
        public boolean isZClipped() {
            return false;
        }
    }
}
