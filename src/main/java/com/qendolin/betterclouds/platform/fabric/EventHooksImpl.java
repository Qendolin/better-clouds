package com.qendolin.betterclouds.platform.fabric;

import com.qendolin.betterclouds.platform.EventHooks;

//? if fabric {
/*import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import com.mojang.brigadier.CommandDispatcher;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class EventHooksImpl extends EventHooks {
    @Override
    public void onClientStarted(Consumer<MinecraftClient> callback) {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> callback.accept(client));
    }

    @Override
    public void onWorldJoin(Consumer<MinecraftClient> callback) {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> callback.accept(client));
    }

    @Override
    public void onClientResourcesReload(Supplier<ResourceReloader> supplier) {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
            .registerReloadListener((IdentifiableResourceReloadListener) supplier.get());
    }

    @Override
    public void onClientCommandRegistration(Consumer<CommandDispatcher<FabricClientCommandSource>> callback) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> callback.accept(dispatcher));
    }
}
*///?} else {
public abstract class EventHooksImpl extends EventHooks {
}
//?}

