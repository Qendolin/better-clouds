package com.qendolin.betterclouds.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.qendolin.betterclouds.ConfigGUI;
import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.clouds.Debug;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Commands {

    static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal(Main.MODID + ":profile")
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
        dispatcher.register(literal(Main.MODID + ":frustum")
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
        dispatcher.register(literal(Main.MODID + ":generator")
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
                }))
            .then(literal("update")
                .executes(context -> {
                    Debug.generatorForceUpdate = true;
                    return 1;
                })));
        dispatcher.register(literal(Main.MODID + ":animation")
            .then(literal("pause")
                .executes(context -> {
                    Debug.animationPause = 0;
                    Main.debugChatMessage("animationPaused");
                    return 1;
                })
                .then(argument("ticks", IntegerArgumentType.integer(1))
                    .executes(context -> {
                        Debug.animationPause = IntegerArgumentType.getInteger(context, "ticks");
                        Main.debugChatMessage("animationPaused");
                        return 1;
                    })))
            .then(literal("resume")
                .executes(context -> {
                    Debug.animationPause = -1;
                    Main.debugChatMessage("animationResumed");
                    return 1;
                })));
        dispatcher.register(literal(Main.MODID + ":config")
            .then(literal("open").executes(context -> {
                MinecraftClient client = context.getSource().getClient();
                // The chat screen will call setScreen(null) after the command handler
                // which would override our call, so we delay it
                client.send(() -> client.setScreen(ConfigGUI.create(null)));
                return 1;
            }))
            .then(literal("reload").executes(context -> {
                Main.debugChatMessage("reloadingConfig");
                Main.getConfigInstance().load();
                Main.debugChatMessage("configReloaded");
                return 1;
            }))
            .then(literal("gpuIncompatibleMessage")
                .then(argument("enable", BoolArgumentType.bool())
                    .executes(context -> {
                        boolean enable = BoolArgumentType.getBool(context, "enable");
                        if (Main.getConfig().gpuIncompatibleMessageEnabled == enable) return 1;
                        Main.getConfig().gpuIncompatibleMessageEnabled = enable;
                        Main.getConfigInstance().save();
                        Main.debugChatMessage("updatedPreferences");
                        return 1;
                    }))));
        dispatcher.register(literal(Main.MODID + ":debug")
            .then(literal("trace")
                .executes(context -> {
                    Debug.DebugTrace trace = Debug.captureDebugTrace(snap -> {
                        File file = Debug.writeDebugTrace(snap);
                        if (file == null) {
                            Main.debugChatMessage(Text.literal("Failed to write debug trace"));
                        } else {
                            Main.debugChatMessage(Text.literal("Saved debug trace at " + file.getAbsolutePath()));
                        }
                    });
                    trace.captureFramebuffers = false;
                    trace.startRecording();
                    AtomicInteger endFrame = new AtomicInteger(6000);
                    CompletableFuture.runAsync(() -> {
                        while (trace.isRecording()) {
                            if (trace.getRecordedFrames() > endFrame.get()) {
                                trace.stopRecording();
                            }
                        }
                    });
                    context.getSource().getClient().reloadResources().whenComplete((unused, throwable) -> {
                        trace.captureFramebuffers = true;
                        endFrame.set(trace.getRecordedFrames() + 3);
                    });
                    return 1;
                })));
    }
}
