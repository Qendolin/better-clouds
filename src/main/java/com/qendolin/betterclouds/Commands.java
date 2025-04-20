package com.qendolin.betterclouds;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import com.qendolin.betterclouds.clouds.Debug;
import com.qendolin.betterclouds.clouds.FrustumCuller;
import com.qendolin.betterclouds.compat.GLCompat;
import com.qendolin.betterclouds.config.ConfigGUI;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.renderdoc.CaptureManager;
import com.qendolin.betterclouds.renderdoc.RenderDoc;
import com.qendolin.betterclouds.renderdoc.RenderDocLoader;
import com.qendolin.betterclouds.util.ChatUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

//? if fabric {
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
//?} else {
/*import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
*///?}

public class Commands {

    //? if !fabric {
    /*public static LiteralArgumentBuilder<ServerCommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    /^*
     * Creates a required argument builder.
     *
     * @param name the name of the argument
     * @param type the type of the argument
     * @param <T>  the type of the parsed argument value
     * @return the created argument builder
     ^/
    public static <T> RequiredArgumentBuilder<ServerCommandSource, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }
    *///?}

    //? if fabric {
    static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        //?} else {
        /*static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
         *///?}
        final MinecraftClient client = MinecraftClient.getInstance();
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
                    //client.worldRenderer.captureFrustum();
                    FrustumCuller.DEBUG_LOCK = true;
                    return 1;
                }))
            .then(literal("release")
                .executes(context -> {
                    //client.worldRenderer.killFrustum();
                    FrustumCuller.DEBUG_LOCK = false;
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
                client.send(() -> client.setScreen(ConfigGUI.create(null)));
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
            )
        );
        dispatcher.register(literal(BetterCloudsStatic.MODID + ":dimension")
            .then(literal("enable")
                .executes(context -> {
                    if (client.world == null)
                        return 0;
                    var entry = client.world.getDimensionEntry();
                    var key = entry.getKey().orElse(null);
                    if (key == null)
                        return 0;
                    if (!ConfigManager.instance().enabledDimensions.contains(key)) {
                        ConfigManager.instance().enabledDimensions.add(key);
                    }
                    ConfigManager.handler().save();
                    ChatUtil.debugChatMessage("dimensionAdded", key.getValue().toString());
                    return 1;
                }))
            .then(literal("disable")
                .executes(context -> {
                    if (client.world == null)
                        return 0;
                    var entry = client.world.getDimensionEntry();
                    var key = entry.getKey().orElse(null);
                    if (key == null)
                        return 0;
                    ConfigManager.instance().enabledDimensions.remove(key);
                    ConfigManager.handler().save();
                    ChatUtil.debugChatMessage("dimensionRemoved", key.getValue().toString());
                    return 1;
                })));

        dispatcher.register(literal(BetterCloudsStatic.MODID + ":debug")
            //? if fabric {
            .then(renderdocCommands())
            //?}
            .then(literal("fallback")
                .then(argument("name", FallbackArgumentType.fallback())
                    .executes(context -> {
                        FallbackArgument fallback = FallbackArgumentType.getFallback(context, "name");
                        boolean enabled = fallback.get(GLCompat.glCompat);
                        ChatUtil.debugChatMessage(Text.literal(String.format("Fallback %s is currently %s", fallback.asString(), enabled ? "enabled" : "disabled")));
                        return 1;
                    })
                    .then(argument("enable", BoolArgumentType.bool())
                        .executes(context -> {
                            FallbackArgument fallback = FallbackArgumentType.getFallback(context, "name");
                            boolean enable = BoolArgumentType.getBool(context, "enable");
                            fallback.set(GLCompat.glCompat, enable);
                            client.reloadResources().whenComplete((unused, throwable) -> {
                                ChatUtil.debugChatMessage(Text.literal(String.format("Fallback %s is now %s", fallback.asString(), enable ? "enabled" : "disabled")));
                            });
                            return 1;
                        })))
            )
        );
    }

    // rendedoc can't be loaded before opengl context creation on forge
    //? if fabric {
    private static LiteralArgumentBuilder<FabricClientCommandSource> renderdocCommands() {
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
                                    Text.literal(path.toAbsolutePath().normalize().toString())
                                        .styled(style -> style
                                            .withUnderline(true)
                                            .withClickEvent(createOpenFileClickEvent(path.getParent().toString()))
                                        ));
                            }
                        });
                        return 1;
                    } else if (RenderDocLoader.isAvailable()) {
                        ChatUtil.debugChatMessage(Text.translatable(
                            ChatUtil.debugChatMessageKey("renderdoc.prompt.load"),
                            Text.translatable(ChatUtil.debugChatMessageKey("renderdoc.prompt.load.action"))
                                .styled(style -> style
                                    .withUnderline(true)
                                    .withClickEvent(createCommandClickEvent("/betterclouds:debug renderdoc load")))
                        ));
                        return 0;
                    } else {
                        ChatUtil.debugChatMessage(Text.translatable(
                            ChatUtil.debugChatMessageKey("renderdoc.prompt.install"),
                            Text.translatable(ChatUtil.debugChatMessageKey("renderdoc.prompt.install.action"))
                                .styled(style -> style
                                    .withUnderline(true)
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
                        Text.literal(path.toAbsolutePath().normalize().toString())
                            .styled(style -> style
                                .withUnderline(true)
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
                    ChatUtil.debugChatMessage(Text.translatable(
                        ChatUtil.debugChatMessageKey("renderdoc.prompt.install"),
                        Text.translatable(ChatUtil.debugChatMessageKey("renderdoc.prompt.install.action"))
                            .styled(style -> style
                                .withUnderline(true)
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
    //?}

    private enum FallbackArgument implements StringIdentifiable {
        BASE_INSTANCE(GLCompat::useBaseInstanceFallback, GLCompat::setUseBaseInstanceFallback),
        STENCIL_TEXTURE(GLCompat::useStencilTextureFallback, GLCompat::setUseStencilTextureFallback),
        TEX_STORAGE(GLCompat::useTexStorageFallback, GLCompat::setUseTexStorageFallback),
        DEPTH_WRITE(GLCompat::useDepthWriteFallback, GLCompat::setUseDepthWriteFallback);

        private static final Codec<FallbackArgument> CODEC = StringIdentifiable.createCodec(FallbackArgument::values);

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
        public String asString() {
            return name().toLowerCase();
        }
    }

    private static class FallbackArgumentType extends EnumArgumentType<FallbackArgument> {
        private FallbackArgumentType() {
            super(FallbackArgument.CODEC, FallbackArgument::values);
        }

        public static EnumArgumentType<FallbackArgument> fallback() {
            return new FallbackArgumentType();
        }

        public static FallbackArgument getFallback(CommandContext<?> context, String id) {
            return context.getArgument(id, FallbackArgument.class);
        }
    }

    public static void sendGpuIncompatibleChatMessage() {
        if (!ConfigManager.instance().gpuIncompatibleMessageEnabled) return;
        ChatUtil.debugChatMessage(
            Text.translatable(ChatUtil.debugChatMessageKey("gpuIncompatible"))
                .append(Text.literal("\n - "))
                .append(Text.translatable(ChatUtil.debugChatMessageKey("generic.disable"))
                    .styled(style -> style.withItalic(true).withUnderline(true).withColor(Formatting.GRAY)
                        .withClickEvent(createCommandClickEvent(
                            "/betterclouds:config set gpuIncompatibleMessage false")))));
    }

    public static void sendGpuPartiallyIncompatibleChatMessage() {
        if (!ConfigManager.instance().gpuIncompatibleMessageEnabled) return;
        ChatUtil.debugChatMessage(
            Text.translatable(ChatUtil.debugChatMessageKey("gpuPartiallyIncompatible"))
                .append(Text.literal("\n - "))
                .append(Text.translatable(ChatUtil.debugChatMessageKey("generic.disable"))
                    .styled(style -> style.withItalic(true).withUnderline(true).withColor(Formatting.GRAY)
                        .withClickEvent(createCommandClickEvent(
                            "/betterclouds:config set gpuIncompatibleMessage false")))));
    }

    public static void sendHardwareMaybeIncompatibleChatMessage() {
        if (!ConfigManager.instance().gpuIncompatibleMessageEnabled) return;
        ChatUtil.debugChatMessage(
            Text.translatable(ChatUtil.debugChatMessageKey("hwMaybeIncompatible"), GLCompat.getCpuInfo(), GLCompat.getRenderer())
                .append(Text.literal("\n - "))
                .append(Text.translatable(ChatUtil.debugChatMessageKey("generic.disable"))
                    .styled(style -> style.withItalic(true).withUnderline(true).withColor(Formatting.GRAY)
                        .withClickEvent(createCommandClickEvent(
                            "/betterclouds:config set gpuIncompatibleMessage false")))));
    }

    private static ClickEvent createCommandClickEvent(String command) {
        //? if >=1.21.5 {
        /*return new ClickEvent.RunCommand(command);
        *///?} else {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
         //?}
    }

    private static ClickEvent createOpenFileClickEvent(String path) {
        //? if >=1.21.5 {
        /*return new ClickEvent.OpenFile(path);
        *///?} else {
        return new ClickEvent(ClickEvent.Action.OPEN_FILE, path);
         //?}
    }
}
