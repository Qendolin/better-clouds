package com.qendolin.betterclouds.compat;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public class EnhancedCelestials2CompatImpl extends EnhancedCelestialsSharedCompatImpl {
    @Override
    protected LunarForecastAccess getLunarForecast(Level world) {
        return null;
    }

    @Override
    protected Identifier defaultLunarEvent() {
        return Identifier.withDefaultNamespace("none");
    }
}
