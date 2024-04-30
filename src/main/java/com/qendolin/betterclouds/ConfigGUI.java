package com.qendolin.betterclouds;

import com.qendolin.betterclouds.gui.ConfigScreen;
import com.qendolin.betterclouds.gui.CustomButtonOption;
import com.qendolin.betterclouds.gui.CustomIntegerFieldController;
import com.qendolin.betterclouds.gui.SelectController;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.ColorController;
import dev.isxander.yacl3.gui.controllers.TickBoxController;
import dev.isxander.yacl3.gui.controllers.slider.FloatSliderController;
import dev.isxander.yacl3.gui.controllers.slider.IntegerSliderController;
import dev.isxander.yacl3.gui.controllers.string.StringController;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigGUI {
    protected final Config config;
    protected final Config defaults;

    public final Option<Integer> chunkSize;
    public final Option<Float> distance;
    public final Option<Float> fuzziness;
    public final Option<Float> spacing;
    public final Option<Float> sparsity;
    public final Option<Boolean> shuffle;
    public final Option<Float> saturation;
    public final Option<Color> tint;
    public final Option<Float> gamma;
    public final Option<Float> dayBrightness;
    public final Option<Float> nightBrightness;
    public final Option<Integer> sunriseStartTime;
    public final Option<Integer> sunriseEndTime;
    public final Option<Integer> sunsetStartTime;
    public final Option<Integer> sunsetEndTime;
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
    public final Option<Float> opacity;
    public final Option<Float> opacityFactor;
    public final Option<Float> opacityExponent;
    public final Option<Float> fadeEdge;
    public final LabelOption irisDisclaimer;
    public final Option<Boolean> irisSupport;
    public final Option<Boolean> cloudOverride;
    public final Option<Float> sunPathAngle;
    public final Option<Boolean> useIrisFBO;
    public final Option<Float> upscaleResolutionFactor;
    public final Option<Boolean> usePersistentBuffers;
    public final Option<Integer> selectedPreset;
    public final Option<String> presetTitle;
    public final ButtonOption copyPresetButton;
    public final ButtonOption removePresetButton;

    protected final List<Pair<ConfigCategory.Builder, List<Pair<OptionGroup.Builder, List<Option<?>>>>>> categories = new ArrayList<>();

    protected final List<Pair<OptionGroup.Builder, List<Option<?>>>> commonCategory = new ArrayList<>();
    protected final List<Pair<OptionGroup.Builder, List<Option<?>>>> generationCategory = new ArrayList<>();
    protected final List<Pair<OptionGroup.Builder, List<Option<?>>>> appearanceCategory = new ArrayList<>();
    protected final List<Pair<OptionGroup.Builder, List<Option<?>>>> performanceCategory = new ArrayList<>();
    protected final List<Pair<OptionGroup.Builder, List<Option<?>>>> shadersCategory = new ArrayList<>();

    protected final List<Option<?>> commonPresetsGroup = new ArrayList<>();
    protected final List<Option<?>> commonGenerationGroup = new ArrayList<>();
    protected final List<Option<?>> commonAppearanceGroup = new ArrayList<>();
    protected final List<Option<?>> commonShadersGroup = new ArrayList<>();
    protected final List<Option<?>> generationVisualGroup = new ArrayList<>();
    protected final List<Option<?>> generationPerformanceGroup = new ArrayList<>();
    protected final List<Option<?>> appearanceGeometryGroup = new ArrayList<>();
    protected final List<Option<?>> appearanceVisibilityGroup = new ArrayList<>();
    protected final List<Option<?>> appearanceColorGroup = new ArrayList<>();
    protected final List<Option<?>> appearanceSkyGroup = new ArrayList<>();
    protected final List<Option<?>> performanceGenerationGroup = new ArrayList<>();
    protected final List<Option<?>> performanceTechnicalGroup = new ArrayList<>();
    protected final List<Option<?>> shadersGeneralGroup = new ArrayList<>();
    protected final List<Option<?>> shadersPresetGroup = new ArrayList<>();
    protected final List<Option<?>> shadersColorGroup = new ArrayList<>();
    protected final List<Option<?>> shadersTechnicalGroup = new ArrayList<>();

    protected final List<Option<?>> shaderConfigPresetOptions = new ArrayList<>();

    protected final List<Config.ShaderConfigPreset> presetsToBeDeleted = new ArrayList<>();

    public ConfigGUI(Config defaults, Config config) {
        this.defaults = defaults;
        this.config = config;
        config.addFirstPreset();
        config.sortPresets();

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
            .customController(opt -> new FloatSliderController(opt, 0, 128, 0.5f))
            .build();
        this.yOffset = createOption(float.class, "yOffset")
            .binding(defaults.yOffset, () -> config.yOffset, val -> config.yOffset = val)
            .customController(opt -> new FloatSliderController(opt, -64, 256, 8))
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
            .customController(opt -> new FloatSliderController(opt, 1, 32, 1))
            .build();
        this.scaleFalloffMin = createOption(float.class, "scaleFalloffMin")
            .binding(defaults.scaleFalloffMin, () -> config.scaleFalloffMin, val -> config.scaleFalloffMin = val)
            .customController(opt -> new FloatSliderController(opt, 0, 1, 0.05f, ConfigGUI::formatAsPercent))
            .build();
        this.travelSpeed = createOption(float.class, "travelSpeed")
            .binding(defaults.travelSpeed, () -> config.travelSpeed, val -> config.travelSpeed = val)
            .customController(opt -> new FloatSliderController(opt, 0, 0.1f, 0.005f, ConfigGUI::formatAsBlocksPerSecond))
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
        this.fadeEdge = createOption(float.class, "fadeEdge")
            .binding(defaults.fadeEdge, () -> config.fadeEdge, val -> config.fadeEdge = val)
            .customController(opt -> new FloatSliderController(opt, 0.1f, 0.5f, 0.01f, ConfigGUI::formatAsPercent))
            .build();
        this.irisDisclaimer = LabelOption.create(Text.translatable(LANG_KEY_PREFIX + ".text.shaders"));
        this.irisSupport = createOption(boolean.class, "irisSupport")
            .binding(defaults.irisSupport, () -> config.irisSupport, val -> config.irisSupport = val)
            .customController(TickBoxController::new)
            .build();
        this.cloudOverride = createOption(boolean.class, "cloudOverride")
            .binding(defaults.cloudOverride, () -> config.cloudOverride, val -> config.cloudOverride = val)
            .customController(TickBoxController::new)
            .build();
        this.useIrisFBO = createOption(boolean.class, "useIrisFBO")
            .binding(defaults.useIrisFBO, () -> config.useIrisFBO, val -> config.useIrisFBO = val)
            .customController(TickBoxController::new)
            .build();
        this.usePersistentBuffers = createOption(boolean.class, "usePersistentBuffers")
            .binding(defaults.usePersistentBuffers, () -> config.usePersistentBuffers, val -> config.usePersistentBuffers = val)
            .customController(TickBoxController::new)
            .build();

        // FIXME: defaults.preset() gives default values defined in the code, not from the `default` preset

        this.selectedPreset = createOption(int.class, "shaderPreset")
            .binding(defaults.selectedPreset, () -> config.selectedPreset, val -> config.selectedPreset = val)
            .customController(opt -> new SelectController<>(opt, config.presets, (i, preset) -> {
                if (preset.title.isBlank()) {
                    return Text.translatable(LANG_KEY_PREFIX + ".entry.shaderPreset.untitled")
                        .styled(style -> style.withColor(Formatting.GRAY).withItalic(true));
                } else if (!preset.editable) {
                    return Text.literal(preset.title)
                        .styled(style -> style.withItalic(true));
                } else if (presetsToBeDeleted.contains(preset)) {
                    return Text.literal(preset.title).styled(style -> style.withStrikethrough(true));
                } else {
                    return Text.literal(preset.title);
                }
            }))
            .listener((opt, i) -> {
                // The 'instant' listener gets called later, applyValue is called now manually
                opt.applyValue();
                if (opt.controller() instanceof SelectController select) {
                    select.updateValues();
                }
                for (Option<?> option : shaderConfigPresetOptions) {
                    option.forgetPendingValue();
                    option.setAvailable(config.preset().editable);
                }
                updateRemovePresetButton();
            })
            .build();
        this.presetTitle = createOption(String.class, "presetTitle", false)
            .binding("", () -> config.preset().title, val -> config.preset().title = val)
            .customController(StringController::new)
            .build();
        this.saturation = createOption(float.class, "saturation")
            .binding(defaults.preset().saturation, () -> config.preset().saturation, val -> config.preset().saturation = val)
            .customController(opt -> new FloatSliderController(opt, 0, 2, 0.05f, ConfigGUI::formatAsPercent))
            .build();
        this.tint = createOption(Color.class, "tint")
            .binding(new Color(defaults.preset().tintRed, defaults.preset().tintGreen, defaults.preset().tintBlue), () -> new Color(config.preset().tintRed, config.preset().tintGreen, config.preset().tintBlue), val -> {
                config.preset().tintRed = val.getRed() / 255f;
                config.preset().tintGreen = val.getGreen() / 255f;
                config.preset().tintBlue = val.getBlue() / 255f;
            })
            .customController(ColorController::new)
            .build();
        this.gamma = createOption(float.class, "gamma")
            .binding(defaults.preset().gamma, () -> config.preset().gamma, val -> config.preset().gamma = val)
            .customController(opt -> new FloatSliderController(opt, -5, 5, 0.01f, ConfigGUI::formatAsTwoDecimals))
            .build();
        this.dayBrightness = createOption(float.class, "dayBrightness")
            .binding(defaults.preset().dayBrightness, () -> config.preset().dayBrightness, val -> config.preset().dayBrightness = val)
            .customController(opt -> new FloatSliderController(opt, 0.1f, 4, 0.01f, ConfigGUI::formatAsPercent))
            .build();
        this.nightBrightness = createOption(float.class, "nightBrightness")
            .binding(defaults.preset().nightBrightness, () -> config.preset().nightBrightness, val -> config.preset().nightBrightness = val)
            .customController(opt -> new FloatSliderController(opt, 0.1f, 4, 0.01f, ConfigGUI::formatAsPercent))
            .build();
        this.sunriseStartTime = createOption(int.class, "sunriseStartTime")
            .binding(defaults.preset().sunriseStartTime, () -> config.preset().sunriseStartTime, val -> config.preset().sunriseStartTime = val)
            .customController(opt -> new CustomIntegerFieldController(opt, -6000, 6000))
            .build();
        this.sunriseEndTime = createOption(int.class, "sunriseEndTime")
            .binding(defaults.preset().sunriseEndTime, () -> config.preset().sunriseEndTime, val -> config.preset().sunriseEndTime = val)
            .customController(opt -> new CustomIntegerFieldController(opt, -6000, 6000))
            .build();
        this.sunsetStartTime = createOption(int.class, "sunsetStartTime")
            .binding(defaults.preset().sunsetStartTime, () -> config.preset().sunsetStartTime, val -> config.preset().sunsetStartTime = val)
            .customController(opt -> new CustomIntegerFieldController(opt, 6000, 18000))
            .build();
        this.sunsetEndTime = createOption(int.class, "sunsetEndTime")
            .binding(defaults.preset().sunsetEndTime, () -> config.preset().sunsetEndTime, val -> config.preset().sunsetEndTime = val)
            .customController(opt -> new CustomIntegerFieldController(opt, 6000, 18000))
            .build();
        this.upscaleResolutionFactor = createOption(float.class, "upscaleResolutionFactor")
            .binding(defaults.preset().upscaleResolutionFactor, () -> config.preset().upscaleResolutionFactor, val -> config.preset().upscaleResolutionFactor = val)
            .customController(opt -> new FloatSliderController(opt, 0.25f, 1.0f, 0.01f, ConfigGUI::formatAsPercent))
            .build();
        this.sunPathAngle = createOption(float.class, "sunPathAngle")
            .binding(defaults.preset().sunPathAngle, () -> config.preset().sunPathAngle, val -> config.preset().sunPathAngle = val)
            .customController(opt -> new FloatSliderController(opt, -60f, 60f, 1f, ConfigGUI::formatAsDegrees))
            .build();
        this.opacityFactor = createOption(float.class, "opacityFactor")
            .binding(defaults.preset().opacityFactor, () -> config.preset().opacityFactor, val -> config.preset().opacityFactor = val)
            .customController(opt -> new FloatSliderController(opt, 0, 1, 0.01f, ConfigGUI::formatAsPercent))
            .build();
        this.opacityExponent = createOption(float.class, "opacityExponent")
            .binding(defaults.preset().opacityExponent, () -> config.preset().opacityExponent, val -> config.preset().opacityExponent = val)
            .customController(opt -> new FloatSliderController(opt, 0.25f, 4f, 0.01f, ConfigGUI::formatAsTwoDecimals))
            .build();
        this.opacity = createOption(float.class, "opacity")
            .binding(defaults.preset().opacity, () -> config.preset().opacity, val -> config.preset().opacity = val)
            .customController(opt -> new FloatSliderController(opt, 0, 1, 0.01f, ConfigGUI::formatAsPercent))
            .build();
        shaderConfigPresetOptions.addAll(List.of(presetTitle,
            saturation,
            tint,
            gamma,
            dayBrightness,
            nightBrightness,
            sunriseStartTime,
            sunriseEndTime,
            sunsetStartTime,
            sunsetEndTime,
            upscaleResolutionFactor,
            sunPathAngle,
            opacityFactor,
            opacityExponent,
            opacity));
        shaderConfigPresetOptions.forEach(opt -> opt.setAvailable(config.preset().editable));

        final Text removeButtonRemoveText = Text.translatable(LANG_KEY_PREFIX + ".entry.shaderPreset.remove");
        final Text removeButtonRestoreText = Text.translatable(LANG_KEY_PREFIX + ".entry.shaderPreset.restore");

        this.removePresetButton = CustomButtonOption.createBuilder()
            .name(() -> presetsToBeDeleted.contains(config.preset()) ? removeButtonRestoreText : removeButtonRemoveText)
            .available(config.presets.size() > 1)
            .action((screen, option) -> {
                if (config.presets.size() <= 1 || !config.preset().editable) {
                    option.setAvailable(false);
                    return;
                }
                if (presetsToBeDeleted.contains(config.preset())) {
                    presetsToBeDeleted.remove(config.preset());
                } else {
                    presetsToBeDeleted.add(config.preset());
                }
            })
            .build();
        updateRemovePresetButton();
        this.copyPresetButton = CustomButtonOption.createBuilder()
            .name(() -> Text.translatable(LANG_KEY_PREFIX + ".entry.shaderPreset.copy"))
            .action((screen, buttonOption) -> {
                Config.ShaderConfigPreset preset = new Config.ShaderConfigPreset(config.preset());
                preset.title = Text.translatable(LANG_KEY_PREFIX + ".entry.shaderPreset.copyOf", config.preset().title).getString();
                preset.markAsCopy();
                config.presets.add(0, preset);
                selectedPreset.requestSet(0);
                if (selectedPreset.controller() instanceof SelectController select) {
                    select.updateValues();
                }
                updateRemovePresetButton();
            })
            .build();

        categories.add(new Pair<>(ConfigCategory.createBuilder()
            .name(categoryLabel("common")), commonCategory));
        commonCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("common.presets")), commonPresetsGroup));
        commonPresetsGroup.addAll(List.of(selectedPreset, presetTitle, copyPresetButton, removePresetButton));
        commonCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("common.generation")), commonGenerationGroup));
        commonGenerationGroup.addAll(List.of(sizeXZ, sizeY, spacing, samplingScale, distance));
        commonCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("common.appearance")), commonAppearanceGroup));
        commonAppearanceGroup.addAll(List.of(enabled, opacity, opacityFactor));
        commonCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("common.shaders")), commonShadersGroup));
        commonShadersGroup.addAll(List.of(irisDisclaimer, irisSupport, gamma, dayBrightness, nightBrightness, sunPathAngle));

        categories.add(new Pair<>(ConfigCategory.createBuilder()
            .name(categoryLabel("generation")), generationCategory));
        generationCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("generation.visual")), generationVisualGroup));
        generationVisualGroup.addAll(List.of(
            randomPlacement, fuzziness, sparsity, yRange, yOffset, spacing, samplingScale, shuffle
        ));
        generationCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("generation.performance")), generationPerformanceGroup));
        generationPerformanceGroup.addAll(List.of(distance, chunkSize));

        categories.add(new Pair<>(ConfigCategory.createBuilder()
            .name(categoryLabel("appearance")), appearanceCategory));
        appearanceCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("appearance.geometry")), appearanceGeometryGroup));
        appearanceGeometryGroup.addAll(List.of(sizeXZ, sizeY, scaleFalloffMin, travelSpeed, windEffectFactor, windSpeedFactor));
        appearanceCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("appearance.visibility")), appearanceVisibilityGroup));
        appearanceVisibilityGroup.addAll(List.of(enabled, opacity, opacityFactor, opacityExponent, fadeEdge));
        appearanceCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("appearance.color")), appearanceColorGroup));
        appearanceColorGroup.addAll(List.of(colorVariationFactor, gamma, dayBrightness, nightBrightness, saturation, tint));
        appearanceCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("appearance.sky")), appearanceSkyGroup));
        appearanceSkyGroup.add(celestialBodyHalo);

        categories.add(new Pair<>(ConfigCategory.createBuilder()
            .name(categoryLabel("performance")), performanceCategory));
        performanceCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("performance.generation")), performanceGenerationGroup));
        performanceGenerationGroup.addAll(List.of(spacing, chunkSize, distance, sparsity, fuzziness, shuffle));
        performanceCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("performance.technical")), performanceTechnicalGroup));
        performanceTechnicalGroup.add(usePersistentBuffers);

        categories.add(new Pair<>(ConfigCategory.createBuilder()
            .name(categoryLabel("shaders")), shadersCategory));
        shadersCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("shaders.general")), shadersGeneralGroup));
        shadersGeneralGroup.addAll(List.of(irisDisclaimer, irisSupport, cloudOverride));
        shadersCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("shaders.presets")), shadersPresetGroup));
        shadersPresetGroup.addAll(List.of(selectedPreset, presetTitle, copyPresetButton, removePresetButton));
        shadersCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("shaders.color")), shadersColorGroup));
        shadersColorGroup.addAll(List.of(gamma, dayBrightness, nightBrightness, saturation, tint));
        shadersCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("shaders.technical")), shadersTechnicalGroup));

        shadersTechnicalGroup.addAll(List.of(sunPathAngle,
            sunriseStartTime,
            sunriseEndTime,
            sunsetStartTime,
            sunsetEndTime,
            upscaleResolutionFactor,
            useIrisFBO));
    }

    private void updateRemovePresetButton() {
        if (removePresetButton == null) return;
        removePresetButton.setAvailable(config.preset().editable && config.presets.size() > 1);
    }

    public static ConfigScreen create(Screen parent) {
        YetAnotherConfigLib yacl = YetAnotherConfigLib.create(Main.getConfigHandler(),
            (defaults, config, builder) -> new ConfigGUI(defaults, config).assemble(builder));
        return new ConfigScreen(yacl, parent);
    }

    public YetAnotherConfigLib.Builder assemble(YetAnotherConfigLib.Builder builder) {
        builder = builder
            .save(() -> {
                for (Config.ShaderConfigPreset preset : presetsToBeDeleted) {
                    config.presets.remove(preset);
                }
                config.selectedPreset = MathHelper.clamp(config.selectedPreset, 0, config.presets.size());
                config.sortPresets();
                Main.getConfigHandler().save();
            })
            .title(Text.translatable(LANG_KEY_PREFIX + ".title"));

        for (Pair<ConfigCategory.Builder, List<Pair<OptionGroup.Builder, List<Option<?>>>>> categoryPair : categories) {
            ConfigCategory.Builder categoryBuilder = categoryPair.getLeft();
            for (Pair<OptionGroup.Builder, List<Option<?>>> groupPair : categoryPair.getRight()) {
                if (groupPair.getRight().isEmpty()) continue;
                OptionGroup.Builder groupBuilder = groupPair.getLeft();
                groupBuilder.options(groupPair.getRight());
                categoryBuilder.group(groupBuilder.build());
            }
            builder.category(categoryBuilder.build());
        }

        return builder;
    }

    private static <T> Option.Builder<T> createOption(Class<T> typeClass, String key) {
        return createOption(typeClass, key, true);
    }

    private static <T> Option.Builder<T> createOption(Class<T> typeClass, String key, boolean hasDescription) {
        Option.Builder<T> builder = Option.<T>createBuilder()
            .name(optionLabel(key))
            .instant(true);
        if (hasDescription) builder.description(OptionDescription.of(optionDescription(key)));
        return builder;
    }

    public static final String LANG_KEY_PREFIX = Main.MODID + ".config";

    private static Text formatAsBlocksPerSecond(Float value) {
        return Text.translatable(LANG_KEY_PREFIX + ".unit.blocks_per_second", String.format("%.1f", value * 20));
    }

    private static Text formatAsPercent(float value) {
        return Text.translatable(LANG_KEY_PREFIX + ".unit.percent", ((int) (value * 100)));
    }

    private static Text formatAsTimes(float value) {
        return Text.translatable(LANG_KEY_PREFIX + ".unit.times", String.format("%.2f", value));
    }

    private static Text formatAsDegrees(Float value) {
        return Text.translatable(LANG_KEY_PREFIX + ".unit.degrees", String.format("%.0f", value));
    }

    private static Text categoryLabel(String key) {
        return Text.translatable(LANG_KEY_PREFIX + ".category." + key);
    }

    private static Text groupLabel(String key) {
        return Text.translatable(LANG_KEY_PREFIX + ".group." + key);
    }

    private static Text optionLabel(String key) {
        return Text.translatable(LANG_KEY_PREFIX + ".entry." + key);
    }

    private static Text optionDescription(String key) {
        return Text.translatable(LANG_KEY_PREFIX + ".entry." + key + ".description");
    }

    private static Text formatAsTwoDecimals(Float value) {
        return Text.literal(String.format("%,.2f", value).replaceAll("[\u00a0\u202F]", " "));
    }
}
