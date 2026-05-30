package com.qendolin.betterclouds.mixin.required;

import com.qendolin.betterclouds.mixin.duck.WorldDuck;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;


@Mixin(value = Level.class, priority = 900)
public abstract class WorldMixin implements WorldDuck {

    @Shadow
    protected float oRainLevel;
    @Shadow
    protected float rainLevel;

    @Shadow
    protected float oThunderLevel;
    @Shadow
    protected float thunderLevel;

    @Unique
    public float betterclouds$getOriginalRainGradient(float delta) {
        float prev;
        prev = oThunderLevel;
        return Mth.lerp(delta, prev, this.thunderLevel) * this.betterclouds$getOriginalThunderGradient(delta);
    }

    @Unique
    public float betterclouds$getOriginalThunderGradient(float delta) {
        float prev;
        prev = oRainLevel;
        return Mth.lerp(delta, prev, this.rainLevel);
    }
}
