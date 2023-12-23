package com.qendolin.betterclouds.renderdoc;

import javax.security.auth.callback.Callback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CaptureManager {

    public static final Path LAUNCH_CONFIG_PATH = Path.of("./better-clouds/capture.conf");

    private static final List<Map.Entry<Long, Runnable>> callbacks = new ArrayList<>();
    private static final AtomicLong frameIndex = new AtomicLong(0);

    public static void capture(Consumer<RenderDoc.Capture> callback) {
        int captureIndex = RenderDoc.getNumCaptures();
        RenderDoc.triggerCapture();
        synchronized (callbacks) {
            callbacks.add(Map.entry(frameIndex.get()+2, () -> {
                callback.accept(RenderDoc.getCapture(captureIndex));
            }));
        }
    }

    public static void writeLaunchConfig(LaunchConfig config) throws IOException {
        String str = "";
        str += "load=" + config.load + "\n";
        str += "once=" + config.once + "\n";
        str += "expires=" + config.expires + "\n";
        Files.writeString(LAUNCH_CONFIG_PATH, str, StandardCharsets.UTF_8);
    }

    public static LaunchConfig readLaunchConfig() {
        try {
            List<String> confLines = Files.readAllLines(LAUNCH_CONFIG_PATH, StandardCharsets.UTF_8);
            Map<String,String> conf = confLines.stream()
                .map(line -> line.split("="))
                .collect(Collectors.toMap(kvp -> kvp[0], kvp -> kvp[1]));
            boolean load = conf.get("load").equalsIgnoreCase("true");
            boolean once = conf.get("once").equalsIgnoreCase("true");
            long expires = Long.parseLong(conf.get("expires"));
            return new LaunchConfig(load, once, expires);
        } catch (Exception e) {
            return new LaunchConfig(false, false, 0);
        }
    }

    public static void deleteLaunchConfig() {
        try {
            Files.delete(LAUNCH_CONFIG_PATH);
        } catch (IOException ignored) {}
    }

    public static void onSwapBuffers() {
        long idx = frameIndex.getAndIncrement();
        if(callbacks.isEmpty()) return;
        Iterator<Map.Entry<Long, Runnable>> iterator = callbacks.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Runnable> entry = iterator.next();
            if(idx >= entry.getKey()) {
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
