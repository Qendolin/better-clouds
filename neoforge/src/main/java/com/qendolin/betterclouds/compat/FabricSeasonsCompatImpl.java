package com.qendolin.betterclouds.compat;

import net.minecraft.world.World;

public class FabricSeasonsCompatImpl extends FabricSeasonsCompat.Stub {
    @Override
    public float getCloudinessFactor(World world) {
        return 1.0f;
    }
}
