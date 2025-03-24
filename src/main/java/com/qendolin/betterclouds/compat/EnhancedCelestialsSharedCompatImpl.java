package com.qendolin.betterclouds.compat;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.joml.Vector3f;

public abstract class EnhancedCelestialsSharedCompatImpl extends EnhancedCelestialsCompat {

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

        protected abstract boolean matches(Identifier id);

    }

    protected abstract LunarForecastAccess getLunarForecast(World world);

    protected abstract Identifier defaultLunarEvent();


    @Override
    public Vector3f getEventTint(World world) {
        var forecast = getLunarForecast(world);
        if(forecast == null)
            return null;

        var lastEvent = forecast.lastLunarEvent();
        var currEvent = forecast.currentLunarEvent();

        var lastColor = lastEvent.getGLSkyLightColor();
        var currColor = currEvent.getGLSkyLightColor();

        float blend = MathHelper.clamp(forecast.getBlend(), 0.0f, 1.0f);

        var delta = currColor.sub(lastColor, new Vector3f());
        return lastColor.add(delta.mul(blend), new Vector3f());
    }

    @Override
    public boolean isEventActive(World world) {
        var forecast = getLunarForecast(world);
        if(forecast == null)
            return false;

        var lastKey = forecast.lastLunarEvent();
        var currKey = forecast.currentLunarEvent();

        float blend = forecast.getBlend();

        boolean lastActive = !lastKey.matches(defaultLunarEvent()) && forecast.switchingEvents();
        boolean currActive = !currKey.matches(defaultLunarEvent()) && blend > 0.0;

        return lastActive || currActive;
    }

    @Override
    public float getMoonSize(World world) {
        var forecast = getLunarForecast(world);
        if(forecast == null)
            return 1.0f;

        var lastEvent = forecast.lastLunarEvent();
        var currEvent = forecast.currentLunarEvent();

        float lastSize = lastEvent.getMoonSize();
        float currSize = currEvent.getMoonSize();

        return MathHelper.clampedLerp(lastSize, currSize, forecast.getBlend()) / 20.0f;
    }
}
