package com.qendolin.betterclouds.mixin.optional;

import mod.lwhrvw.astrocraft.planets.Body;
import mod.lwhrvw.astrocraft.planets.PlanetManager;
import mod.lwhrvw.astrocraft.planets.StarBody;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlanetManager.class)
public interface AstrocraftPlanetManagerAccessor {
    @Accessor
    static StarBody getSun() {
        return null;
    }

    @Accessor
    static Body getObsMount() {
        return null;
    }

}
