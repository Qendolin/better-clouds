package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.platform.ModLoader;
import com.qendolin.betterclouds.platform.ModVersion;

public class YACLCompat {

    public static final ModVersion.SemVer Version3_8_0 = new ModVersion.SemVer(3, 8, 0);

    private static ModVersion.SemVer version;

    public static void initialize() {

        BetterCloudsStatic.getLogger().info("YACL: initializing compat");

        ModVersion version = ModLoader.getModVersion("yet_another_config_lib_v3");
        if(version == null) {
            throw new RuntimeException("Could not get YACL version");
        }
        ModVersion.SemVer semver = version.asSemVer()
            .orElseThrow(() -> new RuntimeException("Could not parse YACL version: " + version.getFriendlyString()));

        if (semver.major != 3) {
            throw new RuntimeException("Unsupported YACL version. Must be 3.*.*: " + version.getFriendlyString());
        }

        YACLCompat.version = semver;
    }

    public static ModVersion.SemVer getVersion() {
        return version;
    }

}
