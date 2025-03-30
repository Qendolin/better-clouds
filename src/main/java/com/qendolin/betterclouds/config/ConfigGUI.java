package com.qendolin.betterclouds.config;

import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.gui.ConfigScreen;
import com.qendolin.betterclouds.gui.OptionGroupBuilderWrapper;
import com.qendolin.betterclouds.gui.YACLOptionBuilder;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.TickBoxController;
import dev.isxander.yacl3.gui.controllers.slider.FloatSliderController;
import dev.isxander.yacl3.gui.controllers.slider.IntegerSliderController;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class ConfigGUI {
    private final Config config;

    public final ShaderPresetGUI shaderPresetGUI;
    public final SereneSeasonsGUI sereneSeasonsCompatGUI;
    public final FabricSeasonsGUI fabricSeasonsCompatGUI;
    public final DimensionsGUI dimensionsGUI;

    public final Option<Integer> chunkSize;
    public final Option<Float> distance;
    public final Option<Float> fuzziness;
    public final Option<Float> spacing;
    public final Option<Float> sparsity;
    public final Option<Boolean> shuffle;
    public final Option<Float> randomPlacement;
    public final Option<Float> yRange;
    public final Option<Float> yOffset;
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
    public final Option<Boolean> usePersistentBuffers;
    public final Option<Boolean> useFrustumCulling;

    public final List<Pair<ConfigCategory.Builder, List<Pair<OptionGroup.Builder, List<Option<?>>>>>> categories = new ArrayList<>();

    public final List<Pair<OptionGroup.Builder, List<Option<?>>>> commonCategory = new ArrayList<>();
    public final List<Pair<OptionGroup.Builder, List<Option<?>>>> generationCategory = new ArrayList<>();
    public final List<Pair<OptionGroup.Builder, List<Option<?>>>> appearanceCategory = new ArrayList<>();
    public final List<Pair<OptionGroup.Builder, List<Option<?>>>> performanceCategory = new ArrayList<>();
    public final List<Pair<OptionGroup.Builder, List<Option<?>>>> compatCategory = new ArrayList<>();

    public final List<Option<?>> commonPresetsGroup = new ArrayList<>();
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

    public ConfigGUI(Config defaults, Config config) {
        this.config = config;

        shaderPresetGUI = new ShaderPresetGUI(defaults, config);
        sereneSeasonsCompatGUI = new SereneSeasonsGUI(defaults.sereneSeasonsConfig, config.sereneSeasonsConfig);
        fabricSeasonsCompatGUI = new FabricSeasonsGUI(defaults.fabricSeasonsConfig, config.fabricSeasonsConfig);
        dimensionsGUI = new DimensionsGUI(defaults, config);

        this.chunkSize = createOption(int.class, "chunkSize")
            .binding(defaults.chunkSize, () -> config.chunkSize, val -> config.chunkSize = val)
            .customController(opt -> new IntegerSliderController(opt, 16, 128, 8))
            .build();
        this.distance = createOption(float.class, "distance")
            .binding(defaults.distance, () -> config.distance, val -> config.distance = val)
            .customController(opt -> new FloatSliderController(opt, 1, 4, 0.05f, ConfigGUI::formatAsTimes))
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
            .customController(opt -> new BooleanController(opt, val -> Text.translatable(LANG_KEY_PREFIX + ".entry.enabled." + val), false))
            .build();
        this.fogRangeFactor = createOption(float.class, "fogRangeFactor")
            .binding(defaults.fogRangeFactor, () -> config.fogRangeFactor, val -> config.fogRangeFactor = val)
            .customController(opt -> new FloatSliderController(opt, 0.1f, 8.0f, 0.1f, ConfigGUI::formatAsTimes))
            .build();
        this.usePersistentBuffers = createOption(boolean.class, "usePersistentBuffers")
            .binding(defaults.usePersistentBuffers, () -> config.usePersistentBuffers, val -> config.usePersistentBuffers = val)
            .customController(TickBoxController::new)
            .build();
        this.useFrustumCulling = createOption(boolean.class, "useFrustumCulling")
            .binding(defaults.useFrustumCulling, () -> config.useFrustumCulling, val -> config.useFrustumCulling = val)
            .customController(TickBoxController::new)
            .build();


        categories.add(new Pair<>(ConfigCategory.createBuilder()
            .name(categoryLabel("common")), commonCategory));

        commonCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("common.appearance")), commonAppearanceGroup));
        commonAppearanceGroup.addAll(List.of(
            enabled,
            shaderPresetGUI.opacity,
            shaderPresetGUI.opacityFactor
        ));

        commonCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("common.presets")), commonPresetsGroup));
        commonPresetsGroup.addAll(List.of(
            shaderPresetGUI.selectedPreset,
            shaderPresetGUI.presetTitle,
            shaderPresetGUI.copyPresetButton,
            shaderPresetGUI.removePresetButton
        ));

        commonCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("common.generation")), commonGenerationGroup));
        commonGenerationGroup.addAll(List.of(
            sizeXZ,
            sizeY,
            spacing,
            samplingScale,
            distance
        ));

        commonCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("common.shaders")), shaderPresetGUI.commonShadersGroup));

        categories.add(new Pair<>(ConfigCategory.createBuilder()
            .name(categoryLabel("generation")), generationCategory));

        generationCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("generation.visual")), generationVisualGroup));
        generationVisualGroup.addAll(List.of(
            randomPlacement,
            fuzziness,
            sparsity,
            yRange,
            yOffset,
            spacing,
            samplingScale,
            shuffle
        ));

        generationCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("generation.performance")), generationPerformanceGroup));
        generationPerformanceGroup.addAll(List.of(
            distance,
            chunkSize
        ));

        categories.add(new Pair<>(ConfigCategory.createBuilder()
            .name(categoryLabel("appearance")), appearanceCategory));

        appearanceCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("appearance.geometry")), appearanceGeometryGroup));
        appearanceGeometryGroup.addAll(List.of(
            sizeXZ,
            sizeY,
            scaleFalloffMin,
            travelSpeed,
            windEffectFactor,
            windSpeedFactor
        ));

        appearanceCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("appearance.visibility")), appearanceVisibilityGroup));
        appearanceVisibilityGroup.addAll(List.of(
            enabled,
            shaderPresetGUI.opacity,
            shaderPresetGUI.opacityFactor,
            shaderPresetGUI.opacityExponent,
            fogRangeFactor
        ));

        appearanceCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("appearance.color")), appearanceColorGroup));
        appearanceColorGroup.addAll(List.of(
            colorVariationFactor,
            shaderPresetGUI.gamma,
            shaderPresetGUI.dayBrightness,
            shaderPresetGUI.nightBrightness,
            shaderPresetGUI.saturation,
            shaderPresetGUI.tint
        ));

        appearanceCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("appearance.sky")), appearanceSkyGroup));
        appearanceSkyGroup.add(celestialBodyHalo);

        categories.add(new Pair<>(ConfigCategory.createBuilder()
            .name(categoryLabel("performance")), performanceCategory));

        performanceCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("performance.generation")), performanceGenerationGroup));
        performanceGenerationGroup.addAll(List.of(
            spacing,
            chunkSize,
            distance,
            sparsity,
            fuzziness,
            shuffle
        ));

        performanceCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("performance.technical")), performanceTechnicalGroup));
        performanceTechnicalGroup.addAll(List.of(usePersistentBuffers, useFrustumCulling));

        categories.add(new Pair<>(ConfigCategory.createBuilder()
            .name(categoryLabel("shaders")), shaderPresetGUI.shadersCategory));

        categories.add(new Pair<>(ConfigCategory.createBuilder()
            .name(categoryLabel("compat")), compatCategory));

        compatCategory.add(new Pair<>(new OptionGroupBuilderWrapper(dimensionsGUI.compatDimensionsListGroup),
            List.of()));

        compatCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("compat.sereneSeasons"))
            .description(OptionDescription.of(groupDescription("compat.sereneSeasons")))
            .collapsed(true), sereneSeasonsCompatGUI.compatSereneSeasonsGroup));

        compatCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("compat.fabricSeasons"))
            .description(OptionDescription.of(groupDescription("compat.fabricSeasons")))
            .collapsed(true), fabricSeasonsCompatGUI.compatFabricSeasonsGroup));
    }

    public static ConfigScreen create(Screen parent) {
        YetAnotherConfigLib yacl = YetAnotherConfigLib.create(ConfigManager.handler(),
            (defaults, config, builder) -> new ConfigGUI(defaults, config).assemble(builder));
        return new ConfigScreen(yacl, parent);
    }

    public YetAnotherConfigLib.Builder assemble(YetAnotherConfigLib.Builder builder) {
        builder = builder
            .save(() -> {
                shaderPresetGUI.onSave();
                config.selectedPreset = MathHelper.clamp(config.selectedPreset, 0, config.presets.size());
                config.sortPresets();
                ConfigManager.handler().save();
            })
            .title(Text.translatable(LANG_KEY_PREFIX + ".title"));

        for (Pair<ConfigCategory.Builder, List<Pair<OptionGroup.Builder, List<Option<?>>>>> categoryPair : categories) {
            ConfigCategory.Builder categoryBuilder = categoryPair.getLeft();
            for (Pair<OptionGroup.Builder, List<Option<?>>> groupPair : categoryPair.getRight()) {
                OptionGroup.Builder groupBuilder = groupPair.getLeft();
                if (!groupPair.getRight().isEmpty())
                    groupBuilder.options(groupPair.getRight());
                categoryBuilder.group(groupBuilder.build());
            }
            builder.category(categoryBuilder.build());
        }

        return builder;
    }

    static <T> YACLOptionBuilder<T> createOption(Class<T> typeClass, String key) {
        return createOption(typeClass, key, true);
    }

    static <T> YACLOptionBuilder<T> createOption(Class<T> typeClass, String key, boolean hasDescription) {
        YACLOptionBuilder<T> builder = YACLOptionBuilder.create(Option.<T>createBuilder())
            .name(optionLabel(key))
            .instant(true);
        if (hasDescription) builder.description(OptionDescription.of(optionDescription(key)));
        return builder;
    }

    public static final String LANG_KEY_PREFIX = BetterCloudsStatic.MODID + ".config";

    static Text formatAsBlocksPerSecond(Float value) {
        return Text.translatable(LANG_KEY_PREFIX + ".unit.blocks_per_second", String.format("%.1f", value * 20));
    }

    static Text formatAsPercent(float value) {
        return Text.translatable(LANG_KEY_PREFIX + ".unit.percent", ((int) (value * 100)));
    }

    static Text formatAsTimes(float value) {
        return Text.translatable(LANG_KEY_PREFIX + ".unit.times", String.format("%.2f", value));
    }

    static Text formatAsDays(float value) {
        return Text.translatable("gui.days", String.format("%.2f", value));
    }

    static Text formatAsDegrees(Float value) {
        return Text.translatable(LANG_KEY_PREFIX + ".unit.degrees", String.format("%.0f", value));
    }

    static Text formatAsTwoDecimals(Float value) {
        return Text.literal(String.format("%,.2f", value).replaceAll("[\u00a0\u202F]", " "));
    }

    static Text categoryLabel(String key) {
        return Text.translatable(LANG_KEY_PREFIX + ".category." + key);
    }

    static Text groupLabel(String key) {
        return Text.translatable(LANG_KEY_PREFIX + ".group." + key);
    }

    static Text optionLabel(String key) {
        return Text.translatable(LANG_KEY_PREFIX + ".entry." + key);
    }

    static Text optionDescription(String key) {
        return Text.translatable(LANG_KEY_PREFIX + ".entry." + key + ".description");
    }

    static Text groupDescription(String key) {
        return Text.translatable(LANG_KEY_PREFIX + ".group." + key + ".description");
    }
}
