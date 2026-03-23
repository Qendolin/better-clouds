package com.qendolin.betterclouds.platform;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class EventHooks {

    public static EventHooks instance;

    public abstract void onClientStarted(Consumer<Minecraft> callback);

    public abstract void onWorldJoin(Consumer<Minecraft> callback);

    public abstract void onClientResourcesReload(Supplier<PreparableReloadListener> supplier);

    public abstract void onClientTick(Consumer<Minecraft> callback);

    public abstract void onClientCommandRegistration(Consumer<CommandDispatcher<?>> callback);
}
