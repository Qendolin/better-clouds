package com.qendolin.betterclouds;

import com.qendolin.betterclouds.gui.ConfigScreen;
import com.qendolin.betterclouds.platform.EventHooks;

//? if fabric {
import net.fabricmc.api.ClientModInitializer;
import com.qendolin.betterclouds.platform.fabric.EventHooksImpl;

public final class Entrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EventHooks.instance = new EventHooksImpl();

        Main.initializeClientEvents();
        Main.initializeClient();
    }
}
//?} elif neoforge {
/*import com.qendolin.betterclouds.platform.neoforge.EventHooksImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.function.BiFunction;

@Mod(Main.MODID)
public final class Entrypoint {
    public Entrypoint(IEventBus modEventBus) {
        EventHooks.instance = new EventHooksImpl(modEventBus);

        Main.initializeClientEvents();

        modEventBus.addListener(FMLClientSetupEvent.class, event -> {
            MinecraftClient.getInstance().execute(Main::initializeClient);

            //? if <1.20.6 {
            ModLoadingContext.get().registerExtensionPoint(net.neoforged.neoforge.client.ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new net.neoforged.neoforge.client.ConfigScreenHandler.ConfigScreenFactory(
                    (client, parent) -> ConfigGUI.create(parent)));
            //?} else {
            /^ModLoadingContext.get().registerExtensionPoint(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
                () -> (modContainer, parent) -> ConfigGUI.create(parent));
            ^///?}
        });
    }
}
*///?}