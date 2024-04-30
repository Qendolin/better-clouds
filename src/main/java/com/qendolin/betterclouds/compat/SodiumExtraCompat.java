package com.qendolin.betterclouds.compat;

import net.fabricmc.loader.api.FabricLoader;

public class SodiumExtraCompat {
    public static final boolean IS_LOADED = FabricLoader.getInstance().isModLoaded("sodium-extra");

}
