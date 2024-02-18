package com.qendolin.betterclouds.compat;

import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.methods.events.DhApiEventRegister;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiAfterDhInitEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiAfterRenderEvent;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiEventParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import net.fabricmc.loader.api.FabricLoader;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class DistantHorizonsCompat {
    public static final boolean IS_LOADED = FabricLoader.getInstance().isModLoaded("distanthorizons");
    // Used when DH is enabled, but some other issue prevents it from working as intended
    // The matrix just maps everything to the near plane
    public static final Matrix4f NOOP_MATRIX = new Matrix4f(
        new Vector4f(0, 0, 0, 0),
        new Vector4f(0, 0, 0, 0),
        new Vector4f(0, 0, 0, 0),
        new Vector4f(0, 0, -1, 1)
    );

    private static boolean isInitialized = false;
    private static boolean isDhInitialized = false;
    private static DhApiRenderParam lastRenderParam = null;

    public static void initialize() {
        if(isInitialized) return;
        isInitialized = true;
        // Lambdas didn't work
        DhApiEventRegister.on(DhApiAfterDhInitEvent.class, new DhApiAfterDhInitEventHandler());
        DhApiEventRegister.on(DhApiAfterRenderEvent.class, new DhApiAfterRenderEventHandler());
    }

    public static boolean isReady() {
        return IS_LOADED && isDhInitialized && lastRenderParam != null;
    }

    public static boolean isEnabled() {
        return DhApi.Delayed.configs.graphics().renderingEnabled().getValue();
    }

    public static DhApiRenderParam getRenderParams() {
        return lastRenderParam;
    }

    private static class DhApiAfterDhInitEventHandler extends DhApiAfterDhInitEvent {
        @Override
        public void afterDistantHorizonsInit(DhApiEventParam<Void> input) {
            isDhInitialized = true;
        }
    }

    private static class DhApiAfterRenderEventHandler extends DhApiAfterRenderEvent {
        @Override
        public void afterRender(DhApiEventParam<EventParam> input) {
            lastRenderParam = input.value;
        }
    }
}
