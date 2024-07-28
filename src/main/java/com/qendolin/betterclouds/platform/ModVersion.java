package com.qendolin.betterclouds.platform;

public abstract class ModVersion {
    public static final ModVersion NONE = new ModVersion() {
        @Override
        public String getFriendlyString() {
            return "unknown";
        }

        @Override
        public boolean isPresent() {
            return false;
        }
    };

    public abstract boolean isPresent();

    /**
     * Returns the user-friendly representation of this version.
     */
    public abstract String getFriendlyString();
}
