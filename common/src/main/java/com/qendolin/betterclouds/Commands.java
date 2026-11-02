package com.qendolin.betterclouds;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.qendolin.betterclouds.compat.DhCompat;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.config.gui.ConfigGUI;
import com.qendolin.betterclouds.renderdoc.*;
import com.qendolin.betterclouds.util.ChatUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class Commands {

    private static LiteralArgumentBuilder<Object> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    private static <T> RequiredArgumentBuilder<Object, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    static void register(CommandDispatcher<?> dispatcher) {
        registerImpl(castDispatcher(dispatcher));
    }

    @SuppressWarnings("unchecked")
    private static CommandDispatcher<Object> castDispatcher(CommandDispatcher<?> dispatcher) {
        return (CommandDispatcher<Object>) dispatcher;
    }

    private static void registerImpl(CommandDispatcher<Object> dispatcher) {
        final Minecraft client = Minecraft.getInstance();
        dispatcher.register(literal(BetterCloudsStatic.MODID + ":config")
                .then(literal("open").executes(_ -> {
                    // The chat screen will call setScreen(null) after the command handler
                    // which would override our call, so we delay it
                    client.schedule(() -> client.gui.setScreen(ConfigGUI.create(null)));
                    return 1;
                }))
                .then(literal("reload").executes(_ -> {
                    ChatUtil.debugChatMessage("reloadingConfig");
                    ConfigManager.handler().load();
                    ChatUtil.debugChatMessage("configReloaded");
                    return 1;
                }))
                .then(literal("set")
                        .then(literal("cloudSpeed")
                                .then(argument("speed", FloatArgumentType.floatArg(0, 1024))
                                        .executes(context -> {
                                            ConfigManager.instance().travelSpeed = FloatArgumentType.getFloat(context, "speed") / 20;
                                            return 1;
                                        })
                                )
                        )
                        .then(literal("cloudsDisabledMessage")
                                .then(argument("enable", BoolArgumentType.bool())
                                        .executes(context -> {
                                            boolean enable = BoolArgumentType.getBool(context, "enable");
                                            if (ConfigManager.instance().cloudsDisabledMessageEnabled == enable)
                                                return 1;
                                            ConfigManager.instance().cloudsDisabledMessageEnabled = enable;
                                            ConfigManager.handler().save();
                                            ChatUtil.debugChatMessage("updatedPreferences");
                                            return 1;
                                        })))
                )
        );
        dispatcher.register(literal(BetterCloudsStatic.MODID + ":dimension")
                .then(literal("enable")
                        .executes(_ -> {
                            if (client.level == null)
                                return 0;
                            var entry = client.level.dimensionTypeRegistration();
                            var key = entry.unwrapKey().orElse(null);
                            if (key == null)
                                return 0;
                            if (!ConfigManager.instance().enabledDimensions.contains(key)) {
                                ConfigManager.instance().enabledDimensions.add(key);
                            }
                            ConfigManager.handler().save();
                            ChatUtil.debugChatMessage("dimensionAdded", key.identifier().toString());
                            return 1;
                        }))
                .then(literal("disable")
                        .executes(_ -> {
                            if (client.level == null)
                                return 0;
                            var entry = client.level.dimensionTypeRegistration();
                            var key = entry.unwrapKey().orElse(null);
                            if (key == null)
                                return 0;
                            ConfigManager.instance().enabledDimensions.remove(key);
                            ConfigManager.handler().save();
                            ChatUtil.debugChatMessage("dimensionRemoved", key.identifier().toString());
                            return 1;
                        })));
        dispatcher.register(literal(BetterCloudsStatic.MODID + ":debug")
                .then(renderdocCommands()));
    }

    private static LiteralArgumentBuilder<Object> renderdocCommands() {
        return literal("renderdoc")
                .then(literal("capture")
                        .executes(_ -> {
                            if (RenderDoc.isAvailable()) {
                                ChatUtil.debugChatMessage("renderdoc.capture.trigger");
                                CaptureManager.capture(result -> {
                                    if (result == null) {
                                        ChatUtil.debugChatMessage("renderdoc.capture.failure");
                                    } else {
                                        Path path = Path.of(result.path());
                                        ChatUtil.debugChatMessage("renderdoc.capture.success",
                                                Component.literal(path.toAbsolutePath().normalize().toString())
                                                        .withStyle(style -> style
                                                                .withUnderlined(true)
                                                                .withClickEvent(createOpenFileClickEvent(path.getParent().toString()))
                                                        ));
                                    }
                                });
                                return 1;
                            } else if (RenderDocLoader.isAvailable()) {
                                ChatUtil.debugChatMessage(Component.translatable(
                                        ChatUtil.debugChatMessageKey("renderdoc.prompt.load"),
                                        Component.translatable(ChatUtil.debugChatMessageKey("renderdoc.prompt.load.action"))
                                                .withStyle(style -> style
                                                        .withUnderlined(true)
                                                        .withClickEvent(createCommandClickEvent("/betterclouds:debug renderdoc load")))
                                ));
                                return 0;
                            } else {
                                ChatUtil.debugChatMessage(Component.translatable(
                                        ChatUtil.debugChatMessageKey("renderdoc.prompt.install"),
                                        Component.translatable(ChatUtil.debugChatMessageKey("renderdoc.prompt.install.action"))
                                                .withStyle(style -> style
                                                        .withUnderlined(true)
                                                        .withClickEvent(createCommandClickEvent("/betterclouds:debug renderdoc install")))
                                ));
                                return 0;
                            }
                        }))
                .then(literal("install").executes(_ -> {
                    CompletableFuture.runAsync(() -> {
                        if (!RenderDoc.isAvailable() && !RenderDocLoader.isAvailable()) {
                            ChatUtil.debugChatMessage("renderdoc.downloading");
                            try {
                                RenderDocLoader.install();
                            } catch (Exception e) {
                                ChatUtil.debugChatMessage("generic.error", e.toString());
                            }
                        }
                        Path path = RenderDocLoader.libPath();
                        ChatUtil.debugChatMessage("renderdoc.installed",
                                Component.literal(path.toAbsolutePath().normalize().toString())
                                        .withStyle(style -> style
                                                .withUnderlined(true)
                                                .withClickEvent(createOpenFileClickEvent(path.getParent().toString()))));
                    });
                    return 1;
                }))
                .then(literal("uninstall").executes(_ -> {
                    try {
                        RenderDocLoader.uninstall();
                    } catch (Exception e) {
                        ChatUtil.debugChatMessage("generic.error", e.toString());
                        return 0;
                    }
                    return 1;
                }))
                .then(literal("load").executes(_ -> {
                    if (!RenderDocLoader.isAvailable()) {
                        ChatUtil.debugChatMessage(Component.translatable(
                                ChatUtil.debugChatMessageKey("renderdoc.prompt.install"),
                                Component.translatable(ChatUtil.debugChatMessageKey("renderdoc.prompt.install.action"))
                                        .withStyle(style -> style
                                                .withUnderlined(true)
                                                .withClickEvent(createCommandClickEvent("/betterclouds:debug renderdoc install")))
                        ));
                        return 0;
                    }
                    if (RenderDoc.isAvailable()) {
                        ChatUtil.debugChatMessage("renderdoc.load.ready", RenderDoc.getAPIVersion());
                        return 1;
                    }
                    try {
                        // in 12 hours
                        long expires = System.currentTimeMillis() + 1000 * 60 * 60 * 12;
                        CaptureManager.writeLaunchConfig(new CaptureManager.LaunchConfig(true, true, expires));
                    } catch (IOException e) {
                        ChatUtil.debugChatMessage("generic.error", e.toString());
                        return 0;
                    }
                    ChatUtil.debugChatMessage("renderdoc.load.queued");
                    return 1;
                }));
    }

    public static void sendCloudsDisabledMessage() {
        var component = Component.translatable(ChatUtil.debugChatMessageKey("cloudsDisabledMessage"));
        if (DhCompat.instance.isEnabled())
            component.append(Component.translatable(ChatUtil.debugChatMessageKey("distantHorizonsDisablingMessage")));
        component.append(Component.literal("\n - "))
                .append(Component.translatable(ChatUtil.debugChatMessageKey("generic.disable"))
                        .withStyle(style -> style.withItalic(true).withUnderlined(true).withColor(ChatFormatting.GRAY)
                                .withClickEvent(createCommandClickEvent(
                                        "/betterclouds:config set cloudsDisabledMessage false"))));
        ChatUtil.debugChatMessage(component);
    }

    public static void sendCrashChatMessage() {
        ChatUtil.debugChatMessage(Component.translatable(ChatUtil.debugChatMessageKey("crashMessage")));
    }

    private static ClickEvent createCommandClickEvent(String command) {
        return new ClickEvent.RunCommand(command);
    }

    private static ClickEvent createOpenFileClickEvent(String path) {
        return new ClickEvent.OpenFile(path);
    }
}
