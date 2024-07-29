package com.qendolin.betterclouds.platform;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.MinecraftClient;
import java.util.function.Consumer;
import java.util.function.Supplier;


//? if fabric {
/*import com.qendolin.betterclouds.platform.fabric.EventHooksImpl;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;*/
//?}

//? if neoforge {
import com.qendolin.betterclouds.platform.neoforge.EventHooksImpl;
import net.minecraft.command.CommandSource;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.server.command.ServerCommandSource;
//?}

public abstract class EventHooks {

    private static EventHooks instance;

    public static EventHooks instance() {
        if(instance == null) {
            //? if fabric {
            /*instance = new FabricEventHooks();*/
            //?} elif neoforge {
            instance = new EventHooksImpl();
            //?} else {
            /*instance = new EventHooks();
            *///?}
        }

        return instance;
    }

    public abstract void onClientStarted(Consumer<MinecraftClient> callback);

    public abstract void onWorldJoin(Consumer<MinecraftClient> callback);

    public abstract void onClientResourcesReload(Supplier<ResourceReloader> supplier);

    //? if fabric {
    /*public abstract void onClientCommandRegistration(Consumer<CommandDispatcher<FabricClientCommandSource>> callback);
    *///?} else {
    public abstract void onClientCommandRegistration(Consumer<CommandDispatcher<ServerCommandSource>> callback);
    //?}
}
