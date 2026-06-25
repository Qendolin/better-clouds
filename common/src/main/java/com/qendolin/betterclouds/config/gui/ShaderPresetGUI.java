package com.qendolin.betterclouds.config.gui;

import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.config.compat.ShaderPresetConfig;
import com.qendolin.betterclouds.gui.*;
import com.qendolin.betterclouds.mixin.duck.OptionDuck;
import com.qendolin.betterclouds.mixin.duck.StringControllerDuck;
import com.qendolin.betterclouds.util.Tuple;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.gui.controllers.ColorController;
import dev.isxander.yacl3.gui.controllers.TickBoxController;
import dev.isxander.yacl3.gui.controllers.slider.FloatSliderController;
import dev.isxander.yacl3.gui.controllers.slider.IntegerSliderController;
import dev.isxander.yacl3.gui.controllers.string.StringController;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.qendolin.betterclouds.config.gui.ConfigGUI.*;

public class ShaderPresetGUI {

    public final Option<Float> opacity;
    public final Option<Float> opacityFactor;
    public final Option<Float> opacityExponent;
    public final Option<Float> sunPathAngle;
    public final Option<Float> upscaleResolutionFactor;
    public final Option<Integer> selectedPreset;
    public final Option<String> presetTitle;
    public final Option<String> description;
    public final List<Integer> worldCurvatureValues = List.of(0, -256, -512, -1024, -2048, -4096, -8192, -16384, 16384, 8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16);
    public final Option<Integer> worldCurvatureSize; // option value is the index
    public final Option<Float> saturation;
    public final Option<Color> tint;
    public final Option<Color> bottomTint;
    public final Option<Float> gamma;
    public final Option<Float> dayBrightness;
    public final Option<Float> nightBrightness;
    public final Option<Integer> sunriseStartTime;
    public final Option<Integer> sunriseEndTime;
    public final Option<Integer> sunsetStartTime;
    public final Option<Integer> sunsetEndTime;
    public final LabelOption irisDisclaimer;
    public final Option<Boolean> irisSupport;
    public final Option<Boolean> cloudOverride;
    public final Option<Boolean> useIrisFBO;
    public final ButtonOption copyPresetButton;
    public final ButtonOption removePresetButton;
    public final List<Tuple<OptionGroup.Builder, List<Option<?>>>> shadersCategory = new ArrayList<>();
    public final List<Option<?>> commonShadersGroup = new ArrayList<>();
    public final List<Option<?>> shadersGeneralGroup = new ArrayList<>();
    public final List<Option<?>> shadersPresetGroup = new ArrayList<>();
    public final List<Option<?>> shadersColorGroup = new ArrayList<>();
    public final List<Option<?>> shadersMiscGroup = new ArrayList<>();
    public final List<Option<?>> shadersTechnicalGroup = new ArrayList<>();
    private final Config config;
    private final List<Option<?>> shaderConfigPresetOptions = new ArrayList<>();

    private final List<ShaderPresetConfig> presetsToBeDeleted = new ArrayList<>();

    public ShaderPresetGUI(Config defaults, Config config) {
        this.config = config;
        config.sortShaderPresets();

        this.presetTitle = createOption(String.class, "presetTitle", false)
                .binding("", () -> config.shaderPreset().title, val -> config.shaderPreset().title = val)
                .customController(StringController::new)
                .build();
        this.description = createOption(String.class, "presetDescription", false)
                .binding("", () -> config.shaderPreset().description, val -> config.shaderPreset().description = val)
                .customController(StringController::new)
                .build();
        this.selectedPreset = createOption(int.class, "shaderPreset")
                .binding(defaults.selectedPreset, () -> config.selectedPreset, val -> config.selectedPreset = val)
                .customController(opt -> new SelectDropdownController<>(opt, config.presets, (_, preset) -> {
                    boolean deleted = presetsToBeDeleted.contains(preset);
                    if (preset.title.isBlank()) {
                        return Component.translatable(LANG_KEY_PREFIX + ".entry.shaderPreset.untitled")
                                .withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(true).withStrikethrough(deleted));
                    } else if (!preset.editable) {
                        return Component.literal(preset.title + " §7(§obuilt-in§r§7)§r");
                    } else {
                        return Component.literal(preset.title).withStyle(style -> style.withStrikethrough(deleted));
                    }
                }))
                .listener((opt, _) -> {
                    // The 'instant' listener gets called later, applyValue is called now manually
                    opt.applyValue();
                    if (opt.controller() instanceof SelectDropdownController<?> select) select.updateValues();
                    shaderConfigPresetOptions.forEach(this::setOptionEditable);
                    updateNonResponsiveOptions();
                })
                .build();
        this.saturation = createOption(float.class, "saturation")
                .binding(defaults.shaderPreset().saturation, () -> config.shaderPreset().saturation, val -> config.shaderPreset().saturation = val)
                .customController(opt -> new FloatSliderController(opt, 0, 2, 0.05f, ConfigGUI::formatAsPercent))
                .build();
        this.tint = createOption(Color.class, "tint")
                .binding(new Color(defaults.shaderPreset().tintRed, defaults.shaderPreset().tintGreen, defaults.shaderPreset().tintBlue), () -> new Color(config.shaderPreset().tintRed, config.shaderPreset().tintGreen, config.shaderPreset().tintBlue), val -> {
                    config.shaderPreset().tintRed = val.getRed() / 255f;
                    config.shaderPreset().tintGreen = val.getGreen() / 255f;
                    config.shaderPreset().tintBlue = val.getBlue() / 255f;
                })
                .customController(ColorController::new)
                .build();
        this.bottomTint = createOption(Color.class, "bottomTint")
                .binding(new Color(defaults.shaderPreset().bottomTintRed, defaults.shaderPreset().bottomTintGreen, defaults.shaderPreset().bottomTintBlue), () -> new Color(config.shaderPreset().bottomTintRed, config.shaderPreset().bottomTintGreen, config.shaderPreset().bottomTintBlue), val -> {
                    config.shaderPreset().bottomTintRed = val.getRed() / 255f;
                    config.shaderPreset().bottomTintGreen = val.getGreen() / 255f;
                    config.shaderPreset().bottomTintBlue = val.getBlue() / 255f;
                })
                .customController(ColorController::new)
                .build();
        this.gamma = createOption(float.class, "gamma")
                .binding(defaults.shaderPreset().gamma, () -> config.shaderPreset().gamma, val -> config.shaderPreset().gamma = val)
                .customController(opt -> new FloatSliderController(opt, -5, 5, 0.01f, ConfigGUI::formatAsTwoDecimals))
                .build();
        this.dayBrightness = createOption(float.class, "dayBrightness")
                .binding(defaults.shaderPreset().dayBrightness, () -> config.shaderPreset().dayBrightness, val -> config.shaderPreset().dayBrightness = val)
                .customController(opt -> new FloatSliderController(opt, 0.1f, 4, 0.01f, ConfigGUI::formatAsPercent))
                .build();
        this.nightBrightness = createOption(float.class, "nightBrightness")
                .binding(defaults.shaderPreset().nightBrightness, () -> config.shaderPreset().nightBrightness, val -> config.shaderPreset().nightBrightness = val)
                .customController(opt -> new FloatSliderController(opt, 0.1f, 4, 0.01f, ConfigGUI::formatAsPercent))
                .build();
        this.sunriseStartTime = createOption(int.class, "sunriseStartTime")
                .binding(defaults.shaderPreset().sunriseStartTime, () -> config.shaderPreset().sunriseStartTime, val -> config.shaderPreset().sunriseStartTime = val)
                .customController(opt -> new CustomIntegerFieldController(opt, -6000, 6000))
                .build();
        this.sunriseEndTime = createOption(int.class, "sunriseEndTime")
                .binding(defaults.shaderPreset().sunriseEndTime, () -> config.shaderPreset().sunriseEndTime, val -> config.shaderPreset().sunriseEndTime = val)
                .customController(opt -> new CustomIntegerFieldController(opt, -6000, 6000))
                .build();
        this.sunsetStartTime = createOption(int.class, "sunsetStartTime")
                .binding(defaults.shaderPreset().sunsetStartTime, () -> config.shaderPreset().sunsetStartTime, val -> config.shaderPreset().sunsetStartTime = val)
                .customController(opt -> new CustomIntegerFieldController(opt, 6000, 18000))
                .build();
        this.sunsetEndTime = createOption(int.class, "sunsetEndTime")
                .binding(defaults.shaderPreset().sunsetEndTime, () -> config.shaderPreset().sunsetEndTime, val -> config.shaderPreset().sunsetEndTime = val)
                .customController(opt -> new CustomIntegerFieldController(opt, 6000, 18000))
                .build();
        this.upscaleResolutionFactor = createOption(float.class, "upscaleResolutionFactor")
                .binding(defaults.shaderPreset().upscaleResolutionFactor, () -> config.shaderPreset().upscaleResolutionFactor, val -> config.shaderPreset().upscaleResolutionFactor = val)
                .customController(opt -> new FloatSliderController(opt, 0.25f, 1.0f, 0.01f, ConfigGUI::formatAsPercent))
                .build();
        this.sunPathAngle = createOption(float.class, "sunPathAngle")
                .binding(defaults.shaderPreset().sunPathAngle, () -> config.shaderPreset().sunPathAngle, val -> config.shaderPreset().sunPathAngle = val)
                .customController(opt -> new FloatSliderController(opt, -60f, 60f, 1f, ConfigGUI::formatAsDegrees))
                .build();
        this.opacityFactor = createOption(float.class, "opacityFactor")
                .binding(defaults.shaderPreset().opacityFactor, () -> config.shaderPreset().opacityFactor, val -> config.shaderPreset().opacityFactor = val)
                .customController(opt -> new FloatSliderController(opt, 0, 1, 0.01f, ConfigGUI::formatAsPercent))
                .build();
        this.opacityExponent = createOption(float.class, "opacityExponent")
                .binding(defaults.shaderPreset().opacityExponent, () -> config.shaderPreset().opacityExponent, val -> config.shaderPreset().opacityExponent = val)
                .customController(opt -> new FloatSliderController(opt, 0.25f, 4f, 0.01f, ConfigGUI::formatAsTwoDecimals))
                .build();
        this.opacity = createOption(float.class, "opacity")
                .binding(defaults.shaderPreset().opacity, () -> config.shaderPreset().opacity, val -> config.shaderPreset().opacity = val)
                .customController(opt -> new FloatSliderController(opt, 0, 1, 0.01f, ConfigGUI::formatAsPercent))
                .build();
        this.worldCurvatureSize = createOption(int.class, "worldCurvatureSize")
                .binding(
                        Math.max(worldCurvatureValues.indexOf(defaults.shaderPreset().worldCurvatureSize), 0),
                        () -> Math.max(worldCurvatureValues.indexOf(config.shaderPreset().worldCurvatureSize), 0),
                        val -> config.shaderPreset().worldCurvatureSize = worldCurvatureValues.get(val))
                .customController(opt -> new IntegerSliderController(opt, 0, worldCurvatureValues.size() - 1, 1,
                        i -> i == 0 ? Component.translatable("options.off") : Component.literal(worldCurvatureValues.get(i).toString())))
                .build();
        shaderConfigPresetOptions.addAll(List.of(
                presetTitle,
                description,
                saturation,
                tint,
                bottomTint,
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
                opacity,
                worldCurvatureSize));
        shaderConfigPresetOptions.forEach(this::setOptionEditable);

        final Component removeButtonRemoveText = Component.translatable(LANG_KEY_PREFIX + ".entry.shaderPreset.remove");
        final Component removeButtonRestoreText = Component.translatable(LANG_KEY_PREFIX + ".entry.shaderPreset.restore");

        this.removePresetButton = CustomButtonOption.createBuilder()
                .name(() -> presetsToBeDeleted.contains(config.shaderPreset()) ? removeButtonRestoreText : removeButtonRemoveText)
                .available(config.presets.size() > 1)
                .action((_, option) -> {
                    if (config.presets.size() <= 1 || !config.shaderPreset().editable) {
                        option.setAvailable(false);
                        return;
                    }
                    if (presetsToBeDeleted.contains(config.shaderPreset())) {
                        presetsToBeDeleted.remove(config.shaderPreset());
                    } else {
                        presetsToBeDeleted.add(config.shaderPreset());
                    }
                })
                .build();
        updateNonResponsiveOptions();
        this.copyPresetButton = CustomButtonOption.createBuilder()
                .name(() -> Component.translatable(LANG_KEY_PREFIX + ".entry.shaderPreset.copy"))
                .action((_, _) -> {
                    ShaderPresetConfig preset = new ShaderPresetConfig(config.shaderPreset());
                    preset.title = Component.translatable(LANG_KEY_PREFIX + ".entry.shaderPreset.copyOf", config.shaderPreset().title).getString();
                    preset.markAsCopy();
                    config.presets.addFirst(preset);
                    selectedPreset.requestSet(0);
                    //noinspection rawtypes
                    if (selectedPreset.controller() instanceof SelectDropdownController select) {
                        select.updateValues();
                    }
                    updateNonResponsiveOptions();
                })
                .build();

        this.irisDisclaimer = LabelOption.create(Component.translatable(LANG_KEY_PREFIX + ".text.shaders"));
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


        commonShadersGroup.addAll(List.of(
                irisDisclaimer,
                irisSupport,
                gamma,
                dayBrightness,
                nightBrightness,
                sunPathAngle
        ));

        shadersCategory.add(new Tuple<>(OptionGroup.createBuilder()
                .name(groupLabel("shaders.general")), shadersGeneralGroup));
        shadersGeneralGroup.addAll(List.of(
                irisDisclaimer,
                irisSupport,
                cloudOverride
        ));

        shadersCategory.add(new Tuple<>(OptionGroup.createBuilder()
                .name(groupLabel("shaders.presets")), shadersPresetGroup));
        shadersPresetGroup.addAll(List.of(selectedPreset,
                presetTitle,
                description,
                copyPresetButton,
                removePresetButton
        ));

        shadersCategory.add(new Tuple<>(OptionGroup.createBuilder()
                .name(groupLabel("shaders.color")), shadersColorGroup));
        shadersColorGroup.addAll(List.of(
                gamma,
                dayBrightness,
                nightBrightness,
                saturation,
                tint,
                bottomTint
        ));

        shadersCategory.add(new Tuple<>(OptionGroup.createBuilder()
                .name(groupLabel("shaders.misc")), shadersMiscGroup));
        shadersMiscGroup.add(worldCurvatureSize);

        shadersCategory.add(new Tuple<>(OptionGroup.createBuilder()
                .name(groupLabel("shaders.technical")), shadersTechnicalGroup));
        shadersTechnicalGroup.addAll(List.of(
                sunPathAngle,
                sunriseStartTime,
                sunriseEndTime,
                sunsetStartTime,
                sunsetEndTime,
                upscaleResolutionFactor,
                useIrisFBO
        ));
    }

    private void setPresetDescription() {
        setPresetDescription(description, description.pendingValue().isBlank() ? "Description is empty" : description.pendingValue());
    }

    private void setPresetDescription(Option<String> descriptionOption, String newValue) {
        ((StringControllerDuck) descriptionOption.controller()).better_clouds$setDescription(
                OptionDescription.of(Component.literal(newValue)));
    }

    private void updateNonResponsiveOptions() {
        setPresetDescription();

        shaderConfigPresetOptions.forEach(this::setOptionEditable);
        if (removePresetButton != null) {
            removePresetButton.setAvailable(config.shaderPreset().editable && config.presets.size() > 1);
        }
    }

    public void setOptionEditable(Option<?> option) {
        boolean editable = config.shaderPreset().editable;
        option.forgetPendingValue();
        option.setAvailable(editable);
        ((OptionDuck) option).better_clouds$appendToDescription(
                OptionDescription.of(
                        !editable ?
                                Component.translatable("betterclouds.config.message.fieldControlledByShaderPreset") :
                                Component.literal("")
                ));
    }

    public void onSave() {
        if (presetsToBeDeleted.isEmpty()) return;

        ShaderPresetConfig currentPreset = config.shaderPreset();
        for (ShaderPresetConfig preset : presetsToBeDeleted) {
            config.presets.remove(preset);
        }
        config.selectedPreset = Mth.clamp(config.presets.indexOf(currentPreset), 0, config.presets.size() - 1);
    }
}
