package com.qendolin.betterclouds;

import com.qendolin.betterclouds.config.ConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;


public class Main implements ClientModInitializer {
	public static final String MODID = "betterclouds";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	public static final Config CONFIG = ConfigManager.createOrLoad(new Config());
	public static final boolean IS_DEV = false && FabricLoader.getInstance().isDevelopmentEnvironment();
	public static final boolean DO_PROFILE = IS_DEV && false;

	private static final KeyBinding captureFrustum = IS_DEV ? KeyBindingHelper.registerKeyBinding(new KeyBinding(
		"key."+MODID+".captureFrustum", // The translation key of the keybinding's name
		InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
		GLFW.GLFW_KEY_J, // The keycode of the key
		"category."+MODID+".debug" // The translation key of the keybinding's category.
	)) : null;

	@Override
	public void onInitializeClient() {
		if(IS_DEV) {
			LOGGER.info("Initialized in dev mode, performance might vary");

			ClientTickEvents.END_CLIENT_TICK.register(client -> {
				while (captureFrustum.wasPressed()) {
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

	public static void bcObjectLabel(int type, int name, String label) {
		if(!IS_DEV) return;
		String typeString = switch (type) {
			case GL43.GL_TEXTURE -> "tex";
			case GL43.GL_BUFFER -> "buf";
			case GL43.GL_VERTEX_ARRAY -> "va";
			case GL43.GL_FRAMEBUFFER -> "fb";
			case GL43.GL_SHADER -> "sh";
			case GL43.GL_PROGRAM -> "shp";
			case GL43.GL_QUERY -> "qry";
			default -> "unk";
		};
		GLCapabilities caps = GL.getCapabilities();
		if(caps.OpenGL43) {
			GL43.glObjectLabel(type, name, Main.MODID + ":" + label + ":" + typeString);
		} else if(caps.GL_KHR_debug) {
			KHRDebug.glObjectLabel(type, name, Main.MODID + ":" + label + ":" + typeString);
		}
	}

	public static void bcPushDebugGroup(String name) {
		if(!IS_DEV) return;
		GLCapabilities caps = GL.getCapabilities();
		if(caps.OpenGL43) {
			GL43.glPushDebugGroup(GL43.GL_DEBUG_SOURCE_APPLICATION, 1337, name+"\0");
		} else if(caps.GL_KHR_debug) {
			KHRDebug.glPushDebugGroup(GL43.GL_DEBUG_SOURCE_APPLICATION, 1337, name+"\0");
		}
	}

	public static void bcPopDebugGroup() {
		if(!IS_DEV) return;
		GLCapabilities caps = GL.getCapabilities();
		if(caps.OpenGL43) {
			GL43.glPopDebugGroup();
		} else if(caps.GL_KHR_debug) {
			KHRDebug.glPopDebugGroup();
		}
	}

}
