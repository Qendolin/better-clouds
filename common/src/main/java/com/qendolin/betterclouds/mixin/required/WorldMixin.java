package com.qendolin.betterclouds.mixin.required;

import com.qendolin.betterclouds.compat.WorldDuck;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;


@Mixin(value = World.class, priority = 900)
public abstract class WorldMixin implements WorldDuck {

    @Shadow
    protected float lastRainGradient;
    @Shadow
    protected float rainGradient;

    @Shadow
    protected float lastThunderGradient;
    @Shadow
    protected float thunderGradient;

    @Unique
    public float betterclouds$getOriginalRainGradient(float delta) {
        float prev;
        prev = lastThunderGradient;
        return MathHelper.lerp(delta, prev, this.thunderGradient) * this.betterclouds$getOriginalThunderGradient(delta);
    }

    @Unique
    public float betterclouds$getOriginalThunderGradient(float delta) {
        float prev;
        prev = lastRainGradient;
        return MathHelper.lerp(delta, prev, this.rainGradient);
    }
}
