package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.platform.ModLoader;

// These constants may be used by mixin plugins and are here to avoid loading more classes than needed
public abstract class ModLoaded {
    public static final boolean DISTANT_HORIZONS = ModLoader.isModLoaded("distanthorizons");
    public static final boolean ENHANCED_CELESTIALS = ModLoader.isModLoaded("enhancedcelestials");
    public static final boolean FABRIC_SEASONS = ModLoader.isModLoaded("seasons");
    public static final boolean IRIS = ModLoader.isModLoaded("iris") || ModLoader.isModLoaded("oculus");
    public static final boolean HEAD_IN_THE_CLOUDS = ModLoader.isModLoaded("head_in_the_clouds");
    public static final boolean SERENE_SEASONS = ModLoader.isModLoaded("sereneseasons");
    public static final boolean SODIUM_EXTRA = ModLoader.isModLoaded("sodium-extra");
    public static final boolean ARS_NOUVEAU = ModLoader.isModLoaded("ars_nouveau");
}
