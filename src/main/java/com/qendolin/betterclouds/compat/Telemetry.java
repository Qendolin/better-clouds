package com.qendolin.betterclouds.compat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qendolin.betterclouds.Main;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL32;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Telemetry {
    public static final String ENDPOINT = "https://europe-west3-better-clouds.cloudfunctions.net/collect_telemetry";
    public static final int CONNECT_TIMEOUT_MS = 5000;
    public static final int READ_TIMEOUT_MS = 5000;
    public static final LocalDateTime EXPIRATION_DATE = LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0);
    public static final int VERSION = 1;
    @Nullable
    public static final Telemetry INSTANCE = tryCreate();
    public static final String SHADER_COMPILE_ERROR = "SHADER_COMPILE_ERROR";
    public static final String SYSTEM_INFORMATION = "SYSTEM_INFORMATION";

    public boolean enabled = true;
    protected final TelemetryCache cache = new TelemetryCache();
    protected final URL url;
    protected final Gson gson = new GsonBuilder()
        .create();

    protected Telemetry(URL url) {
        this.url = url;
        if (LocalDateTime.now().isAfter(EXPIRATION_DATE)) {
            // To prevent errors if the telemetry server shuts down in the future
            Main.LOGGER.info("Telemetry is expired, telemetry will not be sent");
            enabled = false;
        }
        if (Main.IS_DEV) {
            Main.LOGGER.info("Started in dev mode, telemetry will not be sent");
            enabled = false;
        }
    }

    public static Telemetry tryCreate() {
        try {
            URL url = new URL(ENDPOINT);
            return new Telemetry(url);
        } catch (Throwable e) {
            Main.LOGGER.error("Failed to create telemetry service: ", e);
        }
        return null;
    }

    public CompletableFuture<Boolean> sendSystemInfo() {
        return sendPayload("", SYSTEM_INFORMATION);
    }

    protected CompletableFuture<Boolean> sendPayload(String payload, String... labels) {
        if (!enabled) return CompletableFuture.completedFuture(false);
        try {
            RequestBody body = new RequestBody(new SystemDetails(), List.of(labels), payload, Main.getVersion().getFriendlyString(), VERSION);
            String json = gson.toJson(body);
            final byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            return postAsync(bytes);
        } catch (Throwable e) {
            Main.LOGGER.error("Failed to send system information: ", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    protected CompletableFuture<Boolean> postAsync(byte[] body) {
        return CompletableFuture.supplyAsync(() -> post(body));
    }

    protected boolean post(byte[] body) {
        final HttpURLConnection conn = createConnection();
        if (conn == null) return false;
        OutputStream outputStream = null;
        try {
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("Content-Length", "" + body.length);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            outputStream = conn.getOutputStream();
            Main.LOGGER.info("Sending telemetry, see https://github.com/Qendolin/better-clouds/blob/main/Telemetry.md for mor information");
            IOUtils.write(body, outputStream);

            InputStreamReader is = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
            String response = IOUtils.toString(is);
            if (response == null || !response.trim().equalsIgnoreCase("ok")) {
                Main.LOGGER.warn("Failed to post: bad request");
                return false;
            }
            return true;
        } catch (Throwable e) {
            Main.LOGGER.error("Failed to post to telemetry endpoint: ", e);
            return false;
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    protected HttpURLConnection createConnection() {
        if (!enabled) return null;
        try {
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setUseCaches(false);
            return connection;
        } catch (Throwable e) {
            Main.LOGGER.error("Failed to connect to telemetry endpoint: ", e);
            enabled = false;
        }
        return null;
    }

    public void sendShaderCompileError(String error) {
        if (lazyOpenCache()) {
            String hash = cache.hash(error);
            if (cache.contains(SHADER_COMPILE_ERROR, hash)) return;
        }
        sendPayload(error, SHADER_COMPILE_ERROR)
            .whenComplete((success, throwable) -> {
                if (success) {
                    String hash = cache.hash(error);
                    cache.add(SHADER_COMPILE_ERROR, hash);
                }
            });
    }

    protected boolean lazyOpenCache() {
        if (!cache.isOpened()) {
            try {
                cache.open();
            } catch (IOException e) {
                Main.LOGGER.warn("Failed to open telemetry cache: ", e);
            }
        }
        return cache.isAvailable();
    }

    public record RequestBody(SystemDetails systemDetails, List<String> labels, String payload, String modVersion,
                              int telemetryVersion) {
    }

    public static final class SystemDetails {
        public final String os;
        public final String vendor;
        public final String renderer;
        public final String glVersion;
        public final int glVersionMajor;
        public final int glVersionMinor;
        public final String glVersionCombined;
        public final int glVersionLwjgl;
        public final String glslVersion;
        public final List<String> extensions;
        public final List<String> functions;
        public final String cpuName;
        public final boolean compatible;

        public SystemDetails() {
            this.os = SystemUtils.OS_NAME;
            this.vendor = GL32.glGetString(GL32.GL_VENDOR);
            this.renderer = GL32.glGetString(GL32.GL_RENDERER);
            this.glVersion = GL32.glGetString(GL32.GL_VERSION);
            this.glVersionMajor = GL32.glGetInteger(GL32.GL_MAJOR_VERSION);
            this.glVersionMinor = GL32.glGetInteger(GL32.GL_MINOR_VERSION);
            this.glVersionCombined = String.format("%d%d", glVersionMajor, glVersionMinor);
            this.glVersionLwjgl = Main.glCompat.openGlMax;
            this.glslVersion = GL32.glGetString(GL32.GL_SHADING_LANGUAGE_VERSION);
            this.extensions = Main.glCompat.supportedCheckedExtensions;
            this.functions = Main.glCompat.supportedCheckedFunctions;
            this.compatible = !Main.glCompat.isIncompatible();

            String cpuName;
            try {
                CentralProcessor cpu = new SystemInfo().getHardware().getProcessor();
                cpuName = cpu.getProcessorIdentifier().getName().replaceAll("\\s+", " ");
            } catch (Exception ignored) {
                cpuName = "unavailable";
            }
            this.cpuName = cpuName;
        }
    }
}
