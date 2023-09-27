package com.qendolin.betterclouds;

import com.google.gson.FieldNamingPolicy;
import com.qendolin.betterclouds.clouds.Debug;
import com.qendolin.betterclouds.compat.GLCompat;
import com.qendolin.betterclouds.compat.Telemetry;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.impl.util.version.StringVersion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.opengl.GL32;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


public class Main implements ClientModInitializer {
    public static final String MODID = "betterclouds";
    public static final boolean IS_DEV = FabricLoader.getInstance().isDevelopmentEnvironment();
    public static final boolean IS_CLIENT = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    public static final NamedLogger LOGGER = new NamedLogger(LogManager.getLogger(MODID), !IS_DEV);

    public static GLCompat glCompat;
    public static Version version;

    private static final ConfigClassHandler<Config> CONFIG;
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("betterclouds-v1.json");

    static {
        if (IS_CLIENT) {
            CONFIG = ConfigClassHandler.createBuilder(Config.class)
                .id(new Identifier(MODID, "betterclouds-v1"))
                .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .appendGsonBuilder(b -> b
                        .setLenient()
                        .serializeNulls()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .setPrettyPrinting()
                        .registerTypeAdapter(Config.class, Config.INSTANCE_CREATOR)
                        .registerTypeAdapter(Config.ShaderConfigPreset.class, Config.ShaderConfigPreset.INSTANCE_CREATOR))
                    .setPath(CONFIG_PATH)
                    .setJson5(false)
                    .build())
                .build();
        } else {
            CONFIG = null;
        }

    }

    public static void initGlCompat() {
        try {
            glCompat = new GLCompat(IS_DEV);
        } catch (Exception e) {
            Telemetry.INSTANCE.sendUnhandledException(e);
            throw e;
        }

        if (glCompat.isIncompatible()) {
            LOGGER.warn("Your GPU is not compatible with Better Clouds. Try updating your drivers?");
            LOGGER.info(" - Vendor:       {}", GL32.glGetString(GL32.GL_VENDOR));
            LOGGER.info(" - Renderer:     {}", GL32.glGetString(GL32.GL_RENDERER));
            LOGGER.info(" - GL Version:   {}", GL32.glGetString(GL32.GL_VERSION));
            LOGGER.info(" - GLSL Version: {}", GL32.glGetString(GL32.GL_SHADING_LANGUAGE_VERSION));
            LOGGER.info(" - Extensions:   {}", String.join(", ", glCompat.supportedCheckedExtensions));
            LOGGER.info(" - Functions:    {}", String.join(", ", glCompat.supportedCheckedFunctions));
        } else if (glCompat.isPartiallyIncompatible()) {
            LOGGER.warn("Your GPU is not fully compatible with Better Clouds.");
            for (String fallback : glCompat.usedFallbacks) {
                LOGGER.info("- Using {} fallback", fallback);
            }
        }

        if (getConfig().lastTelemetryVersion < Telemetry.VERSION) {
            Telemetry.INSTANCE.sendSystemInfo()
                .whenComplete((success, throwable) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (success && client != null) {
                        client.execute(() -> {
                            getConfig().lastTelemetryVersion = Telemetry.VERSION;
                            CONFIG.serializer().save();
                        });
                    }
                });
        }
    }

    public static Config getConfig() {
        return CONFIG.instance();
    }

    public static boolean isProfilingEnabled() {
        return Debug.profileInterval > 0;
    }

    public static void debugChatMessage(String id, Object... args) {
        debugChatMessage(Text.translatable(debugChatMessageKey(id), args));
    }

    public static void debugChatMessage(Text message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return;
        client.inGameHud.getChatHud().addMessage(Text.literal("§e[§bBC§b§e]§r ").append(message));
    }

    public static String debugChatMessageKey(String id) {
        return MODID + ".message." + id;
    }

    public static Version getVersion() {
        return version;
    }

    public static ConfigClassHandler<Config> getConfigHandler() {
        return CONFIG;
    }

    @Override
    public void onInitializeClient() {
        if (!IS_CLIENT)
            throw new IllegalStateException("Fabric environment is " + FabricLoader.getInstance().getEnvironmentType().name() + " but onInitializeClient was called");
        loadConfig();

        ModContainer mod = FabricLoader.getInstance().getModContainer(MODID).orElse(null);
        if (mod != null) version = mod.getMetadata().getVersion();
        else version = new StringVersion("unknown");

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> glCompat.enableDebugOutputSynchronousDev());

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (glCompat.isIncompatible()) {
                CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS)
                    .execute(() -> client.execute(Main::sendGpuIncompatibleChatMessage));
            } else if (glCompat.isPartiallyIncompatible()) {
                CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS)
                    .execute(() -> client.execute(Main::sendGpuPartiallyIncompatibleChatMessage));
            }
        });

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
            .registerReloadListener(ShaderPresetLoader.INSTANCE);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> Commands.register(dispatcher));

        if (!IS_DEV) return;
        LOGGER.info("Initialized in dev mode, performance might vary");
    }

    private void loadConfig() {
        assert CONFIG != null;

        try {
            CONFIG.serializer().load();
            return;
        } catch (Exception loadException) {
            LOGGER.error("Failed to load config: ", loadException);
        }

        File file = CONFIG_PATH.toFile();
        if(file.exists() && file.isFile()) {
            String backupName = FilenameUtils.getBaseName(file.getName()) +
                "-backup-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) +
                "." + FilenameUtils.getExtension(file.getName());
            Path backup = Path.of(CONFIG_PATH.toAbsolutePath().getParent().toString(), backupName);
            try {
                Files.copy(file.toPath(), backup, StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Created config backup at: {}", backup);
            } catch (Exception backupException) {
                LOGGER.error("Failed to create config backup: ", backupException);
            }
        } else if(file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            LOGGER.info("Deleted old config");
        }

        try {
            CONFIG.serializer().save();
            LOGGER.info("Created new config");
            CONFIG.serializer().load();
        } catch (Exception loadException) {
            LOGGER.error("Failed to load config again, please report this issue: ", loadException);
        }
    }

    public static void sendGpuIncompatibleChatMessage() {
        if (!getConfig().gpuIncompatibleMessageEnabled) return;
        debugChatMessage(
            Text.translatable(debugChatMessageKey("gpuIncompatible"))
                .append(Text.literal("\n - "))
                .append(Text.translatable(debugChatMessageKey("disable"))
                    .styled(style -> style.withItalic(true).withUnderline(true).withColor(Formatting.GRAY)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/betterclouds:config gpuIncompatibleMessage false")))));
    }

    public static void sendGpuPartiallyIncompatibleChatMessage() {
        if (!getConfig().gpuIncompatibleMessageEnabled) return;
        debugChatMessage(
            Text.translatable(debugChatMessageKey("gpuPartiallyIncompatible"))
                .append(Text.literal("\n - "))
                .append(Text.translatable(debugChatMessageKey("disable"))
                    .styled(style -> style.withItalic(true).withUnderline(true).withColor(Formatting.GRAY)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/betterclouds:config gpuIncompatibleMessage false")))));
    }

}
