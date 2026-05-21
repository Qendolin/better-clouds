package com.qendolin.betterclouds;

import com.qendolin.betterclouds.platform.EventHooks;

//? if fabric {
import net.fabricmc.api.ClientModInitializer;
import com.qendolin.betterclouds.platform.fabric.EventHooksImpl;

public final class Entrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EventHooks.instance = new EventHooksImpl();
        BetterClouds.initializeClientEarly();
        BetterClouds.initializeClientEvents();
        BetterClouds.initializeClient();
    }

}
//?} elif neoforge {
/*import com.qendolin.betterclouds.platform.neoforge.EventHooksImpl;
import com.qendolin.betterclouds.config.ConfigGUI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.function.BiFunction;

@Mod(BetterClouds.MODID)
public final class Entrypoint {
    public Entrypoint(IEventBus modEventBus) {
        EventHooks.instance = new EventHooksImpl(modEventBus);

        BetterClouds.initializeClientEarly();
        BetterClouds.initializeClientEvents();

        modEventBus.addListener(FMLClientSetupEvent.class, event -> {
            MinecraftClient.getInstance().execute(BetterClouds::initializeClient);

            //? if <1.20.6 {
            /^ModLoadingContext.get().registerExtensionPoint(net.neoforged.neoforge.client.ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new net.neoforged.neoforge.client.ConfigScreenHandler.ConfigScreenFactory(
                    (client, parent) -> ConfigGUI.create(parent)));
            ^///?} else {
            ModLoadingContext.get().registerExtensionPoint(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
                () -> (modContainer, parent) -> ConfigGUI.create(parent));
            //?}
        });
    }
}
*///?}
