package com.qendolin.betterclouds.compat;

public class ArsNouveauCompat {
    private static final int STACK_DEPTH = 10;
    public static boolean isArsRendering() {
        if (!ModLoaded.ARS_NOUVEAU) {
            return false;
        }
        StackWalker instance = StackWalker.getInstance();
        return instance.walk(stream -> stream.limit(STACK_DEPTH).anyMatch(s -> s.getClassName().equals("com.hollingsworth.arsnouveau.client.SkyTextureHandler")));
    }
}
