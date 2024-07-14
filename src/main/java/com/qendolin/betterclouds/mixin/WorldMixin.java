package com.qendolin.betterclouds.mixin;

import com.qendolin.betterclouds.compat.WorldDuck;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;


@Mixin(value = World.class, priority = 900)
public abstract class WorldMixin implements WorldDuck {

    @Shadow
    protected float rainGradientPrev;
    @Shadow
    protected float rainGradient;
    @Shadow
    protected float thunderGradientPrev;
    @Shadow
    protected float thunderGradient;

    @Unique
    public float betterclouds$getOriginalRainGradient(float delta) {
        return MathHelper.lerp(delta, this.thunderGradientPrev, this.thunderGradient) * this.betterclouds$getOriginalThunderGradient(delta);
    }

    @Unique
    public float betterclouds$getOriginalThunderGradient(float delta) {
        return MathHelper.lerp(delta, this.rainGradientPrev, this.rainGradient);
    }
}
