package com.qendolin.betterclouds.mixin.required;

import com.qendolin.betterclouds.duck.BiomeAccessDuck;
import net.minecraft.world.biome.source.BiomeAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BiomeAccess.class)
public abstract class BiomeAccessMixin implements BiomeAccessDuck {
    @Shadow
    @Final
    private long seed;

    @Unique
    public long better_clouds$biomeSeed() {
        return seed;
    }
}
