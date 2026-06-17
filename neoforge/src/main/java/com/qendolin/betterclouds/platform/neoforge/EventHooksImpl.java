package com.qendolin.betterclouds.platform.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.config.preset.PresetLoader;
import com.qendolin.betterclouds.platform.EventHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
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
    public void onClientStarted(Consumer<Minecraft> callback) {
        modEventBus.addListener(FMLLoadCompleteEvent.class, _ -> {
            Minecraft client = Minecraft.getInstance();
            client.execute(() -> callback.accept(client));
        });
    }

    @Override
    public void onWorldJoin(Consumer<Minecraft> callback) {
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingIn.class, _ -> callback.accept(Minecraft.getInstance()));
    }

    @Override
    public void onClientResourcesReload(Supplier<PreparableReloadListener> supplier) {
        PreparableReloadListener listener = supplier.get();
        Identifier id = listener instanceof PresetLoader<?> d ?
                d.id : Identifier.fromNamespaceAndPath(BetterCloudsStatic.MODID, "resource_reloader");
        modEventBus.addListener(AddClientReloadListenersEvent.class, event -> event.addListener(id, listener));
    }

    @Override
    public void onClientTick(Consumer<Minecraft> callback) {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, _ -> {
            Minecraft client = Minecraft.getInstance();
            client.execute(() -> callback.accept(client));
        });
    }

    @Override
    public void onClientCommandRegistration(Consumer<CommandDispatcher<?>> callback) {
        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, event -> {
            CommandDispatcher<?> dispatcher = event.getDispatcher();
            callback.accept(dispatcher);
        });
    }
}
