package com.qendolin.betterclouds.renderdoc;

import com.sun.jna.Library;
import com.sun.jna.Native;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.lwjgl.system.linux.DynamicLinkLoader;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipFile;

public class RenderDocLoader {
    public static final Path LIB_LINUX_PATH = Path.of("./better-clouds/librenderdoc.so");
    public static final Path LIB_WINDOWS_PATH = Path.of("./better-clouds/renderdoc.dll");

    private enum OperatingSystem {
        UNKNOWN,
        LINUX,
        SOLARIS,
        WINDOWS,
        OSX
    }

    private static String getOperatingSystemName() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT);
    }

    private static OperatingSystem getOperatingSystem() {
        String name = getOperatingSystemName();
        if (name.contains("win")) {
            return OperatingSystem.WINDOWS;
        } else if (name.contains("mac")) {
            return OperatingSystem.OSX;
        } else if (name.contains("solaris")) {
            return OperatingSystem.SOLARIS;
        } else if (name.contains("sunos")) {
            return OperatingSystem.SOLARIS;
        } else if (name.contains("linux")) {
            return OperatingSystem.LINUX;
        } else {
            return name.contains("unix") ? OperatingSystem.LINUX : OperatingSystem.UNKNOWN;
        }
    }

    public static void install() {
        OperatingSystem os = getOperatingSystem();

        try {
            if (os == OperatingSystem.WINDOWS) {
                downloadWindows();
            } else if (os == OperatingSystem.LINUX) {
                downloadLinux();
            } else {
                throw new RuntimeException("Unsupported OS: " + getOperatingSystemName());
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void uninstall() {
        if (!Files.exists(libPath())) {
            return;
        }
        if (RenderDoc.isAvailable()) {
            throw new RuntimeException("Cannot uninstall RenderDoc as it is currently in use");
        }
        try {
            Files.delete(libPath());
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }


    public static boolean isAvailable() {
        OperatingSystem os = getOperatingSystem();

        if (os == OperatingSystem.WINDOWS) {
            return isAvailable("renderdoc.dll", "913a2f5b87981169f40207ab81be3e88");
        } else if (os == OperatingSystem.LINUX) {
            return isAvailable("librenderdoc.so", "3d134559f0128b2e079eab0bd5395588");
        }

        return false;
    }

    private static boolean isAvailable(String name, String md5sum) {
        File dllFile = Path.of("./better-clouds/", name).toFile();
        if (!dllFile.exists() || dllFile.isDirectory()) return false;

        try (FileInputStream is = new FileInputStream(dllFile)) {
            String sum = DigestUtils.md5Hex(is);
            if (sum.equalsIgnoreCase(md5sum)) {
                return true;
            } else {
                RenderDoc.LOGGER.warn("renderdoc library present but md5 checksum wrong: {}, expected {}", sum, md5sum);
            }
        } catch (IOException ignored) {
        }
        return false;
    }

    private static void downloadWindows() throws IOException {
        File archive = Path.of("./better-clouds/RenderDoc_1.30_64.zip").toFile();

        FileUtils.copyURLToFile(new URL("https://renderdoc.org/stable/1.30/RenderDoc_1.30_64.zip"), archive);

        try (ZipFile zipFile = new ZipFile(archive)) {
            InputStream dllEntry = zipFile.getInputStream(zipFile.getEntry("RenderDoc_1.30_64/renderdoc.dll"));

            File dllFile = LIB_WINDOWS_PATH.toFile();
            FileUtils.copyInputStreamToFile(dllEntry, dllFile);
        } finally {
            archive.delete();
        }
    }

    private static void downloadLinux() throws IOException {
        File archive = Path.of("./better-clouds/renderdoc_1.30.tar.gz").toFile();

        FileUtils.copyURLToFile(new URL("https://renderdoc.org/stable/1.30/renderdoc_1.30.tar.gz"), archive);

        try (FileInputStream source = new FileInputStream(archive);
             GZIPInputStream gzip = new GZIPInputStream(source);
             TarArchiveInputStream tar = new TarArchiveInputStream(gzip)) {

            File dllFile = LIB_LINUX_PATH.toFile();
            TarArchiveEntry entry;
            boolean found = false;
            while ((entry = tar.getNextTarEntry()) != null) {
                String name = entry.getName();
                if (name.equalsIgnoreCase("renderdoc_1.30/lib/librenderdoc.so")) {
                    FileUtils.copyInputStreamToFile(tar, dllFile);
                    found = true;
                    break;
                }
            }
            if (!found) throw new FileNotFoundException("renderdoc_1.30/lib/librenderdoc.so");
        } finally {
            archive.delete();
        }
    }

    public static void load() {
        if (RenderDoc.isAvailable()) return;

        OperatingSystem os = getOperatingSystem();

        if (os == OperatingSystem.WINDOWS || os == OperatingSystem.LINUX) {
            try {
                RenderDocLibrary renderdocLibrary;
                String libPath = libPath().toAbsolutePath().toString();
                if (os == OperatingSystem.WINDOWS) {
                    renderdocLibrary = Native.load(libPath, RenderDocLibrary.class);
                } else {
                    int flags = DynamicLinkLoader.RTLD_NOW | DynamicLinkLoader.RTLD_NOLOAD;
                    if (DynamicLinkLoader.dlopen(libPath, flags) == 0) {
                        throw new UnsatisfiedLinkError();
                    }

                    renderdocLibrary = Native.load(libPath, RenderDocLibrary.class, Map.of(Library.OPTION_OPEN_FLAGS, flags));
                }
                RenderDoc.init(renderdocLibrary);
            } catch (UnsatisfiedLinkError ignored) {
            }
        }
    }

    public static Path libPath() {
        OperatingSystem os = getOperatingSystem();
        if (os == OperatingSystem.WINDOWS) {
            return LIB_WINDOWS_PATH;
        } else if (os == OperatingSystem.LINUX) {
            return LIB_LINUX_PATH;
        } else {
            throw new RuntimeException("Unsupported OS: " + getOperatingSystemName());
        }
    }
}
