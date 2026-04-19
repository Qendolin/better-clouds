package com.qendolin.betterclouds.mixin.required;

import com.qendolin.betterclouds.duck.BiomeManagerDuck;
import net.minecraft.world.level.biome.BiomeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BiomeManager.class)
public abstract class BiomeManagerMixin implements BiomeManagerDuck {
    @Shadow
    @Final
    private long biomeZoomSeed;

    @Unique
    public long better_clouds$biomeSeed() {
        return biomeZoomSeed;
    }
}
