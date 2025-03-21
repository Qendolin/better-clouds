package com.qendolin.betterclouds.platform.forge;

import com.qendolin.betterclouds.platform.EventHooks;

//? if forge {
/*import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class EventHooksImpl extends EventHooks {

    private final IEventBus modEventBus;

    public EventHooksImpl(IEventBus modEventBus) {
        super();
        this.modEventBus = modEventBus;
    }

    @Override
    public void onClientStarted(Consumer<MinecraftClient> callback) {
        modEventBus.<FMLLoadCompleteEvent>addListener(event -> {
            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> callback.accept(client));
        });
    }

    @Override
    public void onWorldJoin(Consumer<MinecraftClient> callback) {
        MinecraftForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggingIn>addListener(event -> {
            callback.accept(MinecraftClient.getInstance());
        });
    }

    @Override
    public void onClientResourcesReload(Supplier<ResourceReloader> supplier) {
        modEventBus.<RegisterClientReloadListenersEvent>addListener(event -> {
            event.registerReloadListener(supplier.get());
        });
    }

    @Override
    public void onClientCommandRegistration(Consumer<CommandDispatcher<ServerCommandSource>> callback) {
        MinecraftForge.EVENT_BUS.<RegisterClientCommandsEvent>addListener(event -> {
            callback.accept(event.getDispatcher());
        });
    }

}
*///?} else {
@SuppressWarnings("unused")
public abstract class EventHooksImpl extends EventHooks {
}
//?}

