package com.qendolin.betterclouds.compat;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;

public abstract class BigGlobeCompat {

    public static final RegistryKey<DimensionType> DIMENSION_KEY = RegistryKey.of(RegistryKeys.DIMENSION_TYPE, Identifier.of("bigglobe", "overworld"));
}
