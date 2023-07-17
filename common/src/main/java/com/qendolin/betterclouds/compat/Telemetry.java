package com.qendolin.betterclouds.compat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qendolin.betterclouds.Main;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.MinecraftVersion;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Telemetry implements ITelemetry {
    public static final String ENDPOINT = "https://europe-west3-better-clouds.cloudfunctions.net/collect_telemetry";
    public static final int CONNECT_TIMEOUT_MS = 5000;
    public static final int READ_TIMEOUT_MS = 5000;
    public static final LocalDateTime EXPIRATION_DATE = LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0);
    public static final int VERSION = 2;
    public static final String SHADER_COMPILE_ERROR = "SHADER_COMPILE_ERROR";
    public static final String SYSTEM_INFORMATION = "SYSTEM_INFORMATION";
    public static final String UNHANDLED_EXCEPTION = "UNHANDLED_EXCEPTION";

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

    public CompletableFuture<Boolean> sendSystemInfo() {
        return sendPayload("", SYSTEM_INFORMATION);
    }

    protected CompletableFuture<Boolean> sendPayload(String payload, String... labels) {
        if (!enabled) return CompletableFuture.completedFuture(false);
        try {
            RequestBody body = new RequestBody(new SystemDetails(), List.of(labels), payload, Main.getVersion(), VERSION);
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
        if (error == null || error.strip().equals("")) return;

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

    public void sendUnhandledException(Exception e) {
        if (e == null) return;
        String message = ExceptionUtils.getStackTrace(e);
        if (lazyOpenCache()) {
            String hash = cache.hash(message);
            if (cache.contains(UNHANDLED_EXCEPTION, hash)) return;
        }
        sendPayload(message, UNHANDLED_EXCEPTION)
            .whenComplete((success, throwable) -> {
                if (success) {
                    String hash = cache.hash(message);
                    cache.add(UNHANDLED_EXCEPTION, hash);
                }
            });
    }

    public static final class SemVer {
        public final int major;
        public final int minor;
        public final int patch;
        public final String build;
        public final String prerelease;

        public SemVer(int major, int minor, int patch, String build, String prerelease) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.build = build;
            this.prerelease = prerelease;
        }

        public static Optional<SemVer> fromString(String s) {
            try {
                SemanticVersion v = SemanticVersion.parse(s);
                return fromFabricVersion(v);
            } catch (VersionParsingException e) {
                return Optional.empty();
            }
        }

        public static Optional<SemVer> fromFabricVersion(Version version) {
            if (!(version instanceof SemanticVersion semver)) {
                return Optional.empty();
            }
            int major = 0, minor = 0, patch = 0;
            int count = semver.getVersionComponentCount();
            if (count == 0) return Optional.empty();
            if (count >= 1) major = semver.getVersionComponent(0);
            if (count >= 2) minor = semver.getVersionComponent(1);
            if (count >= 3) patch = semver.getVersionComponent(2);
            return Optional.of(new SemVer(major, minor, patch, semver.getBuildKey().orElse(""), semver.getPrereleaseKey().orElse("")));
        }
    }

    public static final class RequestBody {
        public final SystemDetails systemDetails;
        public final List<String> labels;
        public final String payload;
        public final int telemetryVersion;
        public final MetaInfo metaInfo;

        public RequestBody(SystemDetails systemDetails, List<String> labels, String payload, Version modVersion,
                           int telemetryVersion) {
            this.systemDetails = systemDetails;
            this.labels = labels;
            this.payload = payload;
            this.telemetryVersion = telemetryVersion;
            this.metaInfo = new MetaInfo(modVersion);
        }

        public static final class MetaInfo {
            public final SemVer modSemVer;
            public final String mcVersion;
            public final SemVer mcSemVer;
            public final String modVersion;

            public MetaInfo(Version modVersion) {
                this.modVersion = modVersion.getFriendlyString();
                this.modSemVer = SemVer.fromFabricVersion(modVersion).orElse(null);
                this.mcVersion = MinecraftVersion.CURRENT.getName();
                this.mcSemVer = SemVer.fromString(MinecraftVersion.CURRENT.getName()).orElse(null);
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
            this.fallbacks = Main.glCompat.usedFallbacks;
            this.compatible = !Main.glCompat.isIncompatible();
            this.partiallyIncompatible = Main.glCompat.isPartiallyIncompatible();

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
