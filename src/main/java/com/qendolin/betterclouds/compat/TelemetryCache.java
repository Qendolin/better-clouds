package com.qendolin.betterclouds.compat;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.qendolin.betterclouds.Main;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelemetryCache {

    protected final Pattern linePattern = Pattern.compile("^(\\w+)\\s+([0-9a-f]+)\\s*$");

    protected FileWriter writer;
    protected boolean available;
    protected final Map<String, Set<String>> cache = new HashMap<>();
    protected final HashFunction hashFunction = Hashing.sipHash24();
    private boolean opened;

    public TelemetryCache() {
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isOpened() {
        return opened;
    }

    public void open() throws IOException {
        if (opened) return;
        opened = true;
        cache.clear();

        File dir = Paths.get(".cache").toFile();
        //noinspection ResultOfMethodCallIgnored
        dir.mkdir();
        File file = Paths.get(".cache", Main.MODID + "-telemetry_cache-v" + Telemetry.VERSION + ".bin").toFile();
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();
        int lineCount = 0;
        final int maxLines = 10000;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // the cache should never grow this large
                if (lineCount++ > maxLines) break;

                Matcher matcher = linePattern.matcher(line);
                if (!matcher.matches()) continue;
                String type = matcher.group(1).toLowerCase();
                String hash = matcher.group(2).toLowerCase();
                if (!cache.containsKey(type)) {
                    cache.put(type, new HashSet<>());
                }
                cache.get(type).add(hash);
            }
        }

        if (lineCount > maxLines) {
            Files.write(file.toPath(), new byte[]{}, StandardOpenOption.TRUNCATE_EXISTING);
            cache.clear();
        }

        writer = new FileWriter(file, StandardCharsets.UTF_8, true);
        available = true;
    }

    public boolean contains(String type, String hash) {
        if (!available) throw new UnsupportedOperationException("Cache it not available!");
        type = type.toLowerCase();
        hash = hash.toLowerCase();
        if (!cache.containsKey(type)) return false;
        return cache.get(type).contains(hash);
    }

    public void add(String type, String hash) {
        if (!available) throw new UnsupportedOperationException("Cache it not available!");
        type = type.toLowerCase();
        hash = hash.toLowerCase();
        if (!cache.containsKey(type)) {
            cache.put(type, new HashSet<>());
        }
        cache.get(type).add(hash);
        try {
            writer.append(String.format("%s%s %s", System.lineSeparator(), type, hash));
            writer.flush();
        } catch (IOException ignored) {
        }
    }

    public String hash(String str) {
        return hashFunction.hashString(str, StandardCharsets.UTF_8).toString();
    }
}
