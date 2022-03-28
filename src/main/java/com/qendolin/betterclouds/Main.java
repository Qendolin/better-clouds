package com.qendolin.betterclouds;

import com.qendolin.betterclouds.config.ConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Main implements ClientModInitializer {
	public static final String MODID = "betterclouds";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	public static final Config CONFIG = ConfigManager.createOrLoad(new Config());
	public static final boolean IS_DEV = FabricLoader.getInstance().isDevelopmentEnvironment();

	@Override
	public void onInitializeClient() {
		LOGGER.info("Initialized.");
	}
}
