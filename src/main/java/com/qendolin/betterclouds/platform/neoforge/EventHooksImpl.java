package com.qendolin.betterclouds.platform.neoforge;

import com.qendolin.betterclouds.platform.EventHooks;

//? if neoforge {
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.server.command.ServerCommandSource;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class EventHooksImpl extends EventHooks {
    @Override
    public void onClientStarted(Consumer<MinecraftClient> callback) {
        NeoForge.EVENT_BUS.addListener(FMLClientSetupEvent.class, event -> {
            callback.accept(MinecraftClient.getInstance());
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
        NeoForge.EVENT_BUS.addListener(AddReloadListenerEvent.class, event -> {
            event.addListener(supplier.get());
        });
    }

    @Override
    public void onClientCommandRegistration(Consumer<CommandDispatcher<ServerCommandSource>> callback) {
        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, event -> {
            callback.accept(event.getDispatcher());
        });
    }

}
//?} else {
/*public abstract class EventHooksImpl extends EventHooks {
}
*///?}

