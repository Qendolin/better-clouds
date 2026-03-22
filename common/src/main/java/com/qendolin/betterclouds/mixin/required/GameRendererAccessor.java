package com.qendolin.betterclouds.mixin.required;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Accessor
    net.minecraft.client.renderer.fog.FogRenderer getFogRenderer();
}
