package com.qendolin.betterclouds;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.qendolin.betterclouds.compat.GLCompat;
import dev.isxander.yacl.config.GsonConfigInstance;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;


public class Main implements ClientModInitializer {
	public static final String MODID = "betterclouds";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	public static final boolean IS_DEV = FabricLoader.getInstance().isDevelopmentEnvironment();
	public static int profileInterval = 0;

	public static GLCompat glCompat;

	private static final GsonConfigInstance<Config> CONFIG = GsonConfigInstance
		.createBuilder(Config.class)
		.setPath(Path.of("config/betterclouds-v1.json"))
		// FIXME: This is currently broken, see issue 64
//		.appendGsonBuilder(gsonBuilder -> gsonBuilder.setLenient().setPrettyPrinting())
		.build();

	private static final KeyBinding captureFrustumKeybind = IS_DEV ? KeyBindingHelper.registerKeyBinding(new KeyBinding(
		"key."+MODID+".captureFrustum",
		InputUtil.Type.KEYSYM,
		GLFW.GLFW_KEY_J,
		"category."+MODID+".debug"
	)) : null;

	public static void initGlCompat() {
		glCompat = new GLCompat(IS_DEV);
		if(glCompat.isIncompatible()) {
			LOGGER.warn("Your GPU is not compatible with Better Clouds. OpenGL 4.3 is required!");
		}
	}

	public static boolean isProfilingEnabled() {
		return profileInterval > 0;
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

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			glCompat.enableDebugOutputSynchronous();
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
			ClientCommandManager.literal(MODID+":profile")
				.then(ClientCommandManager.argument("interval", IntegerArgumentType.integer(30))
					.executes(context -> {
						int interval = IntegerArgumentType.getInteger(context, "interval");
						debugChatMessage(String.format("Enabled profiling over %d frames, performance will suffer.", interval));
						profileInterval = interval;
						return 1;
					}))
				.then(ClientCommandManager.literal("stop")
					.executes(context -> {
						debugChatMessage("Disabled profiling.");
						profileInterval = 0;
						return 1;
					}))
		));

		if(!IS_DEV) return;
		LOGGER.info("Initialized in dev mode, performance might vary");

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (captureFrustumKeybind.wasPressed()) {
				if(InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
					client.worldRenderer.killFrustum();
					client.player.sendMessage(Text.literal("Frustum released"), false);
				} else {
					client.worldRenderer.captureFrustum();
					client.player.sendMessage(Text.literal("Frustum captured"), false);
				}
			}
		});
	}

}
