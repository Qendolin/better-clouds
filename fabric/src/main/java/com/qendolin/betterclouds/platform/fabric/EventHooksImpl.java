package com.qendolin.betterclouds.platform.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.config.ShaderPresetLoader;
import com.qendolin.betterclouds.platform.EventHooks;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EventHooksImpl extends EventHooks {
    @Override
    public void onClientStarted(Consumer<MinecraftClient> callback) {
        ClientLifecycleEvents.CLIENT_STARTED.register(callback::accept);
    }

    @Override
    public void onWorldJoin(Consumer<MinecraftClient> callback) {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> callback.accept(client));
    }

    @Override
    public void onClientResourcesReload(Supplier<ResourceReloader> supplier) {
        ResourceReloader reloader = supplier.get();
        IdentifiableResourceReloadListener listener;
        if (reloader instanceof IdentifiableResourceReloadListener identifiable) {
            listener = identifiable;
        } else {
            Identifier id = reloader instanceof ShaderPresetLoader
                ? ShaderPresetLoader.ID
                : Identifier.of(BetterCloudsStatic.MODID, "resource_reloader");
            listener = new IdentifiableResourceReloadListener() {
                @Override
                public Identifier getFabricId() {
                    return id;
                }

                @Override
                public CompletableFuture<Void> reload(Store store, Executor loadExecutor, Synchronizer helper, Executor applyExecutor) {
                    return reloader.reload(store, loadExecutor, helper, applyExecutor);
                }
            };
        }

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
            .registerReloadListener(listener);
    }

    @Override
    public void onClientTick(Consumer<MinecraftClient> callback) {
        ClientTickEvents.END_CLIENT_TICK.register(callback::accept);
    }

    @Override
    public void onClientCommandRegistration(Consumer<CommandDispatcher<?>> callback) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> callback.accept(dispatcher));
    }
}
