package com.qendolin.betterclouds;

import com.qendolin.betterclouds.config.ConfigGUI;
import com.qendolin.betterclouds.platform.EventHooks;
import com.qendolin.betterclouds.platform.neoforge.EventHooksImpl;
import net.minecraft.client.MinecraftClient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(BetterCloudsStatic.MODID)
public final class Entrypoint {
    public Entrypoint(IEventBus modEventBus) {
        EventHooks.instance = new EventHooksImpl(modEventBus);

        BetterClouds.initializeClientEarly();
        BetterClouds.initializeClientEvents();

        modEventBus.addListener(FMLClientSetupEvent.class, event -> {
            MinecraftClient.getInstance().execute(BetterClouds::initializeClient);

            ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class,
                () -> (modContainer, parent) -> ConfigGUI.create(parent));
        });
    }
}
