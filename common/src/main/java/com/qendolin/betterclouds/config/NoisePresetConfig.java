package com.qendolin.betterclouds.config;

import com.google.gson.InstanceCreator;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.clouds.Sampler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NoisePresetConfig extends AbstractPresetConfig {
    public static final InstanceCreator<NoisePresetConfig> INSTANCE_CREATOR = _ -> new NoisePresetConfig();
    public static final String OCTAVE_DELIMITER = ", ";
    public static final Pattern COMMA_REGEX = Pattern.compile("\\s*,\\s*");
    protected static final NoisePresetConfig EMPTY_PRESET = new NoisePresetConfig();

    @SerialEntry
    public List<List<Integer>> octaves = List.of(Sampler.OCTAVE_OPTIONS[0]);

    public NoisePresetConfig() {
        this("Default");
    }

    public NoisePresetConfig(String title) {
        this.title = title;
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public NoisePresetConfig(NoisePresetConfig other) {
        Configs.copy(this, other);
    }

    public List<String> octavesToStringList() {
        return octaves.stream()
                .map(octave -> octave.stream().map(Object::toString).collect(Collectors.joining(OCTAVE_DELIMITER)))
                .toList();
    }

    public void octavesFromStringList(List<String> octaves) {
        try {
            this.octaves = octaves.stream()
                    .map(octave -> Arrays.stream(COMMA_REGEX.split(octave))
                            .map(Integer::parseInt).toList()).toList();
        } catch (Exception e) {
            BetterCloudsStatic.getLogger().warn("Invalid config, reverting to last config", e);
        }
    }

    @Override
    public AbstractPresetConfig getEmptyPreset() {
        return EMPTY_PRESET;
    }
}
