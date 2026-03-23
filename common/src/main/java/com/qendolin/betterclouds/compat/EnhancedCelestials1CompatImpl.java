package com.qendolin.betterclouds.compat;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EnhancedCelestials1CompatImpl extends EnhancedCelestialsSharedCompatImpl {

    private final Class<?> classWorldData;
    private final Method getLunarContext;
    private final Method getLunarForecast;
    private final LunarForecastMethods lunarForecastMethods;
    private final LunarEventMethods lunarEventMethods;
    private final ResourceKey<?> defaultLunarEvent;

    public EnhancedCelestials1CompatImpl(boolean devPackage) {
        try {
            String prefix = devPackage ? "dev." : "";
            classWorldData = Class.forName(prefix + "corgitaco.enhancedcelestials.EnhancedCelestialsWorldData");
            var classDefaultEvents = Class.forName(prefix + "corgitaco.enhancedcelestials.api.lunarevent.DefaultLunarEvents");
            var classLunarForecast = Class.forName(prefix + "corgitaco.enhancedcelestials.lunarevent.LunarForecast");
            var classContext = Class.forName(prefix + "corgitaco.enhancedcelestials.core.EnhancedCelestialsContext");
            var classLunarEvent = Class.forName(prefix + "corgitaco.enhancedcelestials.api.lunarevent.LunarEvent");
            var classClientSettings = Class.forName(prefix + "corgitaco.enhancedcelestials.api.lunarevent.client.LunarEventClientSettings");
            var classColorSettings = Class.forName(prefix + "corgitaco.enhancedcelestials.api.client.ColorSettings");
            getLunarContext = classWorldData.getMethod("getLunarContext");
            getLunarForecast = classContext.getMethod("getLunarForecast");

            defaultLunarEvent = (ResourceKey<?>) classDefaultEvents.getField("DEFAULT").get(null);

            lunarForecastMethods = new LunarForecastMethods(
                    classLunarForecast.getMethod("lastLunarEvent"),
                    classLunarForecast.getMethod("currentLunarEvent"),
                    classLunarForecast.getMethod("getBlend"),
                    classLunarForecast.getMethod("switchingEvents")
            );

            lunarEventMethods = new LunarEventMethods(
                    classLunarEvent.getMethod("getClientSettings"),
                    classClientSettings.getMethod("colorSettings"),
                    classColorSettings.getMethod("getGLSkyLightColor"),
                    classClientSettings.getMethod("moonSize")
            );
        } catch (NoSuchFieldException | NoSuchMethodException | ClassNotFoundException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected LunarForecastAccess getLunarForecast(Level world) {
        if (!classWorldData.isInstance(world)) {
            return null;
        }
        try {
            Object ctx = getLunarContext.invoke(world);
            if (ctx == null) return null;
            Object forecast = getLunarForecast.invoke(ctx);
            return new LunarForecastAccessImpl(forecast, lunarForecastMethods, lunarEventMethods);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Your versions of Better Clouds and EnhancedCelestials are not compatible!", e);
        }
    }

    @Override
    protected Identifier defaultLunarEvent() {
        return defaultLunarEvent.identifier();
    }

    private record LunarForecastMethods(
            Method lastLunarEvent,
            Method currentLunarEvent,
            Method getBlend,
            Method switchingEvents
    ) {
    }

    private record LunarEventMethods(
            Method getClientSettings,
            Method colorSettings,
            Method getGLSkyLightColor,
            Method moonSize
    ) {
    }

    private static class LunarForecastAccessImpl extends LunarForecastAccess {
        private final Object instance;
        private final LunarForecastMethods methods;
        private final LunarEventMethods eventMethods;

        public LunarForecastAccessImpl(Object instance, LunarForecastMethods methods, LunarEventMethods eventMethods) {
            this.instance = instance;
            this.methods = methods;
            this.eventMethods = eventMethods;
        }

        @Override
        public LunarEventAccess lastLunarEvent() {
            return new LunarEventAccessImpl(invoke(instance, methods.lastLunarEvent), eventMethods);
        }

        @Override
        public LunarEventAccess currentLunarEvent() {
            return new LunarEventAccessImpl(invoke(instance, methods.currentLunarEvent), eventMethods);
        }

        @Override
        public float getBlend() {
            return invoke(instance, methods.getBlend);
        }

        @Override
        public boolean switchingEvents() {
            return invoke(instance, methods.switchingEvents);
        }
    }

    private static class LunarEventAccessImpl extends LunarEventAccess {
        private final Holder<?> entry;
        private final LunarEventMethods methods;

        private LunarEventAccessImpl(Holder<?> entry, LunarEventMethods methods) {
            this.entry = entry;
            this.methods = methods;
        }

        private Object getClientSettings() {
            return invoke(entry.value(), methods.getClientSettings);

        }

        @Override
        public Vector3f getGLSkyLightColor() {
            var clientSettings = getClientSettings();
            var colorSettings = invoke(clientSettings, methods.colorSettings);
            return invoke(colorSettings, methods.getGLSkyLightColor);
        }

        @Override
        public float getMoonSize() {
            var clientSettings = getClientSettings();
            return invoke(clientSettings, methods.moonSize);
        }

        @Override
        protected boolean matches(Identifier id) {
            return entry.is(id);
        }
    }
}
