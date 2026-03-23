package com.qendolin.betterclouds.platform;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ModVersion {
    public static final ModVersion NONE = new ModVersion() {
        @Override
        public String getFriendlyString() {
            return "unknown";
        }

        @Override
        public Optional<SemVer> asSemVer() {
            return Optional.empty();
        }

        @Override
        public boolean isPresent() {
            return false;
        }
    };
    private static final Pattern SEMVER_PATTERN = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

    public static ModVersion fromString(String version) {
        return new StringVersion(version);
    }

    public abstract boolean isPresent();

    /**
     * Returns the user-friendly representation of this version.
     */
    public abstract String getFriendlyString();

    public Optional<SemVer> asSemVer() {
        Matcher matcher = SEMVER_PATTERN.matcher(getFriendlyString());
        if (!matcher.find()) return Optional.empty();
        try {
            int major = Integer.parseInt(matcher.group(1));
            int minor = Integer.parseInt(matcher.group(2));
            int patch = Integer.parseInt(matcher.group(3));
            String prerelease = matcher.group(4);
            String buildmetadata = matcher.group(5);

            return Optional.of(new SemVer(major, minor, patch, buildmetadata, prerelease));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static final class StringVersion extends ModVersion {
        final String version;

        private StringVersion(String version) {
            this.version = version;
        }

        @Override
        public boolean isPresent() {
            return version != null;
        }

        @Override
        public String getFriendlyString() {
            return version;
        }
    }

    public static final class SemVer implements Comparable<SemVer> {
        public final int major;
        public final int minor;
        public final int patch;
        public final String build;
        public final String prerelease;

        public SemVer(int major, int minor, int patch) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.build = "";
            this.prerelease = "";
        }

        public SemVer(int major, int minor, int patch, String build, String prerelease) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.build = build;
            this.prerelease = prerelease;
        }


        @Override
        public int compareTo(@NotNull ModVersion.SemVer o) {
            if (major < o.major) return -1;
            if (major > o.major) return 1;
            if (minor < o.minor) return -1;
            if (minor > o.minor) return 1;
            if (patch < o.patch) return -1;
            if (patch > o.patch) return 1;
            return 0;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(major).append('.').append(minor).append('.').append(patch);
            if (!prerelease.isEmpty()) {
                sb.append('-').append(prerelease);
            }
            if (!build.isEmpty()) {
                sb.append('+').append(build);
            }
            return sb.toString();
        }
    }
}
