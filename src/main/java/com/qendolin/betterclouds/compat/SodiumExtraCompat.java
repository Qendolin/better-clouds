package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.platform.ModLoader;

import java.util.concurrent.atomic.AtomicBoolean;

public class SodiumExtraCompat {
    public static final boolean IS_LOADED = ModLoader.isModLoaded("sodium-extra");

    public static final ThreadLocal<Boolean> PREVENT_FOG_MODIFICATION = ThreadLocal.withInitial(() -> false);

}
