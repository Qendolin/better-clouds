package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.platform.ModLoader;
import me.cortex.voxy.client.config.VoxyConfig;

public class VoxyCompat {

    public static final boolean IS_LOADED = ModLoader.isModLoaded("voxy");

    public static int getRenderDistance() {
        if(!IS_LOADED) return 0;
        if(VoxyConfig.CONFIG.enabled) return 0;
        return VoxyConfig.CONFIG.renderDistance;
    }

}
