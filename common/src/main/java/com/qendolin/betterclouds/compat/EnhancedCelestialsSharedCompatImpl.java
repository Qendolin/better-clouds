package com.qendolin.betterclouds.compat;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public abstract class EnhancedCelestialsSharedCompatImpl extends EnhancedCelestialsCompat {

    protected abstract LunarForecastAccess getLunarForecast(Level world);

    protected abstract Identifier defaultLunarEvent();

    @Override
    public Vector3f getEventTint(Level world) {
        var forecast = getLunarForecast(world);
        if (forecast == null)
            return null;

        var lastEvent = forecast.lastLunarEvent();
        var currEvent = forecast.currentLunarEvent();

        var lastColor = lastEvent.getGLSkyLightColor();
        var currColor = currEvent.getGLSkyLightColor();

        float blend = Mth.clamp(forecast.getBlend(), 0.0f, 1.0f);

        var delta = currColor.sub(lastColor, new Vector3f());
        return lastColor.add(delta.mul(blend), new Vector3f());
    }

    @Override
    public boolean isEventActive(Level world) {
        var forecast = getLunarForecast(world);
        if (forecast == null)
            return false;

        var lastKey = forecast.lastLunarEvent();
        var currKey = forecast.currentLunarEvent();

        float blend = forecast.getBlend();

        boolean lastActive = !lastKey.matches(defaultLunarEvent()) && forecast.switchingEvents();
        boolean currActive = !currKey.matches(defaultLunarEvent()) && blend > 0.0;

        return lastActive || currActive;
    }

    @Override
    public float getMoonSize(Level world) {
        var forecast = getLunarForecast(world);
        if (forecast == null)
            return 1.0f;

        var lastEvent = forecast.lastLunarEvent();
        var currEvent = forecast.currentLunarEvent();

        float lastSize = lastEvent.getMoonSize();
        float currSize = currEvent.getMoonSize();

        return Mth.clampedLerp(lastSize, currSize, forecast.getBlend()) / 20.0f;
    }

    protected static abstract class LunarForecastAccess extends ReflectAccess {
        public LunarForecastAccess() {
            super("Enhanced Celestials");
        }

        public abstract LunarEventAccess lastLunarEvent();

        public abstract LunarEventAccess currentLunarEvent();

        public abstract float getBlend();

        public abstract boolean switchingEvents();

    }

    protected static abstract class LunarEventAccess extends ReflectAccess {
        public LunarEventAccess() {
            super("Enhanced Celestials");
        }

        public abstract Vector3f getGLSkyLightColor();

        public abstract float getMoonSize();

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        protected abstract boolean matches(Identifier id);

    }
}
