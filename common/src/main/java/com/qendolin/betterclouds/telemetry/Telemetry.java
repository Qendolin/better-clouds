package com.qendolin.betterclouds.telemetry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qendolin.betterclouds.BetterClouds;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.compat.GLCompat;
import com.qendolin.betterclouds.platform.ModVersion;
import net.minecraft.MinecraftVersion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.ReportType;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.lwjgl.opengl.GL32;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

public class Telemetry implements ITelemetry {
    public static final String ENDPOINT = "https://europe-west3-better-clouds.cloudfunctions.net/collect_telemetry";
    public static final String ENABLED_LABELS_ENDPOINT = "https://storage.googleapis.com/better-clouds-static/v1/enabled_telemetry_labels.txt";
    public static final int CONNECT_TIMEOUT_MS = 5000;
    public static final int READ_TIMEOUT_MS = 5000;
    public static final int VERSION = 3;

    public enum Label {
        SHADER_COMPILE_ERROR("SHADER_COMPILE_ERROR"),
        SYSTEM_INFORMATION("SYSTEM_INFORMATION"),
        UNHANDLED_EXCEPTION("UNHANDLED_EXCEPTION"),
        AUTO_REPORT("AUTO_REPORT");

        private final String string;

        Label(String string) {
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }
    }

    protected boolean enabled = true;
    protected final ReentrantLock enabledLabelsLock = new ReentrantLock();
    protected final Set<String> enabledLabels = new HashSet<>();
    protected final TelemetryCache cache = new TelemetryCache();
    protected final URL url;
    protected final Gson gson = new GsonBuilder()
        .create();

    protected Telemetry(URL url) {
        this.url = url;
        if (BetterCloudsStatic.IS_DEV) {
            BetterCloudsStatic.getLogger().info("Started in dev mode, telemetry will not be sent");
            enabled = false;
        }
        loadEnabledLabels();
    }

    private void loadEnabledLabels() {
        CompletableFuture.runAsync(() -> {
            try {
                enabledLabelsLock.lock();
                URL url = new URI(ENABLED_LABELS_ENDPOINT).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                int responseCode = conn.getResponseCode();
                if (responseCode < 200 || responseCode >= 300) {
                    BetterCloudsStatic.getLogger().warn("Failed to get enabled telemetry labels: responseCode={}", responseCode);
                    return;
                }
                Set<String> labels = new HashSet<>();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    while (true) {
                        String line = reader.readLine();
                        if (line == null) break;
                        if (!line.isBlank()) {
                            labels.add(line.toUpperCase());
                        }
                    }
                }
                enabledLabels.clear();
                enabledLabels.addAll(labels);
            } catch (Exception e) {
                BetterCloudsStatic.getLogger().warn("Failed to get enabled telemetry labels", e.getMessage());
            } finally {
                enabledLabelsLock.unlock();
            }
        });
    };

    public CompletableFuture<Boolean> sendSystemInfo() {
        return sendPayload("", Label.SYSTEM_INFORMATION);
    }

    protected CompletableFuture<Boolean> sendPayload(String payload, Label... labels) {
        if (!enabled)
            return CompletableFuture.completedFuture(false);
        if(!allLabelsEnabled(labels))
            return CompletableFuture.completedFuture(false);

        try {
            List<String> stringLabels = Arrays.stream(labels).map(Label::toString).toList();
            RequestBody body = new RequestBody(new SystemDetails(), stringLabels, payload, BetterClouds.getVersion(), VERSION);
            String json = gson.toJson(body);
            final byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            return postAsync(bytes);
        } catch (Throwable e) {
            BetterCloudsStatic.getLogger().error("Failed to send system information", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    private boolean allLabelsEnabled(Label[] labels) {
        try {
            enabledLabelsLock.lock();
            return Arrays.stream(labels).map(Label::toString).allMatch(enabledLabels::contains);
        } finally {
            enabledLabelsLock.unlock();
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
            BetterCloudsStatic.getLogger().info("Sending telemetry, see https://github.com/Qendolin/better-clouds/blob/main/Telemetry.md for mor information");
            IOUtils.write(body, outputStream);

            InputStreamReader is = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
            String response = IOUtils.toString(is);
            if (response == null || !response.trim().equalsIgnoreCase("ok")) {
                BetterCloudsStatic.getLogger().warn("Failed to post: bad request");
                return false;
            }
            return true;
        } catch (Throwable e) {
            BetterCloudsStatic.getLogger().error("Failed to post to telemetry endpoint", e);
            return false;
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    protected HttpURLConnection createConnection() {
        if (!enabled)
            return null;
        try {
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setUseCaches(false);
            return connection;
        } catch (Throwable e) {
            BetterCloudsStatic.getLogger().error("Failed to connect to telemetry endpoint", e);
            enabled = false;
        }
        return null;
    }

    public void sendShaderCompileError(String error) {
        if (error == null || error.isBlank()) return;

        cachedSend(error, Label.SHADER_COMPILE_ERROR);
    }

    private void cachedSend(String error, Label label) {
        if (lazyOpenCache()) {
            String hash = cache.hash(error);
            if (cache.contains(label.toString(), hash)) return;
        }
        sendPayload(error, label)
            .whenComplete((success, throwable) -> {
                if (success) {
                    String hash = cache.hash(error);
                    cache.add(label.toString(), hash);
                }
            });
    }

    protected boolean lazyOpenCache() {
        if (!cache.isOpened()) {
            try {
                cache.open();
            } catch (IOException e) {
                BetterCloudsStatic.getLogger().warn("Failed to open telemetry cache", e);
            }
        }
        return cache.isAvailable();
    }

    public void sendUnhandledException(Exception e) {
        if (e == null) return;
        String message = ExceptionUtils.getStackTrace(e);
        cachedSend(message, Label.UNHANDLED_EXCEPTION);
    }

    @Override
    public void sendIssueReport(CrashReport report) {
        if (report == null) return;
        String shortReportText = report.getMessage() + "\n\n" + report.getCauseAsString();
        String fullReportText = report.asString(ReportType.MINECRAFT_TEST_REPORT);
        new McLogsUploader().upload(fullReportText)
            .thenAccept(logUrl -> {
                MinecraftClient.getInstance().send(() -> {
                    sendPayload(shortReportText + "\n\n" + "Full Report at: " + logUrl, Label.AUTO_REPORT);
                });
            })
            .whenComplete((success, e) -> {
                if(e != null)
                    BetterCloudsStatic.getLogger().warn("Failed to upload issue report", e);
            });
    }

    public static final class RequestBody {
        public final SystemDetails systemDetails;
        public final List<String> labels;
        public final String payload;
        public final int telemetryVersion;
        public final MetaInfo metaInfo;
        public final List<String> mods;

        public RequestBody(SystemDetails systemDetails, List<String> labels, String payload, ModVersion modVersion, int telemetryVersion) {
            this.systemDetails = systemDetails;
            this.labels = labels;
            this.payload = payload;
            this.telemetryVersion = telemetryVersion;
            this.metaInfo = new MetaInfo(modVersion);
            this.mods = List.of();
        }

        public static final class MetaInfo {
            public final ModVersion.SemVer modSemVer;
            public final String mcVersion;
            public final ModVersion.SemVer mcSemVer;
            public final String modVersion;

            public MetaInfo(ModVersion modVersion) {
                this.modVersion = modVersion.getFriendlyString();
                this.modSemVer = modVersion.asSemVer().orElse(null);
                this.mcVersion = MinecraftVersion.create().name();
                this.mcSemVer = ModVersion.fromString(this.mcVersion).asSemVer().orElse(null);
            }
        }
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
        public final List<String> fallbacks;
        public final String cpuName;
        public final boolean compatible;
        public final boolean partiallyIncompatible;

        public SystemDetails() {
            this.os = SystemUtils.OS_NAME;
            this.vendor = GLCompat.glCompat.getString(GL32.GL_VENDOR);
            this.renderer = GLCompat.glCompat.getString(GL32.GL_RENDERER);
            this.glVersion = GLCompat.glCompat.getString(GL32.GL_VERSION);
            this.glVersionMajor = GLCompat.glCompat.getInteger(GL32.GL_MAJOR_VERSION);
            this.glVersionMinor = GLCompat.glCompat.getInteger(GL32.GL_MINOR_VERSION);
            this.glVersionCombined = String.format("%d%d", glVersionMajor, glVersionMinor);
            this.glVersionLwjgl = GLCompat.glCompat.openGlMax;
            this.glslVersion = GLCompat.glCompat.getString(GL32.GL_SHADING_LANGUAGE_VERSION);
            this.extensions = GLCompat.glCompat.supportedCheckedExtensions;
            this.functions = GLCompat.glCompat.supportedCheckedFunctions;
            this.fallbacks = GLCompat.glCompat.usedFallbacks();
            this.compatible = !GLCompat.glCompat.isIncompatible();
            this.partiallyIncompatible = GLCompat.glCompat.isPartiallyIncompatible();

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
