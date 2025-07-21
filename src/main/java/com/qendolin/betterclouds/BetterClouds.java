package com.qendolin.betterclouds;

import com.qendolin.betterclouds.clouds.RandomPath;
import com.qendolin.betterclouds.clouds.Renderer;
import com.qendolin.betterclouds.compat.*;
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.config.ShaderPresetLoader;
import com.qendolin.betterclouds.duck.WorldRendererDuck;
import com.qendolin.betterclouds.platform.EventHooks;
import com.qendolin.betterclouds.platform.ModLoader;
import com.qendolin.betterclouds.renderdoc.RenderDoc;
import com.qendolin.betterclouds.test.GameTest;
import com.qendolin.betterclouds.test.GameTestEnabled;
import com.qendolin.betterclouds.util.ChatUtil;
import com.qendolin.betterclouds.util.DataDirectoryMigration;
import com.qendolin.betterclouds.util.NamedLogger;
import com.qendolin.betterclouds.util.PreLaunchGuard;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.MixinEnvironment;

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

        YACLCompat.initialize();
        DistantHorizonsCompat.initialize();
        IrisCompat.initialize();
        SereneSeasonsCompat.initialize();
        FabricSeasonsCompat.initialize();
        EnhancedCelestialsCompat.initialize();

        RandomPath.initialize();

        DataDirectoryMigration.runMigration();

        if (!BetterCloudsStatic.IS_DEV) return;
        BetterCloudsStatic.logger.info("Initialized in dev mode, performance might vary");

        if (GameTestEnabled.ENABLED) {
            BetterCloudsStatic.logger.info("Running mixin audit. No additional mods must be loaded!");
            MixinEnvironment.getCurrentEnvironment().audit();
        }
    }

    public static void initializeClientEvents() {
        EventHooks.instance.onClientStarted(client -> {
            if (glCompat == null) {
                throw new IllegalStateException("OpenGL compat not initialized yet. This should not happen!");
            }
            glCompat.enableDebugOutputSynchronousDev();

            if (GameTestEnabled.ENABLED) {
                GameTest.run(client);
            }
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
        EventHooks.instance.onClientResourcesReload(() -> ShaderPresetLoader.INSTANCE);
        EventHooks.instance.onClientCommandRegistration(Commands::register);
    }

    @Nullable
    public static Renderer getCloudsRenderer() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return null;
        if (client.worldRenderer instanceof WorldRendererDuck duck) {
            return duck.betterclouds$getRenderer();
        }
        return null;
    }

    public static boolean isEnabled() {
        if (!ConfigManager.isInitialized()) return false;
        Config config = ConfigManager.instance();
        if(!config.enabled) return false;
        if(!config.irisSupport && IrisCompat.instance().isShadersEnabled()) return false;
        return true;
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
