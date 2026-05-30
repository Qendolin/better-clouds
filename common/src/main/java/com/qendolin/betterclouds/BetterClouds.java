package com.qendolin.betterclouds;

import com.qendolin.betterclouds.compat.*;
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.config.preset.PresetLoader;
import com.qendolin.betterclouds.mixin.duck.WorldRendererDuck;
import com.qendolin.betterclouds.generator.RandomPath;
import com.qendolin.betterclouds.platform.EventHooks;
import com.qendolin.betterclouds.platform.ModLoader;
import com.qendolin.betterclouds.renderdoc.RenderDoc;
import com.qendolin.betterclouds.rendering.opengl.OpenGLRenderer;
import com.qendolin.betterclouds.util.ChatUtil;
import com.qendolin.betterclouds.util.DataDirectoryMigration;
import com.qendolin.betterclouds.util.NamedLogger;
import com.qendolin.betterclouds.util.PreLaunchGuard;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.qendolin.betterclouds.compat.GLCompat.glCompat;

public class BetterClouds extends BetterCloudsStatic {

    static {
        PreLaunchGuard.check();
    }

    public static void initializeClientEarly() {
        if (!BetterCloudsStatic.IS_CLIENT)
            throw new IllegalStateException("Minecraft environment is not 'client' but the early client initializer was called");
        if (initializedEarly) return;
        initializedEarly = true;

        logger = new NamedLogger(LogManager.getLogger("BetterClouds"), !IS_DEV);

        ConfigManager.initialize();
        ConfigManager.loadWithFailureBackup();

        version = ModLoader.getModVersion(BetterCloudsStatic.MODID);
    }

    public static void initializeClient() {
        if (!BetterCloudsStatic.IS_CLIENT)
            throw new IllegalStateException("Minecraft environment is not 'client' but the client initializer was called");
        if (isInitialized()) return;
        initialized = true;

        DistantHorizonsCompat.initialize();
        IrisCompat.initialize();
        SereneSeasonsCompat.initialize();
        FabricSeasonsCompat.initialize();
        EnhancedCelestialsCompat.initialize();

        RandomPath.initialize();

        DataDirectoryMigration.runMigration();

        if (!BetterCloudsStatic.IS_DEV) return;
        BetterCloudsStatic.logger.info("Initialized in dev mode, performance might vary");
    }

    public static void initializeClientEvents() {
        EventHooks.instance.onClientStarted(_ -> {
            if (glCompat == null) {
                throw new IllegalStateException("OpenGL compat not initialized yet. This should not happen!");
            }
            glCompat.enableDebugOutputSynchronousDev();
        });
        EventHooks.instance.onWorldJoin(client -> {
            if (glCompat.isIncompatible()) {
                CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS)
                        .execute(() -> client.execute(Commands::sendGpuIncompatibleChatMessage));
            } else if (glCompat.isPartiallyIncompatible()) {
                CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS)
                        .execute(() -> client.execute(Commands::sendGpuPartiallyIncompatibleChatMessage));
            }
            if (HardwareCompat.isMaybeIncompatible()) {
                CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS)
                        .execute(() -> client.execute(Commands::sendHardwareMaybeIncompatibleChatMessage));
            }
            if (RenderDoc.isAvailable()) {
                ChatUtil.debugChatMessage("renderdoc.load.ready", RenderDoc.getAPIVersion());
            }
        });

        PresetLoader.ALL_PRESETS.forEach(presetLoader -> EventHooks.instance.onClientResourcesReload(() -> presetLoader));
        EventHooks.instance.onClientCommandRegistration(Commands::register);
    }

    @Nullable
    public static OpenGLRenderer getCloudsRenderer() {
        Minecraft client = Minecraft.getInstance();
        if (client.levelRenderer instanceof WorldRendererDuck duck) {
            return duck.betterclouds$getRenderer();
        }
        return null;
    }

    public static boolean isEnabled() {
        if (!ConfigManager.isInitialized()) return false;
        Config config = ConfigManager.instance();
        if (!config.enabled) return false;
        return config.irisSupport || !IrisCompat.instance().isShadersEnabled();
    }

    @Deprecated
    public static Config getConfig() {
        return ConfigManager.instance();
    }

    @Deprecated
    public static ConfigClassHandler<Config> getConfigHandler() {
        return ConfigManager.handler();
    }
}
