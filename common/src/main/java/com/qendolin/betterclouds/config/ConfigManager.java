package com.qendolin.betterclouds.config;

import com.google.gson.FieldNamingPolicy;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.platform.ModLoader;
import com.qendolin.betterclouds.util.PreLaunchGuard;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConfigManager {
    public static final Path CONFIG_PATH = ModLoader.getConfigDir().resolve("betterclouds-v1.json");
    public static final Identifier CONFIG_ID = Identifier.fromNamespaceAndPath(BetterCloudsStatic.MODID, "betterclouds-v1");

    private static ConfigClassHandler<Config> config;

    static {
        PreLaunchGuard.check();
    }

    public static ConfigClassHandler<Config> handler() {
        if (config == null) {
            throw new IllegalStateException("Config accessed before it was initialized");
        }
        return config;
    }

    public static Config instance() {
        if (config == null) {
            throw new IllegalStateException("Config accessed before it was initialized");
        }
        return config.instance();
    }

    public static boolean isInitialized() {
        return config != null;
    }

    public static void initialize() {
        if (!BetterCloudsStatic.IS_CLIENT || isInitialized()) return;

        config = ConfigClassHandler.createBuilder(Config.class)
                .id(CONFIG_ID)
                .serializer(config -> GsonConfigSerializerBuilder.create(config)
                        .appendGsonBuilder(b -> b
                                .setLenient()
                                .serializeNulls()
                                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                .setPrettyPrinting()
                                .registerTypeAdapter(Config.class, Config.INSTANCE_CREATOR)
                                .registerTypeAdapter(ShaderPresetConfig.class, ShaderPresetConfig.INSTANCE_CREATOR)
                                .registerTypeAdapter(ResourceKey.class, Config.REGISTRY_KEY_SERIALIZER))
                        .setPath(CONFIG_PATH)
                        .setJson5(false)
                        .build())
                .build();
    }

    public static void loadWithFailureBackup() {
        assert config != null;

        try {
            config.load();
            Migrations.migrate(config.instance());
            return;
        } catch (Exception e) {
            BetterCloudsStatic.getLogger().error("Failed to load config", e);
        }

        File file = CONFIG_PATH.toFile();
        if (file.exists() && file.isFile()) {
            String backupName = FilenameUtils.getBaseName(file.getName()) +
                    "-backup-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) +
                    "." + FilenameUtils.getExtension(file.getName());
            Path backup = Path.of(CONFIG_PATH.toAbsolutePath().getParent().toString(), backupName);
            try {
                Files.copy(file.toPath(), backup, StandardCopyOption.REPLACE_EXISTING);
                BetterCloudsStatic.getLogger().info("Created config backup at: {}", backup);
            } catch (Exception backupException) {
                BetterCloudsStatic.getLogger().error("Failed to create config backup: ", backupException);
            }
        } else if (file.exists()) {
            if (file.delete())
                BetterCloudsStatic.getLogger().info("Deleted old config");
        }

        try {
            config.save();
            BetterCloudsStatic.getLogger().info("Created new config");
            config.load();
        } catch (Exception loadException) {
            BetterCloudsStatic.getLogger().error("Failed to load config again, please report this issue: ", loadException);
        }
    }
}
