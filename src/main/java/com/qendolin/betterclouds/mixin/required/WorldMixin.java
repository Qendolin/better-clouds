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
    //? if >=1.21.5 {
    /*protected float lastRainGradient;
    *///?} else {
    protected float rainGradientPrev;
    //?}
    @Shadow
    protected float rainGradient;

    @Shadow
    //? if >=1.21.5 {
    /*protected float lastThunderGradient;
    *///?} else {
    protected float thunderGradientPrev;
    //?}
    @Shadow
    protected float thunderGradient;

    @Unique
    public float betterclouds$getOriginalRainGradient(float delta) {
        float prev;
        //? if >=1.21.5 {
        /*prev = lastThunderGradient;
        *///?} else {
        prev = thunderGradientPrev;
         //?}
        return MathHelper.lerp(delta, prev, this.thunderGradient) * this.betterclouds$getOriginalThunderGradient(delta);
    }

    @Unique
    public float betterclouds$getOriginalThunderGradient(float delta) {
        float prev;
        //? if >=1.21.5 {
        /*prev = lastRainGradient;
        *///?} else {
        prev = rainGradientPrev;
         //?}
        return MathHelper.lerp(delta, prev, this.rainGradient);
    }
}
