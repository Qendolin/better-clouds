package com.qendolin.betterclouds.config;

import com.qendolin.betterclouds.compat.BigGlobeCompat;
import com.qendolin.betterclouds.compat.MiddleEarthCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Migrations {

    private static final List<Consumer<Config>> MIGRATIONS = new ArrayList<>();

    static {
        MIGRATIONS.add(config -> {
            if (!config.enabledDimensions.contains(BigGlobeCompat.DIMENSION_KEY)) {
                config.enabledDimensions.add(BigGlobeCompat.DIMENSION_KEY);
            }
        });
        MIGRATIONS.add(config -> {
            if (!config.enabledDimensions.contains(MiddleEarthCompat.DIMENSION_KEY)) {
                config.enabledDimensions.add(MiddleEarthCompat.DIMENSION_KEY);
            }
        });
        MIGRATIONS.add(config -> {
            // todo: remove a few versions later
            config.shaderPreset().topColorRed = config.shaderPreset().tintRed;
            config.shaderPreset().topColorGreen = config.shaderPreset().tintGreen;
            config.shaderPreset().topColorBlue = config.shaderPreset().tintBlue;
            config.shaderPreset().bottomColorRed = config.shaderPreset().bottomTintRed;
            config.shaderPreset().bottomColorGreen = config.shaderPreset().bottomTintGreen;
            config.shaderPreset().bottomColorBlue = config.shaderPreset().bottomTintBlue;
        });
    }

    public static int getCurrentVersion() {
        return MIGRATIONS.size();
    }

    public static void migrate(Config config) {
        try {
            while (config.migrationVersion < MIGRATIONS.size()) {
                MIGRATIONS.get(config.migrationVersion).accept(config);
                config.migrationVersion++;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to migrate config at version " + config.migrationVersion, e);
        }
    }
}
