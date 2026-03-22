package com.qendolin.betterclouds.compat;

import net.minecraft.world.level.Level;

public class FabricSeasonsCompatImpl extends FabricSeasonsCompat {
    @Override
    public float getCloudinessFactor(Level world) {
        return 1.0f;
    }
}
