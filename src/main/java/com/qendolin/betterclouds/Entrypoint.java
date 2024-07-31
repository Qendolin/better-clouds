package com.qendolin.betterclouds;

//? if fabric {
/*import net.fabricmc.api.ClientModInitializer;
import com.qendolin.betterclouds.platform.fabric.EventHooksImpl;

public final class Entrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EventHooks.instance = new EventHooksImpl(modEventBus);

        Main.initializeClientEvents();
        Main.initializeClient();
    }
}
*///?} elif neoforge {
import com.qendolin.betterclouds.platform.EventHooks;
import com.qendolin.betterclouds.platform.neoforge.EventHooksImpl;
import net.minecraft.client.MinecraftClient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(Main.MODID)
public final class Entrypoint {
    public Entrypoint(IEventBus modEventBus) {
        EventHooks.instance = new EventHooksImpl(modEventBus);

        Main.initializeClientEvents();

        modEventBus.addListener(FMLClientSetupEvent.class, event -> {
            MinecraftClient.getInstance().execute(Main::initializeClient);

            ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () ->
                (modContainer, parent) -> ConfigGUI.create(parent));
        });
    }
}
//?}