package com.qendolin.betterclouds.platform.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import com.qendolin.betterclouds.config.ShaderPresetLoader;
import com.qendolin.betterclouds.platform.EventHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceReloader;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

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
        modEventBus.addListener(AddClientReloadListenersEvent.class, event -> {
            event.addListener(ShaderPresetLoader.ID, supplier.get());
        });
    }

    @Override
    public void onClientTick(Consumer<MinecraftClient> callback) {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> {
            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> callback.accept(client));
        });
    }

    @Override
    public void onClientCommandRegistration(Consumer<CommandDispatcher<?>> callback) {
        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, event -> {
            callback.accept(event.getDispatcher());
        });
    }
}
