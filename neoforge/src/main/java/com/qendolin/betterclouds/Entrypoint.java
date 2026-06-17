package com.qendolin.betterclouds;

import com.qendolin.betterclouds.config.gui.ConfigGUI;
import com.qendolin.betterclouds.platform.EventHooks;
import com.qendolin.betterclouds.platform.neoforge.EventHooksImpl;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = BetterCloudsStatic.MODID, dist = Dist.CLIENT)
public final class Entrypoint {
    public Entrypoint(IEventBus modEventBus) {
        EventHooks.instance = new EventHooksImpl(modEventBus);

        BetterClouds.initializeClientEarly();
        BetterClouds.initializeClientEvents();

        modEventBus.addListener(AddClientReloadListenersEvent.class, _ -> BetterClouds.initializeClient());

        if (BetterCloudsStatic.IS_CLIENT) {
            ModLoadingContext.get().registerExtensionPoint(
                    IConfigScreenFactory.class,
                    () -> (_, parent) -> ConfigGUI.create(parent)
            );
        }
    }
}
