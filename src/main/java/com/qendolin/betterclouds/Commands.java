package com.qendolin.betterclouds;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.qendolin.betterclouds.clouds.Debug;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Commands {

    static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            literal(Main.MODID+":profile")
                .then(argument("interval", IntegerArgumentType.integer(30))
                    .executes(context -> {
                        int interval = IntegerArgumentType.getInteger(context, "interval");
                        Main.debugChatMessage("profiling.enabled", interval);
                        Debug.profileInterval = interval;
                        return 1;
                    }))
                .then(literal("stop")
                    .executes(context -> {
                        Main.debugChatMessage("profiling.disabled");
                        Debug.profileInterval = 0;
                        return 1;
                    }))
        );
        dispatcher.register(literal(Main.MODID+":frustum")
            .then(literal("capture")
                .executes(context -> {
                    context.getSource().getClient().worldRenderer.captureFrustum();
                    return 1;
                }))
            .then(literal("release")
                .executes(context -> {
                    context.getSource().getClient().worldRenderer.killFrustum();
                    return 1;
                }))
            .then(literal("debugCulling")
                .then(argument("enable", BoolArgumentType.bool())
                    .executes(context -> {
                        Debug.frustumCulling = BoolArgumentType.getBool(context, "enable");
                        return 1;
                    }))));
        dispatcher.register(literal(Main.MODID+":generator")
            .then(literal("pause")
                .executes(context -> {
                    Debug.generatorPause = true;
                    Main.debugChatMessage("generatorPaused");
                    return 1;
                }))
            .then(literal("resume")
                .executes(context -> {
                    Debug.generatorPause = false;
                    Main.debugChatMessage("generatorResumed");
                    return 1;
                })));
        dispatcher.register(literal(Main.MODID+":config")
            .executes(context -> {
                MinecraftClient client = context.getSource().getClient();
                // The chat screen will call setScreen(null) after the command handler
                // which would override our call, so we delay it
                client.send(() -> {
                    client.setScreen(ConfigGUI.create(null));
                });
                return 1;
            })
            .then(literal("reload").executes(context -> {
                Main.debugChatMessage("reloadingConfig");
                Main.getConfigInstance().load();
                Main.debugChatMessage("configReloaded");
                return 1;
            })));
    }
}
