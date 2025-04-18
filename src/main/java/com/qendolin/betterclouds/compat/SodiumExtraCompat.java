package com.qendolin.betterclouds.compat;

public class SodiumExtraCompat {

    public static final ThreadLocal<Boolean> PREVENT_FOG_MODIFICATION = ThreadLocal.withInitial(() -> false);

}
