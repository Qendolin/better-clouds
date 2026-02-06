package com.qendolin.betterclouds.platform;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceReloader;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class EventHooks {

    public static EventHooks instance;

    public abstract void onClientStarted(Consumer<MinecraftClient> callback);

    public abstract void onWorldJoin(Consumer<MinecraftClient> callback);

    public abstract void onClientResourcesReload(Supplier<ResourceReloader> supplier);

    public abstract void onClientTick(Consumer<MinecraftClient> callback);

    public abstract void onClientCommandRegistration(Consumer<CommandDispatcher<?>> callback);
}
