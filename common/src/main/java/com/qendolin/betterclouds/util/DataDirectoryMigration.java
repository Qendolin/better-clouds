package com.qendolin.betterclouds.util;

import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.platform.ModLoader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public interface DataDirectoryMigration {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static void runMigration() {
        File oldDir = ModLoader.getGameDir().resolve("better-clouds").toFile();
        if(!oldDir.exists() || !oldDir.isDirectory()) return;

        File newDir = BetterCloudsStatic.getDataDirectory().toFile();
        if(newDir.exists()) return;

        newDir.mkdirs();
        var entries = oldDir.listFiles();
        if(entries == null) return;

        BetterCloudsStatic.getLogger().info("Migrating old data directory 'better-clouds' to new directory 'data/betterclouds'");

        boolean anyError = false;
        for (File entry : entries) {
            try {
                FileUtils.moveToDirectory(entry, newDir, false);
            } catch (IOException ignored) {
                anyError = true;
            }
        }

        if(!anyError) {
            FileUtils.deleteQuietly(oldDir);
        } else {
            BetterCloudsStatic.getLogger().info("Some files could not be moved, keeping old directory");
        }
    }
}
