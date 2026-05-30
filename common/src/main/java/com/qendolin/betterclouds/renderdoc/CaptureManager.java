package com.qendolin.betterclouds.renderdoc;

import com.qendolin.betterclouds.BetterCloudsStatic;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class CaptureManager {

    public static final Path LAUNCH_CONFIG_PATH = BetterCloudsStatic.getDataDirectory().resolve("capture.conf");

    private static final List<Map.Entry<Long, Runnable>> callbacks = new ArrayList<>();
    private static final AtomicLong frameIndex = new AtomicLong(0);

    public static void capture(Consumer<RenderDoc.Capture> callback) {
        int captureIndex = RenderDoc.getNumCaptures();
        RenderDoc.triggerCapture();
        synchronized (callbacks) {
            callbacks.add(Map.entry(frameIndex.get() + 2, () -> {
                callback.accept(RenderDoc.getCapture(captureIndex));
            }));
        }
    }

    public static void writeLaunchConfig(LaunchConfig config) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("load", String.valueOf(config.load));
        properties.setProperty("once", String.valueOf(config.once));
        properties.setProperty("expires", String.valueOf(config.expires));

        try (Writer writer = Files.newBufferedWriter(LAUNCH_CONFIG_PATH, StandardCharsets.UTF_8)) {
            properties.store(writer, "RenderDoc Launch Configuration");
        }
    }

    public static LaunchConfig readLaunchConfig() {
        if (!Files.exists(LAUNCH_CONFIG_PATH))
            return new LaunchConfig(false, false, 0);

        try (Reader reader = Files.newBufferedReader(LAUNCH_CONFIG_PATH, StandardCharsets.UTF_8)) {
            Properties properties = new Properties();
            properties.load(reader);
            boolean load = Boolean.parseBoolean(properties.getProperty("load", "false"));
            boolean once = Boolean.parseBoolean(properties.getProperty("once", "false"));
            long expires = Long.parseLong(properties.getProperty("expires", "0"));
            return new LaunchConfig(load, once, expires);
        } catch (IOException | NumberFormatException e) {
            BetterCloudsStatic.getLogger().error("Failed to read RenderDoc launch configuration", e);
            return new LaunchConfig(false, false, 0);
        }
    }

    public static void deleteLaunchConfig() {
        try {
            Files.delete(LAUNCH_CONFIG_PATH);
        } catch (IOException ignored) {
        }
    }

    public static void onSwapBuffers() {
        long idx = frameIndex.getAndIncrement();
        if (callbacks.isEmpty()) return;
        Iterator<Map.Entry<Long, Runnable>> iterator = callbacks.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Runnable> entry = iterator.next();
            if (idx >= entry.getKey()) {
                entry.getValue().run();
                iterator.remove();
            }
        }
    }

    public record LaunchConfig(
            boolean load,
            boolean once,
            long expires
    ) {
        public boolean isExpired() {
            return System.currentTimeMillis() >= expires;
        }
    }
}
