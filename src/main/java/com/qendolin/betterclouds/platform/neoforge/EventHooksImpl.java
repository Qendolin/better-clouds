package com.qendolin.betterclouds.platform.neoforge;

import com.qendolin.betterclouds.platform.EventHooks;

//? if neoforge {
/*import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.server.command.ServerCommandSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

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
        modEventBus.addListener(FMLLoadCompleteEvent.class, event -> {
            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> callback.accept(client));
        });
    }

    @Override
    public void onWorldJoin(Consumer<MinecraftClient> callback) {
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingIn.class, event -> {
            callback.accept(MinecraftClient.getInstance());
        });
    }

    @Override
    public void onClientResourcesReload(Supplier<ResourceReloader> supplier) {
        modEventBus.addListener(RegisterClientReloadListenersEvent.class, event -> {
            event.registerReloadListener(supplier.get());
        });
    }

    @Override
    public void onClientCommandRegistration(Consumer<CommandDispatcher<ServerCommandSource>> callback) {
        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, event -> {
            callback.accept(event.getDispatcher());
        });
    }

}
*///?} else {
public abstract class EventHooksImpl extends EventHooks {
}
//?}

