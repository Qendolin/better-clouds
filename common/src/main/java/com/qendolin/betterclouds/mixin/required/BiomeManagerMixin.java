package com.qendolin.betterclouds.mixin.required;

import com.qendolin.betterclouds.mixin.duck.BiomeManagerDuck;
import net.minecraft.world.level.biome.BiomeManager;
import org.spongepowered.asm.mixin.*;

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
