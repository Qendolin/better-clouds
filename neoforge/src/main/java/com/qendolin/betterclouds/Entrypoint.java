package com.qendolin.betterclouds;

import com.qendolin.betterclouds.platform.EventHooks;
import com.qendolin.betterclouds.platform.neoforge.EventHooksImpl;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(BetterCloudsStatic.MODID)
public final class Entrypoint {
    public Entrypoint(IEventBus modEventBus) {
        EventHooks.instance = new EventHooksImpl(modEventBus);

        BetterClouds.initializeClientEarly();
        BetterClouds.initializeClientEvents();

        modEventBus.addListener(FMLClientSetupEvent.class, _ -> Minecraft.getInstance().execute(BetterClouds::initializeClient));
    }
}
