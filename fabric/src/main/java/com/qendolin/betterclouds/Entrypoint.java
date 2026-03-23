package com.qendolin.betterclouds;

import com.qendolin.betterclouds.platform.EventHooks;
import com.qendolin.betterclouds.platform.fabric.EventHooksImpl;
import net.fabricmc.api.ClientModInitializer;

public final class Entrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EventHooks.instance = new EventHooksImpl();
        BetterClouds.initializeClientEarly();
        BetterClouds.initializeClientEvents();
        BetterClouds.initializeClient();
    }
}
