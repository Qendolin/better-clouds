package com.qendolin.betterclouds.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;

public abstract class MiddleEarthCompat {

    public static final ResourceKey<DimensionType> DIMENSION_KEY = ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath("me", "middle_earth_type"));
}
