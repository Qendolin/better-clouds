package com.qendolin.betterclouds.config;

import com.google.gson.*;
import com.qendolin.betterclouds.compat.BigGlobeCompat;
import com.qendolin.betterclouds.compat.MiddleEarthCompat;
import com.qendolin.betterclouds.util.PreLaunchGuard;
import dev.isxander.yacl3.api.NameableEnum;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.*;

public class Config {

    public static final String DEFAULT_PRESET_KEY = "default";
    public static final InstanceCreator<Config> INSTANCE_CREATOR = type -> new Config();
    public static final RegistryKeySerializer REGISTRY_KEY_SERIALIZER = new RegistryKeySerializer();

    static {
        PreLaunchGuard.check();
    }

    @SuppressWarnings("unused")
    public Config() {
    }

    @SerialEntry
    public float bottomSparsity = 0f;

    @SerialEntry
    public int migrationVersion = 0;
    @SerialEntry
    public boolean enabled = true;
    //? if <1.21.6 {
    /*@SerialEntry
    public float distance = 4;
    *///?}
    @SerialEntry
    public float randomPlacement = 1.0f;
    @SerialEntry
    public float fuzziness = 1.0f;
    @SerialEntry
    public float yRange = 64f;
    @SerialEntry
    public float yOffset = 0f;
    @SerialEntry
    public TimeSource timeSource = TimeSource.WORLD;
    @SerialEntry
    public float sparsity = 0f;
    @SerialEntry
    public float spacing = 5.25f;
    @SerialEntry
    public float sizeXZ = 16f;
    @SerialEntry
    public float sizeY = 8f;
    @SerialEntry
    public float travelSpeed = 0.03f;
    @SerialEntry
    public float windEffectFactor = 1.0f;
    @SerialEntry
    public float windSpeedFactor = 0.8f;
    @SerialEntry
    public float colorVariationFactor = 0.8f;
    @SerialEntry
    public boolean celestialBodyHalo = true;
    @SerialEntry
    public int chunkSize = 32;
    @SerialEntry
    public float samplingScale = 1;
    @SerialEntry
    public float scaleFalloffMin = 0.25f;
    @SerialEntry
    public float fogRangeFactor = 1f;
    @SerialEntry
    public float fogEndFactor = 1f;
    @SerialEntry
    public boolean usePersistentBuffers = true;
    @SerialEntry
    public boolean useSamplerCaching = true;
    @SerialEntry
    public boolean useFrustumCulling = true;
    @SerialEntry
    public boolean irisSupport = true;
    @SerialEntry
    public int selectedNoisePreset = 0;
    @SerialEntry
    public boolean cloudOverride = true;
    @SerialEntry
    public boolean useIrisFBO = true;
    @SerialEntry
    public int selectedPreset = 0;
    @SerialEntry
    public List<ShaderPresetConfig> presets = new ArrayList<>();
    @SerialEntry
    public List<NoisePresetConfig> noisePresets = new ArrayList<>();
    @SerialEntry
    public int lastTelemetryVersion = 0;

    @SuppressWarnings("CopyConstructorMissesField")
    public Config(Config other) {
        Configs.copy(this, other);
    }
    @SerialEntry
    public boolean gpuIncompatibleMessageEnabled = true;
    @SerialEntry
    public boolean issueReportEnabled = true;
    //? if >=1.21.6 {
    @SerialEntry
    public boolean lunarSucksMessageEnabled = true;
    //?}
    @SerialEntry
    public List<RegistryKey<DimensionType>> enabledDimensions = new ArrayList<>(getDefaultDimensions());
    @SerialEntry
    public SereneSeasonsConfig sereneSeasonsConfig = new SereneSeasonsConfig();
    @SerialEntry
    public FabricSeasonsConfig fabricSeasonsConfig = new FabricSeasonsConfig();

    private static boolean isPresetEqualToEmpty(AbstractPresetConfig preset) {
        if (preset == null) return true;
        String title = preset.title;
        // The title does not matter
        preset.title = preset.getEmptyPreset().title;
        boolean equal = preset.isEqualTo(preset.getEmptyPreset());
        preset.title = title;
        return equal;
    }

    public void loadDefaultPresets() {
        if (PresetLoader.ALL_PRESETS.stream().anyMatch(loader -> loader.presets().isEmpty())) {
            return;
        }
        loadDefaultShaderPresets();
        loadDefaultNoisePresets();
        sortPresets(false);
        sortNoisePresets(false);
    }

    private void loadDefaultShaderPresets() {
        // Remember which default preset was selected, if any
        String selectedDefaultPreset = preset().key;
        Map<String, ShaderPresetConfig> defaults = new HashMap<>(PresetLoader.SHADER.presets());
        boolean missingDefault = presets.stream().noneMatch(preset -> DEFAULT_PRESET_KEY.equals(preset.key));
        presets.removeIf(preset -> preset.key != null && !preset.editable && defaults.containsKey(preset.key));
        presets.addAll(defaults.values());

        if (selectedDefaultPreset != null) {
            // Restore the selected default preset
            presets.stream()
                .filter(preset -> selectedDefaultPreset.equals(preset.key)).findFirst()
                .ifPresentOrElse(prevSelectedPreset -> selectedPreset = presets.indexOf(prevSelectedPreset), () -> selectedPreset = 0);
        }

        if (missingDefault) {
            // No preset with the key 'default' was present,
            // so it is assumed that the presets are not initialized
            presets.removeIf(Config::isPresetEqualToEmpty);
            ShaderPresetConfig defaultPreset = defaults.get(DEFAULT_PRESET_KEY);
            if (defaultPreset != null) {
                ShaderPresetConfig defaultCopy = new ShaderPresetConfig(defaultPreset);
                defaultCopy.markAsCopy();
                presets.add(defaultCopy);
                selectedPreset = presets.indexOf(defaultCopy);
            }
        }
    }

    private void loadDefaultNoisePresets() {
        String selectedDefaultPreset = noisePreset().key;
        Map<String, NoisePresetConfig> defaults = new HashMap<>(PresetLoader.NOISE.presets());
        boolean missingDefault = noisePresets.stream().noneMatch(preset -> DEFAULT_PRESET_KEY.equals(preset.key));
        noisePresets.removeIf(preset -> preset.key != null && !preset.editable && defaults.containsKey(preset.key));
        noisePresets.addAll(defaults.values());

        if (selectedDefaultPreset != null) {
            noisePresets.stream()
                    .filter(preset -> selectedDefaultPreset.equals(preset.key)).findFirst()
                    .ifPresentOrElse(prevSelectedPreset -> selectedNoisePreset = noisePresets.indexOf(prevSelectedPreset), () -> selectedNoisePreset = 0);
        }

        if (missingDefault) {
            noisePresets.removeIf(Config::isPresetEqualToEmpty);
            NoisePresetConfig defaultPreset = defaults.get(DEFAULT_PRESET_KEY);
            if (defaultPreset != null) {
                NoisePresetConfig defaultCopy = new NoisePresetConfig(defaultPreset);
                defaultCopy.markAsCopy();
                noisePresets.add(defaultCopy);
                selectedNoisePreset = noisePresets.indexOf(defaultCopy);
            }
        }
    }

    @NotNull
    public ShaderPresetConfig preset() {
        return shaderPreset();
    }

    @NotNull
    public ShaderPresetConfig shaderPreset() {
        if (presets == null || presets.isEmpty()) {
            ShaderPresetConfig preset = PresetLoader.SHADER.presets().get(DEFAULT_PRESET_KEY);
            return preset != null ? preset : ShaderPresetConfig.EMPTY_PRESET;
        }
        selectedPreset = MathHelper.clamp(selectedPreset, 0, presets.size() - 1);
        return presets.get(selectedPreset);
    }

    public void sortPresets() {
        sortPresets(true);
    }

    public void sortPresets(boolean updateSelectedIndex) {
        ShaderPresetConfig selected = preset();
        Comparator<ShaderPresetConfig> comparator = Comparator.
            <ShaderPresetConfig, Boolean>comparing(preset -> !preset.editable)
            .thenComparing(preset -> !DEFAULT_PRESET_KEY.equals(preset.key))
            .thenComparing(preset -> preset.title);
        presets.sort(comparator);
        if (updateSelectedIndex) {
            selectedPreset = presets.indexOf(selected);
        }
    }

    public void addFirstPreset() {
        if (presets == null) presets = new ArrayList<>();
        if (!presets.isEmpty()) return;
        presets.add(new ShaderPresetConfig());
    }

    @NotNull
    public NoisePresetConfig noisePreset() {
        if (noisePresets == null || noisePresets.isEmpty()) {
            NoisePresetConfig preset = PresetLoader.NOISE.presets().get(DEFAULT_PRESET_KEY);
            return preset != null ? preset : NoisePresetConfig.EMPTY_PRESET;
        }
        selectedNoisePreset = MathHelper.clamp(selectedNoisePreset, 0, noisePresets.size() - 1);
        return noisePresets.get(selectedNoisePreset);
    }

    public void sortNoisePresets() {
        sortNoisePresets(true);
    }

    public void sortNoisePresets(boolean updateSelectedIndex) {
        NoisePresetConfig selected = noisePreset();
        Comparator<NoisePresetConfig> comparator = Comparator.
                <NoisePresetConfig, Boolean>comparing(preset -> !preset.editable)
                .thenComparing(preset -> !DEFAULT_PRESET_KEY.equals(preset.key))
                .thenComparing(preset -> preset.title);
        noisePresets.sort(comparator);
        if (updateSelectedIndex) {
            selectedNoisePreset = noisePresets.indexOf(selected);
        }
    }

    public void addFirstNoisePreset() {
        if (noisePresets == null) noisePresets = new ArrayList<>();
        if (!noisePresets.isEmpty()) return;
        noisePresets.add(new NoisePresetConfig());
    }

    public int blockDistance() {
        //? if >=1.21.6 {
        return MinecraftClient.getInstance().options.getCloudRenderDistance().getValue() * 16;
        //?} else {
        /*return (int) (this.distance * MinecraftClient.getInstance().options.getViewDistance().getValue() * 16);
        *///?}
    }

    public static List<RegistryKey<DimensionType>> getDefaultDimensions() {
        return List.of(
            DimensionTypes.OVERWORLD,
            BigGlobeCompat.DIMENSION_KEY,
            MiddleEarthCompat.DIMENSION_KEY);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Config)) {
            return false;
        }
        return Configs.equal(this, obj);
    }

    @Override
    public int hashCode() {
        return Configs.hashCode(this);
    }

    public enum TimeSource implements NameableEnum {
        WORLD, PLAYTIME, RENDERER;

        @Override
        public Text getDisplayName() {
            return Text.translatable("betterclouds.config.entry.timeSource.option." + name().toLowerCase());
        }
    }

    public static class RegistryKeySerializer implements JsonSerializer<RegistryKey<DimensionType>>, JsonDeserializer<RegistryKey<DimensionType>> {
        private RegistryKeySerializer() {
        }

        @Override
        public RegistryKey<DimensionType> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isString())
                throw new JsonParseException("RegistryKey must be a string");
            try {
                //? if >=1.21 {
                Identifier id = Identifier.of(json.getAsString());
                //?} else {
                /*@SuppressWarnings("removal")
                Identifier id = new Identifier(json.getAsString());
                *///?}
                return RegistryKey.of(RegistryKeys.DIMENSION_TYPE, id);
            } catch (InvalidIdentifierException e) {
                throw new JsonParseException("Invalid RegistryKey: " + e.getMessage());
            }
        }

        @Override
        public JsonElement serialize(RegistryKey<DimensionType> src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getValue().toString());
        }
    }

}
