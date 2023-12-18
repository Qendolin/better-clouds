package com.qendolin.betterclouds.compat;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.fabricmc.loader.api.FabricLoader;

public class SodiumExtraCompat {
    public static final boolean IS_LOADED = FabricLoader.getInstance().isModLoaded("sodium-extra");

}
