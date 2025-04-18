package com.qendolin.betterclouds.compat;

import dev.corgitaco.enhancedcelestials.EnhancedCelestials;
import dev.corgitaco.enhancedcelestials.api.lunarevent.DefaultLunarEvents;
import dev.corgitaco.enhancedcelestials.api.lunarevent.LunarEvent;
import dev.corgitaco.enhancedcelestials.lunarevent.EnhancedCelestialsLunarForecastWorldData;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.joml.Vector3f;

public class EnhancedCelestials2CompatImpl extends EnhancedCelestialsSharedCompatImpl {

    private static class LunarForecastAccessImpl extends LunarForecastAccess {

        private final EnhancedCelestialsLunarForecastWorldData data;

        public LunarForecastAccessImpl(EnhancedCelestialsLunarForecastWorldData data) {
            this.data = data;
        }

        @Override
        public LunarEventAccess lastLunarEvent() {
            return new LunarEventAccessImpl(data.lastLunarEventHolder());
        }

        @Override
        public LunarEventAccess currentLunarEvent() {
            return new LunarEventAccessImpl(data.currentLunarEventHolder());
        }

        @Override
        public float getBlend() {
            return data.getBlend();
        }

        @Override
        public boolean switchingEvents() {
            return data.switchingEvents();
        }
    }

    private static class LunarEventAccessImpl extends LunarEventAccess {

        private final RegistryEntry<LunarEvent> event;

        private LunarEventAccessImpl(RegistryEntry<LunarEvent> event) {
            this.event = event;
        }

        @Override
        public Vector3f getGLSkyLightColor() {
            return event.value().getClientSettings().colorSettings().getGLSkyLightColor();
        }

        @Override
        public float getMoonSize() {
            return event.value().getClientSettings().moonSize();
        }

        @Override
        protected boolean matches(Identifier id) {
            return event.matchesId(id);
        }
    }

    @Override
    protected LunarForecastAccess getLunarForecast(World world) {
        return EnhancedCelestials.lunarForecastWorldData(world)
            .map(LunarForecastAccessImpl::new).orElse(null);
    }

    @Override
    protected Identifier defaultLunarEvent() {
        return DefaultLunarEvents.DEFAULT.getValue();
    }
}
