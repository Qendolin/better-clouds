package com.qendolin.betterclouds;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.qendolin.betterclouds.clouds.Debug;
import com.qendolin.betterclouds.renderdoc.CaptureManager;
import com.qendolin.betterclouds.renderdoc.RenderDoc;
import com.qendolin.betterclouds.renderdoc.RenderDocLoader;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

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
            .then(literal("renderdoc")
                .then(literal("capture")
                    .executes(context -> {
                        if(RenderDoc.isAvailable()) {
                            Main.debugChatMessage("renderdoc.capture.trigger");
                            CaptureManager.capture(result -> {
                                if(result == null) {
                                    Main.debugChatMessage("renderdoc.capture.failure");
                                } else {
                                    Path path = Path.of(result.path());
                                    Main.debugChatMessage("renderdoc.capture.success",
                                        Text.literal(path.toAbsolutePath().normalize().toString())
                                            .styled(style -> style
                                                .withUnderline(true)
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path.getParent().toString()))
                                            ));
                                }
                            });
                            return 1;
                        } else if(RenderDocLoader.isAvailable()) {
                            Main.debugChatMessage(Text.translatable(
                                Main.debugChatMessageKey("renderdoc.prompt.load"),
                                Text.translatable(Main.debugChatMessageKey("renderdoc.prompt.load.action"))
                                    .styled(style -> style
                                        .withUnderline(true)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/betterclouds:debug renderdoc load")))
                            ));
                            return 0;
                        } else {
                            Main.debugChatMessage(Text.translatable(
                                Main.debugChatMessageKey("renderdoc.prompt.install"),
                                Text.translatable(Main.debugChatMessageKey("renderdoc.prompt.install.action"))
                                    .styled(style -> style
                                        .withUnderline(true)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/betterclouds:debug renderdoc install")))
                                ));
                            return 0;
                        }
                    }))
                .then(literal("install").executes(context -> {
                    CompletableFuture.runAsync(() -> {
                        if(!RenderDoc.isAvailable() && !RenderDocLoader.isAvailable()) {
                            Main.debugChatMessage("renderdoc.downloading");
                            try {
                                RenderDocLoader.install();
                            } catch (Exception e) {
                                Main.debugChatMessage("generic.error", e.toString());
                            }
                        }
                        Path path = RenderDocLoader.libPath();
                        Main.debugChatMessage("renderdoc.installed",
                            Text.literal(path.toAbsolutePath().normalize().toString())
                                .styled(style -> style
                                    .withUnderline(true)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path.getParent().toString()))));
                    });
                    return 1;
                }))
                .then(literal("uninstall").executes(context -> {
                    try {
                        RenderDocLoader.uninstall();
                    } catch (Exception e) {
                        Main.debugChatMessage("generic.error", e.toString());
                        return 0;
                    }
                    return 1;
                }))
                .then(literal("load").executes(context -> {
                    if(!RenderDocLoader.isAvailable()) {
                        Main.debugChatMessage(Text.translatable(
                            Main.debugChatMessageKey("renderdoc.prompt.install"),
                            Text.translatable(Main.debugChatMessageKey("renderdoc.prompt.install.action"))
                                .styled(style -> style
                                    .withUnderline(true)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/betterclouds:debug renderdoc install")))
                        ));
                        return 0;
                    }
                    if(RenderDoc.isAvailable()) {
                        Main.debugChatMessage("renderdoc.load.ready", RenderDoc.getAPIVersion());
                        return 1;
                    }
                    try {
                        // in 12 hours
                        long expires = System.currentTimeMillis() + 1000 * 60 * 60 * 12;
                        CaptureManager.writeLaunchConfig(new CaptureManager.LaunchConfig(true, true, expires));
                    } catch (IOException e) {
                        Main.debugChatMessage("generic.error", e.toString());
                        return 0;
                    }
                    Main.debugChatMessage("renderdoc.load.queued");
                    return 1;
                }))));

    }
}
