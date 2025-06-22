package com.qendolin.betterclouds.mixin.required;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.6 {
import org.spongepowered.asm.mixin.gen.Accessor;
//?}

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    //? if >=1.21.6 {
    @Accessor
    net.minecraft.client.render.fog.FogRenderer getFogRenderer();
    //?}
}
