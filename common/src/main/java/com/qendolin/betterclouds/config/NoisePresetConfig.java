package com.qendolin.betterclouds.config;

import com.google.gson.InstanceCreator;
import com.qendolin.betterclouds.clouds.Sampler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NoisePresetConfig extends AbstractPresetConfig {
    public static final InstanceCreator<NoisePresetConfig> INSTANCE_CREATOR = _ -> new NoisePresetConfig();
    protected static final NoisePresetConfig EMPTY_PRESET = new NoisePresetConfig();

    @SerialEntry
    public List<List<Integer>> octaves = List.of(Sampler.OCTAVE_OPTIONS[0]);

    public NoisePresetConfig() {
        this("");
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
                .map(octave -> octave.stream().map(Object::toString).collect(Collectors.joining(", ")))
                .toList();
    }

    public void octavesFromStringList(List<String> octaves) {
        this.octaves = octaves.stream()
                .map(octave -> Arrays.stream(octave.split("\\s*,\\s*"))
                        .map(Integer::parseInt).toList()).toList();
    }

    @Override
    public AbstractPresetConfig getEmptyPreset() {
        return EMPTY_PRESET;
    }
}
