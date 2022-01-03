package com.qendolin.betterclouds.config;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;


public class ConfigManager {
    public static final Logger LOGGER = LogManager.getLogger("UntitledConfig");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .create();

    private static boolean isOutside(Path root, Path path) {
        Path parent = path.getParent();
        while (parent != null) {
            if (parent.equals(root))
                return false;
            parent = parent.getParent();
        }
        return true;
    }

    public static <T extends ModConfig> void save(T config) {
        FabricLoader loader = FabricLoader.getInstance();

        String configFileName = config.getId() + ".json";
        Path configPath = loader.getConfigDir().resolve(configFileName);
        if(isOutside(loader.getConfigDir().toAbsolutePath(), configPath.toAbsolutePath())) {
            throw new RuntimeException("Cannot have config file outside config directory");
        }

        writeConfig(config, configPath);
        LOGGER.info("Updated config '{}'.", configFileName);
    }

    private static <T extends ModConfig> void writeConfig(T config, Path configPath) {
        JsonElement json = GSON.toJsonTree(config);
        json.getAsJsonObject().addProperty("__version", config.getVersion());
        String jsonString = GSON.toJson(json);
        try {
            Files.writeString(configPath, jsonString, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("Cannot write config!", e);
        }
    }

    @SuppressWarnings("deprecation")
    public static <T extends ModConfig> T createOrLoad(T defaultConfig) {
        FabricLoader loader = FabricLoader.getInstance();

        String configFileName = defaultConfig.getId() + ".json";
        int configVersion = defaultConfig.getVersion();

        Path configPath = loader.getConfigDir().resolve(configFileName);
        if(isOutside(loader.getConfigDir().toAbsolutePath(), configPath.toAbsolutePath())) {
            throw new RuntimeException("Cannot have config file outside config directory");
        }

        File configFile = configPath.toFile();

        if (configFile.exists()) {
            try (FileReader fileReader = new FileReader(configFile)) {
                JsonReader jsonReader = new JsonReader(fileReader);
                jsonReader.setLenient(true);
                // Static JsonParser.parse is not supported in minecraft 1.17
                JsonParser parser = new JsonParser();
                JsonObject object = parser.parse(fileReader).getAsJsonObject();
                int saveVersion = object.get("__version").getAsInt();
                if(saveVersion == configVersion) {
                    T config = GSON.fromJson(object, (Type) defaultConfig.getClass());
                    LOGGER.info("Loaded config '{}'.", configFileName);
                    return config;
                } else {
                    LOGGER.info("Saved config has old version, making backup and overwriting...");
                    Files.copy(configFile.toPath(), Path.of(loader.getConfigDir().toString(), configFileName.replaceAll("\\.json$", "-old.json")), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                LOGGER.error("Cannot load config!", e);
            }
        }

        writeConfig(defaultConfig, configPath);

        LOGGER.info("Created config '{}'.", configFileName);
        return defaultConfig;
    }
}