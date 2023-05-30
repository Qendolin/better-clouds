package com.qendolin.betterclouds;

import com.qendolin.betterclouds.clouds.Debug;
import com.qendolin.betterclouds.compat.GLCompat;
import dev.isxander.yacl.config.GsonConfigInstance;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL32;

import java.nio.file.Path;


public class Main implements ClientModInitializer {
	public static final String MODID = "betterclouds";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	public static final boolean IS_DEV = FabricLoader.getInstance().isDevelopmentEnvironment();

	public static GLCompat glCompat;

	private static final GsonConfigInstance<Config> CONFIG = GsonConfigInstance
		.createBuilder(Config.class)
		.setPath(Path.of("config/betterclouds-v1.json"))
		// FIXME: This is currently broken, see issue 64
//		.appendGsonBuilder(gsonBuilder -> gsonBuilder.setLenient().setPrettyPrinting())
		.build();

	public static void initGlCompat() {
		glCompat = new GLCompat(IS_DEV);
		if(glCompat.isIncompatible()) {
			LOGGER.warn("Your GPU is not compatible with Better Clouds. OpenGL 4.3 is required!");
			LOGGER.info("Vendor:       {}", GL32.glGetString(GL32.GL_VENDOR));
			LOGGER.info("Renderer:     {}", GL32.glGetString(GL32.GL_RENDERER));
			LOGGER.info("GL Version:   {}", GL32.glGetString(GL32.GL_VERSION));
			LOGGER.info("GLSL Version: {}", GL32.glGetString(GL32.GL_SHADING_LANGUAGE_VERSION));
		}
	}

	public static boolean isProfilingEnabled() {
		return Debug.profileInterval > 0;
	}

	public static void debugChatMessage(String message) {
		debugChatMessage(Text.literal(message));
	}

	public static void debugChatMessage(Text message) {
		MinecraftClient client = MinecraftClient.getInstance();
		if(client == null) return;
		client.inGameHud.getChatHud().addMessage(Text.literal("§e[§bBC§b§e]§r ").append(message));
	}

	public static Config getConfig() {
		return CONFIG.getConfig();
	}

	static GsonConfigInstance<Config> getConfigInstance() {
		return CONFIG;
	}

	@Override
	public void onInitializeClient() {
		CONFIG.load();

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> glCompat.enableDebugOutputSynchronous());

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> Commands.register(dispatcher));

		if(!IS_DEV) return;
		LOGGER.info("Initialized in dev mode, performance might vary");
	}

}
