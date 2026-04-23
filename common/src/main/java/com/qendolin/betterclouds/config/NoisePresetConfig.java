package com.qendolin.betterclouds.config;

import com.google.gson.InstanceCreator;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.clouds.Sampler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NoisePresetConfig extends AbstractPresetConfig {
    public static final InstanceCreator<NoisePresetConfig> INSTANCE_CREATOR = _ -> new NoisePresetConfig();
    protected static final NoisePresetConfig EMPTY_PRESET = new NoisePresetConfig();

    @SerialEntry
    public List<String> octaves = new ArrayList<>();
    private List<String> prevOctaves = Sampler.DEFAULT_OCTAVES_STR;

    private List<List<Integer>> parsedOctaves;

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

    public List<List<Integer>> getOctaves() {
        if (!prevOctaves.equals(octaves)) parseOctaves();
        return parsedOctaves;
    }

    private void parseOctaves() {
        try {
            parsedOctaves = octaves.stream()
                    .map(s -> Arrays.stream(s.split(",")).map(Integer::parseInt).toList())
                    .toList();
            prevOctaves = octaves;
        } catch (Exception e) {
            assert !prevOctaves.equals(octaves);    // if assert fails, reverting to previous octaves failed
            BetterCloudsStatic.getLogger().warn("Noise configuration is invalid, reverting to last valid configuration:", e);
            octaves = prevOctaves;
            parseOctaves();
        }
    }

    @Override
    public AbstractPresetConfig getEmptyPreset() {
        return EMPTY_PRESET;
    }
}
