package com.qendolin.betterclouds.platform.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.config.preset.PresetLoader;
import com.qendolin.betterclouds.platform.EventHooks;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EventHooksImpl extends EventHooks {
    @Override
    public void onClientStarted(Consumer<Minecraft> callback) {
        ClientLifecycleEvents.CLIENT_STARTED.register(callback::accept);
    }

    @Override
    public void onWorldJoin(Consumer<Minecraft> callback) {
        ClientPlayConnectionEvents.JOIN.register((_, _, client) -> callback.accept(client));
    }

    @Override
    public void onClientResourcesReload(Supplier<PreparableReloadListener> supplier) {
        PreparableReloadListener reloader = supplier.get();
        IdentifiableResourceReloadListener listener;
        if (reloader instanceof IdentifiableResourceReloadListener identifiable) {
            listener = identifiable;
        } else {
            Identifier id = reloader instanceof PresetLoader
                    ? ((PresetLoader) reloader).id
                    : Identifier.fromNamespaceAndPath(BetterCloudsStatic.MODID, "resource_reloader");
            listener = new IdentifiableResourceReloadListener() {
                @Override
                public @NonNull Identifier getFabricId() {
                    return id;
                }

                @Override
                public @NonNull CompletableFuture<Void> reload(@NonNull SharedState store, @NonNull Executor loadExecutor, @NonNull PreparationBarrier helper, @NonNull Executor applyExecutor) {
                    return reloader.reload(store, loadExecutor, helper, applyExecutor);
                }
            };
        }

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(listener);
    }

    @Override
    public void onClientTick(Consumer<Minecraft> callback) {
        ClientTickEvents.END_CLIENT_TICK.register(callback::accept);
    }

    @Override
    public void onClientCommandRegistration(Consumer<CommandDispatcher<?>> callback) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> callback.accept(dispatcher));
    }
}
