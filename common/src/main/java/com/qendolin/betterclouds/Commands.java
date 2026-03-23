package com.qendolin.betterclouds;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import com.qendolin.betterclouds.clouds.Debug;
import com.qendolin.betterclouds.compat.GLCompat;
import com.qendolin.betterclouds.config.ConfigGUI;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.renderdoc.CaptureManager;
import com.qendolin.betterclouds.renderdoc.RenderDoc;
import com.qendolin.betterclouds.renderdoc.RenderDocLoader;
import com.qendolin.betterclouds.util.ChatUtil;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

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
        dispatcher.register(literal(BetterCloudsStatic.MODID + ":profile")
            .then(argument("interval", IntegerArgumentType.integer(30))
                .executes(context -> {
                    int interval = IntegerArgumentType.getInteger(context, "interval");
                    ChatUtil.debugChatMessage("profiling.enabled", interval);
                    Debug.profileInterval = interval;
                    return 1;
                })));

        dispatcher.register(literal(BetterCloudsStatic.MODID + ":profile")
            .then(argument("interval", IntegerArgumentType.integer(30))
                .executes(context -> {
                    int interval = IntegerArgumentType.getInteger(context, "interval");
                    ChatUtil.debugChatMessage("profiling.enabled", interval);
                    Debug.profileInterval = interval;
                    return 1;
                }))
            .then(literal("stop")
                .executes(context -> {
                    ChatUtil.debugChatMessage("profiling.disabled");
                    Debug.profileInterval = 0;
                    var renderer = BetterClouds.getCloudsRenderer();
                    if (renderer != null) {
                        var timer = renderer.resources().timer();
                        if (timer != null)
                            timer.reset();
                    }
                    return 1;
                }))
        );
        dispatcher.register(literal(BetterCloudsStatic.MODID + ":frustum")
            .then(literal("capture")
                .executes(context -> {
                    ChatUtil.debugChatMessage(Component.literal("Frustum capture is not available on Minecraft 26.1"));
                    return 1;
                }))
            .then(literal("release")
                .executes(context -> {
                    ChatUtil.debugChatMessage(Component.literal("Frustum capture is not available on Minecraft 26.1"));
                    return 1;
                }))
            .then(literal("debugCulling")
                .then(argument("enable", BoolArgumentType.bool())
                    .executes(context -> {
                        Debug.frustumCulling = BoolArgumentType.getBool(context, "enable");
                        return 1;
                    }))));
        dispatcher.register(literal(BetterCloudsStatic.MODID + ":generator")
            .then(literal("pause")
                .executes(context -> {
                    Debug.generatorPause = true;
                    ChatUtil.debugChatMessage("generatorPaused");
                    return 1;
                }))
            .then(literal("resume")
                .executes(context -> {
                    Debug.generatorPause = false;
                    ChatUtil.debugChatMessage("generatorResumed");
                    return 1;
                }))
            .then(literal("update")
                .executes(context -> {
                    Debug.generatorForceUpdate = true;
                    return 1;
                })));
        dispatcher.register(literal(BetterCloudsStatic.MODID + ":animation")
            .then(literal("pause")
                .executes(context -> {
                    Debug.animationPause = 0;
                    ChatUtil.debugChatMessage("animationPaused");
                    return 1;
                })
                .then(argument("ticks", IntegerArgumentType.integer(1))
                    .executes(context -> {
                        Debug.animationPause = IntegerArgumentType.getInteger(context, "ticks");
                        ChatUtil.debugChatMessage("animationPaused");
                        return 1;
                    })))
            .then(literal("resume")
                .executes(context -> {
                    Debug.animationPause = -1;
                    ChatUtil.debugChatMessage("animationResumed");
                    return 1;
                })));
        dispatcher.register(literal(BetterCloudsStatic.MODID + ":config")
            .then(literal("open").executes(context -> {
                // The chat screen will call setScreen(null) after the command handler
                // which would override our call, so we delay it
                client.schedule(() -> client.setScreen(ConfigGUI.create(null)));
                return 1;
            }))
            .then(literal("reload").executes(context -> {
                ChatUtil.debugChatMessage("reloadingConfig");
                ConfigManager.handler().load();
                ChatUtil.debugChatMessage("configReloaded");
                return 1;
            }))
            .then(literal("set")
                .then(literal("gpuIncompatibleMessage")
                    .then(argument("enable", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean enable = BoolArgumentType.getBool(context, "enable");
                            if (ConfigManager.instance().gpuIncompatibleMessageEnabled == enable) return 1;
                            ConfigManager.instance().gpuIncompatibleMessageEnabled = enable;
                            ConfigManager.handler().save();
                            ChatUtil.debugChatMessage("updatedPreferences");
                            return 1;
                        })))
                .then(literal("yesLunarClientSeriouslySucks")
                    .executes(context -> {
                        ConfigManager.instance().lunarSucksMessageEnabled = false;
                        ConfigManager.handler().save();
                        ChatUtil.debugChatMessage("updatedPreferences");
                        return 1;
                    }))
            )
        );
        dispatcher.register(literal(BetterCloudsStatic.MODID + ":dimension")
            .then(literal("enable")
                .executes(context -> {
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
                .executes(context -> {
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
            .then(renderdocCommands())
            .then(literal("fallback")
                .then(argument("name", FallbackArgumentType.fallback())
                    .executes(context -> {
                        FallbackArgument fallback = FallbackArgumentType.getFallback(context, "name");
                        boolean enabled = fallback.get(GLCompat.glCompat);
                        ChatUtil.debugChatMessage(Component.literal(String.format("Fallback %s is currently %s", fallback.getSerializedName(), enabled ? "enabled" : "disabled")));
                        return 1;
                    })
                    .then(argument("enable", BoolArgumentType.bool())
                        .executes(context -> {
                            FallbackArgument fallback = FallbackArgumentType.getFallback(context, "name");
                            boolean enable = BoolArgumentType.getBool(context, "enable");
                            fallback.set(GLCompat.glCompat, enable);
                            client.reloadResourcePacks().whenComplete((unused, throwable) -> {
                                ChatUtil.debugChatMessage(Component.literal(String.format("Fallback %s is now %s", fallback.getSerializedName(), enable ? "enabled" : "disabled")));
                            });
                            return 1;
                        })))
            )
        );
    }

    private static LiteralArgumentBuilder<Object> renderdocCommands() {
        return literal("renderdoc")
            .then(literal("capture")
                .executes(context -> {
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
            .then(literal("install").executes(context -> {
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
            .then(literal("uninstall").executes(context -> {
                try {
                    RenderDocLoader.uninstall();
                } catch (Exception e) {
                    ChatUtil.debugChatMessage("generic.error", e.toString());
                    return 0;
                }
                return 1;
            }))
            .then(literal("load").executes(context -> {
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

    private enum FallbackArgument implements StringRepresentable {
        BASE_INSTANCE(GLCompat::useBaseInstanceFallback, GLCompat::setUseBaseInstanceFallback),
        STENCIL_TEXTURE(GLCompat::useStencilTextureFallback, GLCompat::setUseStencilTextureFallback),
        TEX_STORAGE(GLCompat::useTexStorageFallback, GLCompat::setUseTexStorageFallback),
        DEPTH_WRITE(GLCompat::useDepthWriteFallback, GLCompat::setUseDepthWriteFallback);

        private static final Codec<FallbackArgument> CODEC = StringRepresentable.fromEnum(FallbackArgument::values);

        private final Function<GLCompat, Boolean> getter;
        private final BiConsumer<GLCompat, Boolean> setter;

        FallbackArgument(Function<GLCompat, Boolean> getter, BiConsumer<GLCompat, Boolean> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        public void set(GLCompat compat, boolean enable) {
            setter.accept(compat, enable);
        }

        public boolean get(GLCompat compat) {
            return getter.apply(compat);
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }

    private static class FallbackArgumentType extends StringRepresentableArgument<FallbackArgument> {
        private FallbackArgumentType() {
            super(FallbackArgument.CODEC, FallbackArgument::values);
        }

        public static StringRepresentableArgument<FallbackArgument> fallback() {
            return new FallbackArgumentType();
        }

        public static FallbackArgument getFallback(CommandContext<?> context, String id) {
            return context.getArgument(id, FallbackArgument.class);
        }
    }

    public static void sendGpuIncompatibleChatMessage() {
        if (!ConfigManager.instance().gpuIncompatibleMessageEnabled) return;
        ChatUtil.debugChatMessage(
            Component.translatable(ChatUtil.debugChatMessageKey("gpuIncompatible"))
                .append(Component.literal("\n - "))
                .append(Component.translatable(ChatUtil.debugChatMessageKey("generic.disable"))
                    .withStyle(style -> style.withItalic(true).withUnderlined(true).withColor(ChatFormatting.GRAY)
                        .withClickEvent(createCommandClickEvent(
                            "/betterclouds:config set gpuIncompatibleMessage false")))));
    }

    public static void sendGpuPartiallyIncompatibleChatMessage() {
        if (!ConfigManager.instance().gpuIncompatibleMessageEnabled) return;
        ChatUtil.debugChatMessage(
            Component.translatable(ChatUtil.debugChatMessageKey("gpuPartiallyIncompatible"))
                .append(Component.literal("\n - "))
                .append(Component.translatable(ChatUtil.debugChatMessageKey("generic.disable"))
                    .withStyle(style -> style.withItalic(true).withUnderlined(true).withColor(ChatFormatting.GRAY)
                        .withClickEvent(createCommandClickEvent(
                            "/betterclouds:config set gpuIncompatibleMessage false")))));
    }

    public static void sendHardwareMaybeIncompatibleChatMessage() {
        if (!ConfigManager.instance().gpuIncompatibleMessageEnabled) return;
        ChatUtil.debugChatMessage(
            Component.translatable(ChatUtil.debugChatMessageKey("hwMaybeIncompatible"), GLCompat.getCpuInfo(), GLCompat.getRenderer())
                .append(Component.literal("\n - "))
                .append(Component.translatable(ChatUtil.debugChatMessageKey("generic.disable"))
                    .withStyle(style -> style.withItalic(true).withUnderlined(true).withColor(ChatFormatting.GRAY)
                        .withClickEvent(createCommandClickEvent(
                            "/betterclouds:config set gpuIncompatibleMessage false")))));
    }

    private static ClickEvent createCommandClickEvent(String command) {
        return new ClickEvent.RunCommand(command);
    }

    private static ClickEvent createOpenFileClickEvent(String path) {
        return new ClickEvent.OpenFile(path);
    }
}
