package com.qendolin.betterclouds.config;

import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.gui.ConfigScreen;
import com.qendolin.betterclouds.gui.OptionGroupBuilderWrapper;
import com.qendolin.betterclouds.gui.YACLOptionBuilder;
import com.qendolin.betterclouds.mixin.runtime.SimpleOptionAccessor;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.TickBoxController;
import dev.isxander.yacl3.gui.controllers.cycling.EnumController;
import dev.isxander.yacl3.gui.controllers.slider.FloatSliderController;
import dev.isxander.yacl3.gui.controllers.slider.IntegerSliderController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ConfigGUI {
    public static final String LANG_KEY_PREFIX = BetterCloudsStatic.MODID + ".config";

    public final ShaderPresetGUI shaderPresetGUI;
    public final NoisePresetGUI noisePresetGUI;
    public final SereneSeasonsGUI sereneSeasonsCompatGUI;
    public final FabricSeasonsGUI fabricSeasonsCompatGUI;
    public final DimensionsGUI dimensionsGUI;

    public final Option<Integer> chunkSize;
    public final Option<Integer> distance;
    public final Option<Float> fuzziness;
    public final Option<Float> spacing;
    public final Option<Float> sparsity;
    public final Option<Boolean> shuffle;
    public final Option<Float> randomPlacement;
    public final Option<Float> yRange;
    public final Option<Float> yOffset;
    public final Option<Config.TimeSource> timeSource;
    public final Option<Float> pointiness;
    public final Option<Float> bottomSparsity;
    public final Option<Float> samplingScale;
    public final Option<Float> sizeXZ;
    public final Option<Float> sizeY;
    public final Option<Float> scaleFalloffMin;
    public final Option<Float> travelSpeed;
    public final Option<Float> windEffectFactor;
    public final Option<Float> windSpeedFactor;
    public final Option<Float> colorVariationFactor;
    public final Option<Boolean> celestialBodyHalo;

    public final Option<Boolean> enabled;
    public final Option<Float> fogRangeFactor;
    public final Option<Float> fogEndFactor;
    public final Option<Boolean> usePersistentBuffers;
    public final Option<Boolean> useFrustumCulling;

    public final List<Tuple<ConfigCategory.Builder, List<Tuple<OptionGroup.Builder, List<Option<?>>>>>> categories = new ArrayList<>();
    public final Map<ConfigCategory.Builder, ListOption<?>> listOptions = new HashMap<>();

    public final List<Tuple<OptionGroup.Builder, List<Option<?>>>> commonCategory = new ArrayList<>();
    public final List<Tuple<OptionGroup.Builder, List<Option<?>>>> generationCategory = new ArrayList<>();
    public final List<Tuple<OptionGroup.Builder, List<Option<?>>>> appearanceCategory = new ArrayList<>();
    public final List<Tuple<OptionGroup.Builder, List<Option<?>>>> performanceCategory = new ArrayList<>();
    public final List<Tuple<OptionGroup.Builder, List<Option<?>>>> compatCategory = new ArrayList<>();

    public final List<Option<?>> commonGenerationGroup = new ArrayList<>();
    public final List<Option<?>> commonAppearanceGroup = new ArrayList<>();
    public final List<Option<?>> generationVisualGroup = new ArrayList<>();
    public final List<Option<?>> generationPerformanceGroup = new ArrayList<>();
    public final List<Option<?>> appearanceGeometryGroup = new ArrayList<>();
    public final List<Option<?>> appearanceVisibilityGroup = new ArrayList<>();
    public final List<Option<?>> appearanceColorGroup = new ArrayList<>();
    public final List<Option<?>> appearanceSkyGroup = new ArrayList<>();
    public final List<Option<?>> performanceGenerationGroup = new ArrayList<>();
    public final List<Option<?>> performanceTechnicalGroup = new ArrayList<>();
    private final Config config;

    public ConfigGUI(Config defaults, Config config) {
        this.config = config;

        shaderPresetGUI = new ShaderPresetGUI(defaults, config);
        noisePresetGUI = new NoisePresetGUI(defaults, config);
        sereneSeasonsCompatGUI = new SereneSeasonsGUI(defaults.sereneSeasonsConfig, config.sereneSeasonsConfig);
        fabricSeasonsCompatGUI = new FabricSeasonsGUI(defaults.fabricSeasonsConfig, config.fabricSeasonsConfig);
        dimensionsGUI = new DimensionsGUI(defaults, config);

        this.chunkSize = createOption(int.class, "chunkSize")
                .binding(defaults.chunkSize, () -> config.chunkSize, val -> config.chunkSize = val)
                .customController(opt -> new IntegerSliderController(opt, 16, 128, 8))
                .build();
        final Supplier<Options> options = () -> Minecraft.getInstance().options;
        int defaultDistance = ((SimpleOptionAccessor) (Object) options.get().cloudRange()).getInitialValue();
        this.distance = YACLOptionBuilder.create(Option.<Integer>createBuilder())
                .name(Component.translatable("options.renderCloudsDistance"))
                .instant(true)
                .binding(defaultDistance,
                        () -> options.get().cloudRange().get(), val -> options.get().cloudRange().set(val))
                .customController(opt -> new IntegerSliderController(opt, 1, Math.max(defaultDistance, 128), 1))
                .build();
        this.fuzziness = createOption(float.class, "fuzziness")
                .binding(defaults.fuzziness, () -> config.fuzziness, val -> config.fuzziness = val)
                .customController(opt -> new FloatSliderController(opt, 0, 1, 0.01f, ConfigGUI::formatAsPercent))
                .build();
        this.spacing = createOption(float.class, "spacing")
                .binding(defaults.spacing, () -> config.spacing, val -> config.spacing = val)
                .customController(opt -> new FloatSliderController(opt, 2, 64, 0.25f))
                .build();
        this.sparsity = createOption(float.class, "sparsity")
                .binding(defaults.sparsity, () -> config.sparsity, val -> config.sparsity = val)
                .customController(opt -> new FloatSliderController(opt, 0, 1, 0.01f, ConfigGUI::formatAsPercent))
                .build();
        this.shuffle = createOption(boolean.class, "shuffle")
                .binding(defaults.shuffle, () -> config.shuffle, val -> config.shuffle = val)
                .customController(TickBoxController::new)
                .build();
        this.randomPlacement = createOption(float.class, "randomPlacement")
                .binding(defaults.randomPlacement, () -> config.randomPlacement, val -> config.randomPlacement = val)
                .customController(opt -> new FloatSliderController(opt, 0, 1, 0.01f, ConfigGUI::formatAsPercent))
                .build();
        this.yRange = createOption(float.class, "yRange")
                .binding(defaults.yRange, () -> config.yRange, val -> config.yRange = val)
                .customController(opt -> new FloatSliderController(opt, 0, 265, 1))
                .build();
        this.yOffset = createOption(float.class, "yOffset")
                .binding(defaults.yOffset, () -> config.yOffset, val -> config.yOffset = val)
                .customController(opt -> new FloatSliderController(opt, -384, 256, 8))
                .build();
        this.timeSource = createOption(Config.TimeSource.class, "timeSource")
                .binding(defaults.timeSource, () -> config.timeSource, val -> config.timeSource = val)
                .customController(opt -> new EnumController<>(opt, Config.TimeSource.class))
                .build();
        this.pointiness = createOption(float.class, "pointiness")
                .binding(defaults.pointiness, () -> config.pointiness, val -> config.pointiness = val)
                .customController(opt -> new FloatSliderController(opt, 0.5f, 5f, 0.1f))
                .build();
        this.bottomSparsity = createOption(float.class, "bottomSparsity")
                .binding(defaults.bottomSparsity, () -> config.bottomSparsity, val -> config.bottomSparsity = val)
                .customController(opt -> new FloatSliderController(opt, 0.1f, 1f, 0.01f))
                .build();
        this.samplingScale = createOption(float.class, "samplingScale")
                .binding(defaults.samplingScale, () -> config.samplingScale, val -> config.samplingScale = val)
                .customController(opt -> new FloatSliderController(opt, 0.25f, 4, 0.01f, ConfigGUI::formatAsTimes))
                .build();
        this.sizeXZ = createOption(float.class, "sizeXZ")
                .binding(defaults.sizeXZ, () -> config.sizeXZ, val -> config.sizeXZ = val)
                .customController(opt -> new FloatSliderController(opt, 2, 64, 1))
                .build();
        this.sizeY = createOption(float.class, "sizeY")
                .binding(defaults.sizeY, () -> config.sizeY, val -> config.sizeY = val)
                .customController(opt -> new FloatSliderController(opt, 0, 32, 1))
                .build();
        this.scaleFalloffMin = createOption(float.class, "scaleFalloffMin")
                .binding(defaults.scaleFalloffMin, () -> config.scaleFalloffMin, val -> config.scaleFalloffMin = val)
                .customController(opt -> new FloatSliderController(opt, 0, 1, 0.05f, ConfigGUI::formatAsPercent))
                .build();
        this.travelSpeed = createOption(float.class, "travelSpeed")
                .binding(defaults.travelSpeed, () -> config.travelSpeed, val -> config.travelSpeed = val)
                .customController(opt -> new FloatSliderController(opt, 0, 0.4f, 0.005f, ConfigGUI::formatAsBlocksPerSecond))
                .build();
        this.windEffectFactor = createOption(float.class, "windEffectFactor")
                .binding(defaults.windEffectFactor, () -> config.windEffectFactor, val -> config.windEffectFactor = val)
                .customController(opt -> new FloatSliderController(opt, 0, 1, 0.05f, ConfigGUI::formatAsPercent))
                .build();
        this.windSpeedFactor = createOption(float.class, "windSpeedFactor")
                .binding(defaults.windSpeedFactor, () -> config.windSpeedFactor, val -> config.windSpeedFactor = val)
                .customController(opt -> new FloatSliderController(opt, 0, 1, 0.05f, ConfigGUI::formatAsPercent))
                .build();
        this.colorVariationFactor = createOption(float.class, "colorVariationFactor")
                .binding(defaults.colorVariationFactor, () -> config.colorVariationFactor, val -> config.colorVariationFactor = val)
                .customController(opt -> new FloatSliderController(opt, 0, 1, 0.05f, ConfigGUI::formatAsPercent))
                .build();
        this.celestialBodyHalo = createOption(boolean.class, "celestialBodyHalo")
                .binding(defaults.celestialBodyHalo, () -> config.celestialBodyHalo, val -> config.celestialBodyHalo = val)
                .customController(TickBoxController::new)
                .build();
        this.enabled = createOption(boolean.class, "enabled")
                .binding(defaults.enabled, () -> config.enabled, val -> config.enabled = val)
                .customController(opt -> new BooleanController(opt, val -> Component.translatable(LANG_KEY_PREFIX + ".entry.enabled." + val), false))
                .build();
        this.fogRangeFactor = createOption(float.class, "fogRangeFactor")
                .binding(defaults.fogRangeFactor, () -> config.fogRangeFactor, val -> config.fogRangeFactor = val)
                .customController(opt -> new FloatSliderController(opt, 0.1f, 8.0f, 0.1f, ConfigGUI::formatAsTimes))
                .build();
        this.fogEndFactor = createOption(float.class, "fogEndFactor")
                .binding(defaults.fogEndFactor, () -> config.fogEndFactor, val -> config.fogEndFactor = val)
                .customController(opt -> new FloatSliderController(opt, 0.5f, 16.0f, 0.1f, ConfigGUI::formatAsTimes))
                .build();
        this.usePersistentBuffers = createOption(boolean.class, "usePersistentBuffers")
                .binding(defaults.usePersistentBuffers, () -> config.usePersistentBuffers, val -> config.usePersistentBuffers = val)
                .customController(TickBoxController::new)
                .build();
        this.useFrustumCulling = createOption(boolean.class, "useFrustumCulling")
                .binding(defaults.useFrustumCulling, () -> config.useFrustumCulling, val -> config.useFrustumCulling = val)
                .customController(TickBoxController::new)
                .build();


        categories.add(new Tuple<>(ConfigCategory.createBuilder()
                .name(categoryLabel("common")), commonCategory));

        commonCategory.add(new Tuple<>(OptionGroup.createBuilder()
                .name(groupLabel("common.appearance")), commonAppearanceGroup));
        commonAppearanceGroup.addAll(List.of(
                enabled,
                shaderPresetGUI.opacity,
                shaderPresetGUI.opacityFactor
        ));

        commonCategory.add(new Tuple<>(OptionGroup.createBuilder()
                .name(groupLabel("common.generation")), commonGenerationGroup));
        commonGenerationGroup.addAll(List.of(
                sizeXZ,
                sizeY,
                spacing,
                samplingScale,
                distance
        ));

        commonCategory.add(new Tuple<>(OptionGroup.createBuilder()
                .name(groupLabel("common.shaders")), shaderPresetGUI.commonShadersGroup));

        categories.add(new Tuple<>(ConfigCategory.createBuilder()
                .name(categoryLabel("generation")), generationCategory));

        generationCategory.add(new Tuple<>(OptionGroup.createBuilder()
                .name(groupLabel("generation.visual")), generationVisualGroup));
        generationVisualGroup.addAll(List.of(
                randomPlacement,
                fuzziness,
                sparsity,
                yRange,
                yOffset,
                timeSource,
                pointiness,
                bottomSparsity,
                spacing,
                samplingScale,
                shuffle
        ));

        generationCategory.add(new Tuple<>(OptionGroup.createBuilder()
                .name(groupLabel("generation.performance")), generationPerformanceGroup));
        generationPerformanceGroup.addAll(List.of(
                distance,
                chunkSize
        ));

        categories.add(new Tuple<>(ConfigCategory.createBuilder()
                .name(categoryLabel("appearance")), appearanceCategory));

        appearanceCategory.add(new Tuple<>(OptionGroup.createBuilder()
                .name(groupLabel("appearance.geometry")), appearanceGeometryGroup));
        appearanceGeometryGroup.addAll(List.of(
                sizeXZ,
                sizeY,
                scaleFalloffMin,
                travelSpeed,
                windEffectFactor,
                windSpeedFactor
        ));

        appearanceCategory.add(new Tuple<>(OptionGroup.createBuilder()
                .name(groupLabel("appearance.visibility")), appearanceVisibilityGroup));
        appearanceVisibilityGroup.addAll(List.of(
                enabled,
                shaderPresetGUI.opacity,
                shaderPresetGUI.opacityFactor,
                shaderPresetGUI.opacityExponent,
                fogRangeFactor,
                fogEndFactor
        ));

        appearanceCategory.add(new Tuple<>(OptionGroup.createBuilder()
                .name(groupLabel("appearance.color")), appearanceColorGroup));
        appearanceColorGroup.addAll(List.of(
                colorVariationFactor,
                shaderPresetGUI.gamma,
                shaderPresetGUI.dayBrightness,
                shaderPresetGUI.nightBrightness,
                shaderPresetGUI.saturation,
                shaderPresetGUI.tint
        ));

        appearanceCategory.add(new Tuple<>(OptionGroup.createBuilder()
                .name(groupLabel("appearance.sky")), appearanceSkyGroup));
        appearanceSkyGroup.add(celestialBodyHalo);

        categories.add(new Tuple<>(ConfigCategory.createBuilder()
                .name(categoryLabel("performance")), performanceCategory));

        performanceCategory.add(new Tuple<>(OptionGroup.createBuilder()
                .name(groupLabel("performance.generation")), performanceGenerationGroup));
        performanceGenerationGroup.addAll(List.of(
                spacing,
                chunkSize,
                distance,
                sparsity,
                fuzziness,
                shuffle
        ));

        performanceCategory.add(new Tuple<>(OptionGroup.createBuilder()
                .name(groupLabel("performance.technical")), performanceTechnicalGroup));
        performanceTechnicalGroup.addAll(List.of(usePersistentBuffers, useFrustumCulling));

        categories.add(new Tuple<>(ConfigCategory.createBuilder()
                .name(categoryLabel("shaders")), shaderPresetGUI.shadersCategory));

        ConfigCategory.Builder builder = ConfigCategory.createBuilder().name(categoryLabel("noise"));
        categories.add(new Tuple<>(builder, noisePresetGUI.noiseCategory));

        listOptions.put(builder, noisePresetGUI.noiseBuilder);

        categories.add(new Tuple<>(ConfigCategory.createBuilder()
                .name(categoryLabel("compat")), compatCategory));

        compatCategory.add(new Tuple<>(new OptionGroupBuilderWrapper(dimensionsGUI.compatDimensionsListGroup),
                List.of()));

        compatCategory.add(new Tuple<>(OptionGroup.createBuilder()
                .name(groupLabel("compat.sereneSeasons"))
                .description(OptionDescription.of(groupDescription("compat.sereneSeasons")))
                .collapsed(true), sereneSeasonsCompatGUI.compatSereneSeasonsGroup));

        compatCategory.add(new Tuple<>(OptionGroup.createBuilder()
                .name(groupLabel("compat.fabricSeasons"))
                .description(OptionDescription.of(groupDescription("compat.fabricSeasons")))
                .collapsed(true), fabricSeasonsCompatGUI.compatFabricSeasonsGroup));
    }

    public static ConfigScreen create(Screen parent) {
        YetAnotherConfigLib yacl = YetAnotherConfigLib.create(ConfigManager.handler(),
                (defaults, config, builder) -> new ConfigGUI(defaults, config).assemble(builder));
        return new ConfigScreen(yacl, parent);
    }

    static <T> YACLOptionBuilder<T> createOption(Class<T> typeClass, String key) {
        return createOption(typeClass, key, true);
    }

    static <T> YACLOptionBuilder<T> createOption(Class<T> typeClass, String key, boolean hasDescription) {
        YACLOptionBuilder<T> builder = YACLOptionBuilder.create(Option.<T>createBuilder())
                .name(optionLabel(key))
                .instant(false)
                .listener((opt, _) -> opt.applyValue());
        if (hasDescription) builder.description(OptionDescription.of(optionDescription(key)));
        return builder;
    }

    static Component formatAsBlocksPerSecond(Float value) {
        return Component.translatable(LANG_KEY_PREFIX + ".unit.blocks_per_second", String.format("%.1f", value * 20));
    }

    static Component formatAsPercent(float value) {
        return Component.translatable(LANG_KEY_PREFIX + ".unit.percent", ((int) (value * 100)));
    }

    static Component formatAsTimes(float value) {
        return Component.translatable(LANG_KEY_PREFIX + ".unit.times", String.format("%.2f", value));
    }

    static Component formatAsDays(float value) {
        return Component.translatable("gui.days", String.format("%.2f", value));
    }

    static Component formatAsDegrees(Float value) {
        return Component.translatable(LANG_KEY_PREFIX + ".unit.degrees", String.format("%.0f", value));
    }

    static Component formatAsTwoDecimals(Float value) {
        return Component.literal(String.format("%,.2f", value).replaceAll("[\u00a0\u202F]", " "));
    }

    static Component categoryLabel(String key) {
        return Component.translatable(LANG_KEY_PREFIX + ".category." + key);
    }

    static Component groupLabel(String key) {
        return Component.translatable(LANG_KEY_PREFIX + ".group." + key);
    }

    static Component optionLabel(String key) {
        return Component.translatable(LANG_KEY_PREFIX + ".entry." + key);
    }

    static Component optionDescription(String key) {
        return Component.translatable(LANG_KEY_PREFIX + ".entry." + key + ".description");
    }

    static Component groupDescription(String key) {
        return Component.translatable(LANG_KEY_PREFIX + ".group." + key + ".description");
    }

    public YetAnotherConfigLib.Builder assemble(YetAnotherConfigLib.Builder builder) {
        builder.save(() -> {
                    shaderPresetGUI.onSave();
                    noisePresetGUI.onSave();
                    config.selectedPreset = Mth.clamp(config.selectedPreset, 0, config.presets.size());
                    config.sortShaderPresets();
                    config.selectedNoisePreset = Mth.clamp(config.selectedNoisePreset, 0, config.noisePresets.size());
                    config.sortNoisePresets();
                    ConfigManager.handler().save();
                })
                .title(Component.translatable(LANG_KEY_PREFIX + ".title"));

        for (Tuple<ConfigCategory.Builder, List<Tuple<OptionGroup.Builder, List<Option<?>>>>> categoryPair : categories) {
            ConfigCategory.Builder categoryBuilder = categoryPair.getA();
            for (Tuple<OptionGroup.Builder, List<Option<?>>> groupPair : categoryPair.getB()) {
                OptionGroup.Builder groupBuilder = groupPair.getA();
                if (!groupPair.getB().isEmpty())
                    groupBuilder.options(groupPair.getB());
                categoryBuilder.group(groupBuilder.build());
            }
            if (listOptions.containsKey(categoryBuilder)) {
                categoryBuilder.group(listOptions.get(categoryBuilder));
            }
            builder.category(categoryBuilder.build());
        }

        return builder;
    }
}
