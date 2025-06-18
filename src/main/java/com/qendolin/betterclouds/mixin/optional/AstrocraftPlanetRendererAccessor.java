package com.qendolin.betterclouds.mixin.optional;

import mod.lwhrvw.astrocraft.planets.PlanetRenderer;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlanetRenderer.class)
public interface AstrocraftPlanetRendererAccessor {

    @Invoker
    public static Vector3d invokeGetAngularPosition(Vector3d direction) {
        return null;
    }
}
