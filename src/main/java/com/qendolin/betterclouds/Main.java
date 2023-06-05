package com.qendolin.betterclouds;

import com.qendolin.betterclouds.clouds.Debug;
import com.qendolin.betterclouds.compat.GLCompat;
import com.qendolin.betterclouds.compat.GsonConfigInstanceBuilderDuck;
import com.qendolin.betterclouds.compat.Telemetry;
import dev.isxander.yacl.config.GsonConfigInstance;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL32;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


public class Main implements ClientModInitializer {
	public static final String MODID = "betterclouds";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	public static final boolean IS_DEV = FabricLoader.getInstance().isDevelopmentEnvironment();

	public static GLCompat glCompat;
	public static Version version;

	private static final GsonConfigInstance<Config> CONFIG;

	static {
		if(FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT)) {
			GsonConfigInstance.Builder<Config> builder = GsonConfigInstance
				.createBuilder(Config.class)
				.setPath(Path.of("config/betterclouds-v1.json"));

			if(builder instanceof GsonConfigInstanceBuilderDuck) {
				//noinspection unchecked
				GsonConfigInstanceBuilderDuck<Config> duck = (GsonConfigInstanceBuilderDuck<Config>) builder;
				builder = duck.betterclouds$appendGsonBuilder(b -> b.setLenient().setPrettyPrinting());
			}
			CONFIG = builder.build();
		} else {
			CONFIG = null;
		}
	}

	public static void initGlCompat() {
		glCompat = new GLCompat(IS_DEV);
		if(glCompat.isIncompatible()) {
			LOGGER.warn("Your GPU is not compatible with Better Clouds. OpenGL 4.3 is required!");
			LOGGER.info(" - Vendor:       {}", GL32.glGetString(GL32.GL_VENDOR));
			LOGGER.info(" - Renderer:     {}", GL32.glGetString(GL32.GL_RENDERER));
			LOGGER.info(" - GL Version:   {}", GL32.glGetString(GL32.GL_VERSION));
			LOGGER.info(" - GLSL Version: {}", GL32.glGetString(GL32.GL_SHADING_LANGUAGE_VERSION));
			LOGGER.info(" - Extensions:   {}", String.join(", ", glCompat.supportedCheckedExtensions));
			LOGGER.info(" - Functions:    {}", String.join(", ", glCompat.supportedCheckedFunctions));
		}
		if(getConfig().lastTelemetryVersion < Telemetry.VERSION && Telemetry.INSTANCE != null) {
			Telemetry.INSTANCE.sendSystemInfo()
			.whenComplete((success, throwable) -> {
				MinecraftClient client = MinecraftClient.getInstance();
				if(success && client != null) {
					client.execute(() -> {
						getConfig().lastTelemetryVersion = Telemetry.VERSION;
						CONFIG.save();
					});
				}
			});
		}
	}

	public static void sendGpuIncompatibleChatMessage() {
		if(!getConfig().gpuIncompatibleMessageEnabled) return;
		debugChatMessage(
			Text.translatable(debugChatMessageKey("gpuIncompatible"))
				.append(Text.literal("\n - "))
				.append(Text.translatable(debugChatMessageKey("gpuIncompatible.disable"))
					.styled(style -> style.withItalic(true).withUnderline(true).withColor(Formatting.GRAY)
						.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
							"/betterclouds:config gpuIncompatibleMessage false")))));
	}

	public static boolean isProfilingEnabled() {
		return Debug.profileInterval > 0;
	}

	public static String debugChatMessageKey(String id) {
		return MODID + ".message." + id;
	}

	public static void debugChatMessage(String id, Object ...args) {
		debugChatMessage(Text.translatable(debugChatMessageKey(id), args));
	}

	public static void debugChatMessage(Text message) {
		MinecraftClient client = MinecraftClient.getInstance();
		if(client == null) return;
		client.inGameHud.getChatHud().addMessage(Text.literal("§e[§bBC§b§e]§r ").append(message));
	}

	public static Config getConfig() {
		return CONFIG.getConfig();
	}

	public static Version getVersion() {
		return version;
	}

	static GsonConfigInstance<Config> getConfigInstance() {
		return CONFIG;
	}

	@Override
	public void onInitializeClient() {
		if(CONFIG == null) throw new IllegalStateException("Fabric environment is "+FabricLoader.getInstance().getEnvironmentType().name() +" but onInitializeClient was called");
		CONFIG.load();

		ModContainer mod = FabricLoader.getInstance().getModContainer(MODID).orElse(null);
		if(mod != null) version = mod.getMetadata().getVersion();
		else version = new StringVersion("unknown");

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> glCompat.enableDebugOutputSynchronous());

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if(!glCompat.isIncompatible()) return;
			CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS)
					.execute(() -> client.execute(Main::sendGpuIncompatibleChatMessage));
		});

		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
				.registerReloadListener(ShaderPresetLoader.INSTANCE);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> Commands.register(dispatcher));

		if(!IS_DEV) return;
		LOGGER.info("Initialized in dev mode, performance might vary");
	}

}
