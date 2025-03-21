package com.qendolin.betterclouds.compat;

//? <1.21 {
/*import corgitaco.enhancedcelestials.EnhancedCelestialsWorldData;
import corgitaco.enhancedcelestials.api.lunarevent.DefaultLunarEvents;
import corgitaco.enhancedcelestials.lunarevent.LunarForecast;
*///?} else {
import dev.corgitaco.enhancedcelestials.EnhancedCelestialsWorldData;
import dev.corgitaco.enhancedcelestials.api.lunarevent.DefaultLunarEvents;
import dev.corgitaco.enhancedcelestials.lunarevent.LunarForecast;
//?}

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.joml.Vector3f;

public class EnhancedCelestialsCompatImpl extends EnhancedCelestialsCompat{

    private LunarForecast getLastLunarForecast(World world) {
        if(!(world instanceof EnhancedCelestialsWorldData ecwd)) {
            return null;
        }
        var ctx = ecwd.getLunarContext();
        if(ctx == null) return null;
        return ctx.getLunarForecast();
    }

    @Override
    public Vector3f getEventTint(World world) {
        var forecast = getLastLunarForecast(world);
        if(forecast == null)
            return null;

        var lastKey = forecast.lastLunarEvent();
        var currKey = forecast.currentLunarEvent();

        var lastColor = lastKey.value().getClientSettings().colorSettings().getGLSkyLightColor();
        var currColor = currKey.value().getClientSettings().colorSettings().getGLSkyLightColor();

        float blend = MathHelper.clamp(forecast.getBlend(), 0.0f, 1.0f);

        var delta = currColor.sub(lastColor, new Vector3f());
        return lastColor.add(delta.mul(blend), new Vector3f());
    }

    @Override
    public boolean isEventActive(World world) {
        var forecast = getLastLunarForecast(world);
        if(forecast == null)
            return false;

        var lastKey = forecast.lastLunarEvent();
        var currKey = forecast.currentLunarEvent();

        float blend = forecast.getBlend();

        boolean lastActive = !lastKey.matchesKey(DefaultLunarEvents.DEFAULT) && forecast.switchingEvents();
        boolean currActive = !currKey.matchesKey(DefaultLunarEvents.DEFAULT) && blend > 0.0;

        return lastActive || currActive;
    }

    @Override
    public float getMoonSize(World world) {
        var forecast = getLastLunarForecast(world);
        if(forecast == null)
            return 1.0f;

        var lastKey = forecast.lastLunarEvent();
        var currKey = forecast.currentLunarEvent();

        float lastSize = lastKey.value().getClientSettings().moonSize();
        float currSize = currKey.value().getClientSettings().moonSize();

        return MathHelper.clampedLerp(lastSize, currSize, forecast.getBlend()) / 20.0f;
    }
}
