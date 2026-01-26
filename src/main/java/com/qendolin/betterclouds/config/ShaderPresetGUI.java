package com.qendolin.betterclouds.config;

import com.qendolin.betterclouds.gui.CustomButtonOption;
import com.qendolin.betterclouds.gui.CustomIntegerFieldController;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.gui.controllers.ColorController;
import dev.isxander.yacl3.gui.controllers.TickBoxController;
import dev.isxander.yacl3.gui.controllers.slider.FloatSliderController;
import dev.isxander.yacl3.gui.controllers.slider.IntegerSliderController;
import dev.isxander.yacl3.gui.controllers.string.StringController;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

//? if >=1.21.11 {
import com.qendolin.betterclouds.gui.SelectDropdownController;
//?} else {
/*import com.qendolin.betterclouds.gui.SelectController;
*///?}

import static com.qendolin.betterclouds.config.ConfigGUI.*;

public class ShaderPresetGUI {

    private final Config config;

    public final Option<Float> opacity;
    public final Option<Float> opacityFactor;
    public final Option<Float> opacityExponent;
    public final Option<Float> sunPathAngle;
    public final Option<Float> upscaleResolutionFactor;
    public final Option<Integer> selectedPreset;
    public final Option<String> presetTitle;
    public final List<Integer> worldCurvatureValues = List.of(0, -256, -512, -1024, -2048, -4096, -8192, -16384, 16384, 8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16);
    public final Option<Integer> worldCurvatureSize; // option value is the index

    public final Option<Float> saturation;
    public final Option<Color> tint;
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

    public final List<Pair<OptionGroup.Builder, List<Option<?>>>> shadersCategory = new ArrayList<>();

    public final List<Option<?>> commonShadersGroup = new ArrayList<>();
    public final List<Option<?>> shadersGeneralGroup = new ArrayList<>();
    public final List<Option<?>> shadersPresetGroup = new ArrayList<>();
    public final List<Option<?>> shadersColorGroup = new ArrayList<>();
    public final List<Option<?>> shadersMiscGroup = new ArrayList<>();
    public final List<Option<?>> shadersTechnicalGroup = new ArrayList<>();

    private final List<Option<?>> shaderConfigPresetOptions = new ArrayList<>();

    private final List<ShaderPresetConfig> presetsToBeDeleted = new ArrayList<>();

    public ShaderPresetGUI(Config defaults, Config config) {
        this.config = config;
        config.addFirstPreset();
        config.sortPresets();

        // FIXME: defaults.preset() gives default values defined in the code, not from the `default` preset

        this.selectedPreset = createOption(int.class, "shaderPreset")
            .binding(defaults.selectedPreset, () -> config.selectedPreset, val -> config.selectedPreset = val)
            .customController(opt -> new /*? if >=1.21.11 {*/ SelectDropdownController /*?} else {*/ /*SelectController *//*?}*/ <>(opt, config.presets, (i, preset) -> {
                boolean deleted = presetsToBeDeleted.contains(preset);
                if (preset.title.isBlank()) {
                    return Text.translatable(LANG_KEY_PREFIX + ".entry.shaderPreset.untitled")
                        .styled(style -> style.withColor(Formatting.GRAY).withItalic(true).withStrikethrough(deleted));
                } else if (!preset.editable) {
                    return Text.literal(preset.title)
                        .styled(style -> style.withItalic(true));
                } else {
                    return Text.literal(preset.title).styled(style -> style.withStrikethrough(deleted));
                }
            }))
            .listener((opt, i) -> {
                // The 'instant' listener gets called later, applyValue is called now manually
                opt.applyValue();
                //noinspection rawtypes
                if (opt.controller() instanceof /*? if >=1.21.11 {*/ SelectDropdownController /*?} else {*/ /*SelectController *//*?}*/  select) {
                    select.updateValues();
                }
                for (Option<?> option : shaderConfigPresetOptions) {
                    option.forgetPendingValue();
                    option.setAvailable(config.preset().editable);
                }
                updateNonResponsiveOptions();
            })
            .build();
        this.presetTitle = createOption(String.class, "presetTitle", false)
            .binding("", () -> config.preset().title, val -> {
                if (config.preset().editable) {
                    config.preset().title = val;
                }
            })
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
        this.worldCurvatureSize = createOption(int.class, "worldCurvatureSize")
            .binding(
                Math.max(worldCurvatureValues.indexOf(defaults.preset().worldCurvatureSize), 0),
                () -> Math.max(worldCurvatureValues.indexOf(config.preset().worldCurvatureSize), 0),
                val -> config.preset().worldCurvatureSize = worldCurvatureValues.get(val))
            .customController(opt -> new IntegerSliderController(opt, 0, worldCurvatureValues.size() - 1, 1,
                i -> i == 0 ? Text.translatable("options.off") : Text.literal(worldCurvatureValues.get(i).toString())))
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
            opacity,
            worldCurvatureSize));
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
        updateNonResponsiveOptions();
        this.copyPresetButton = CustomButtonOption.createBuilder()
            .name(() -> Text.translatable(LANG_KEY_PREFIX + ".entry.shaderPreset.copy"))
            .action((screen, buttonOption) -> {
                ShaderPresetConfig preset = new ShaderPresetConfig(config.preset());
                preset.title = Text.translatable(LANG_KEY_PREFIX + ".entry.shaderPreset.copyOf", config.preset().title).getString();
                preset.markAsCopy();
                config.presets.add(0, preset);
                selectedPreset.requestSet(0);
                //noinspection rawtypes
                if (selectedPreset.controller() instanceof /*? if >=1.21.11 {*/ SelectDropdownController /*?} else {*/ /*SelectController *//*?}*/  select) {
                    select.updateValues();
                }
                updateNonResponsiveOptions();
            })
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


        commonShadersGroup.addAll(List.of(
            irisDisclaimer,
            irisSupport,
            gamma,
            dayBrightness,
            nightBrightness,
            sunPathAngle
        ));

        shadersCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("shaders.general")), shadersGeneralGroup));
        shadersGeneralGroup.addAll(List.of(
            irisDisclaimer,
            irisSupport,
            cloudOverride
        ));

        shadersCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("shaders.presets")), shadersPresetGroup));
        shadersPresetGroup.addAll(List.of(selectedPreset,
            presetTitle,
            copyPresetButton,
            removePresetButton
        ));

        shadersCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("shaders.color")), shadersColorGroup));
        shadersColorGroup.addAll(List.of(
            gamma,
            dayBrightness,
            nightBrightness,
            saturation,
            tint
        ));

        shadersCategory.add(new Pair<>(OptionGroup.createBuilder()
            .name(groupLabel("shaders.misc")), shadersMiscGroup));
        shadersMiscGroup.add(worldCurvatureSize);

        shadersCategory.add(new Pair<>(OptionGroup.createBuilder()
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

    private void updateNonResponsiveOptions() {
        if (removePresetButton != null) {
            removePresetButton.setAvailable(config.preset().editable && config.presets.size() > 1);
        }
        if (presetTitle != null) {
            // Yacl issue #263
            String title = config.preset().title;
            presetTitle.stateManager().set(title + " "); // some value that is not equal
            presetTitle.stateManager().set(title);
        }
    }

    public void onSave() {
        for (ShaderPresetConfig preset : presetsToBeDeleted) {
            config.presets.remove(preset);
        }
    }
}
