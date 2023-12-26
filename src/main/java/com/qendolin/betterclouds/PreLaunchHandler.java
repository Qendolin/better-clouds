package com.qendolin.betterclouds;

import com.qendolin.betterclouds.renderdoc.CaptureManager;
import com.qendolin.betterclouds.renderdoc.RenderDoc;
import com.qendolin.betterclouds.renderdoc.RenderDocLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.apache.logging.log4j.LogManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PreLaunchHandler implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        NamedLogger logger = new NamedLogger(LogManager.getLogger("BetterClouds PreLaunch"), !FabricLoader.getInstance().isDevelopmentEnvironment());
        try {
            CaptureManager.LaunchConfig config = CaptureManager.readLaunchConfig();
            if(config.isExpired()) {
                CaptureManager.deleteLaunchConfig();
                return;
            }
            if(!config.load()) {
                return;
            }
            if(!RenderDocLoader.isAvailable()) {
                logger.info("RenderDoc is not available");
                return;
            }
            RenderDocLoader.load();
            if(!RenderDoc.isAvailable()) {
                logger.info("RenderDoc is not available");
                return;
            }

            Path captureTemplatePath = Path.of("./better-clouds/captures/capture");
            Files.createDirectories(captureTemplatePath.getParent());
            RenderDoc.setCaptureOption(RenderDoc.CaptureOption.API_VALIDATION, true);
            RenderDoc.disableOverlayOptions(RenderDoc.OverlayOption.ENABLED);
            RenderDoc.setCaptureKeys();
            RenderDoc.setCaptureFilePathTemplate(captureTemplatePath.toString());
            logger.info("RenderDoc loaded and ready");
            if(config.once()) {
                try {
                    CaptureManager.writeLaunchConfig(new CaptureManager.LaunchConfig(false, true, config.expires()));
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            logger.error("RenderDoc could not be loaded: {}", e);
        }
    }
}
