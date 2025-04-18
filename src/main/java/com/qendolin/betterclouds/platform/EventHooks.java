package com.qendolin.betterclouds.platform;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.MinecraftClient;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.resource.ResourceReloader;


//? if fabric {
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
//?}

//? if neoforge || forge {
/*import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
*///?}


public abstract class EventHooks {

    public static EventHooks instance;

    public abstract void onClientStarted(Consumer<MinecraftClient> callback);

    public abstract void onWorldJoin(Consumer<MinecraftClient> callback);

    public abstract void onClientResourcesReload(Supplier<ResourceReloader> supplier);

    public abstract void onClientTick(Consumer<MinecraftClient> callback);

    //? if fabric {
    public abstract void onClientCommandRegistration(Consumer<CommandDispatcher<FabricClientCommandSource>> callback);
    //?} else {
    /*public abstract void onClientCommandRegistration(Consumer<CommandDispatcher<ServerCommandSource>> callback);
     *///?}
}
