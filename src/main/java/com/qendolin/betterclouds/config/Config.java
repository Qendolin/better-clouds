package com.qendolin.betterclouds.config;

import com.google.gson.*;
import com.qendolin.betterclouds.BetterCloudsStatic;
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

    public void loadDefaultPresets() {
        for (PresetLoader<?> presetLoader : PresetLoader.ALL_PRESETS) {
            if (presetLoader.presets().isEmpty()) {
                // Not initialized yet, wait for other preset file to be loaded
                BetterCloudsStatic.getLogger().info("{} not loaded yet, waiting for next resource load", presetLoader.id);
                return;
            }
        }

        BetterCloudsStatic.getLogger().info("All preset resources loaded, initializing preset config");
        loadDefaultPreset(PresetLoader.SHADER, presets);
        loadDefaultPreset(PresetLoader.NOISE, noisePresets);
        sortShaderPresets(false);
        sortNoisePresets(false);
    }

    public <T extends AbstractPresetConfig> void loadDefaultPreset(
            PresetLoader<T> loader,
            List<T> presets
    ) {
        assert !loader.presets().isEmpty();

        // Remember which default preset was selected, if any
        Map<String, T> defaults = loader.presets();     // map is copied dw
        presets.removeIf(preset -> preset.key != null && !preset.editable && defaults.containsKey(preset.key));
        presets.addAll(defaults.values());
    }

    @NotNull
    public ShaderPresetConfig shaderPreset() {
        if (presets.isEmpty())
            return PresetLoader.SHADER.presets().getOrDefault(DEFAULT_PRESET_KEY, ShaderPresetConfig.EMPTY_PRESET);
        selectedPreset = MathHelper.clamp(selectedPreset, 0, presets.size() - 1);
        return presets.get(selectedPreset);
    }

    public void sortShaderPresets() {
        sortShaderPresets(true);
    }

    public void sortShaderPresets(boolean updateSelectedIndex) {
        ShaderPresetConfig selected = shaderPreset();
        Comparator<ShaderPresetConfig> comparator = Comparator.
            <ShaderPresetConfig, Boolean>comparing(preset -> !preset.editable)
            .thenComparing(preset -> !DEFAULT_PRESET_KEY.equals(preset.key))
            .thenComparing(preset -> preset.title);
        presets.sort(comparator);
        if (updateSelectedIndex) {
            selectedPreset = presets.indexOf(selected);
        }
    }

    @NotNull
    public NoisePresetConfig noisePreset() {
        if (noisePresets.isEmpty())
            return PresetLoader.NOISE.presets().getOrDefault(DEFAULT_PRESET_KEY, NoisePresetConfig.EMPTY_PRESET);
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
