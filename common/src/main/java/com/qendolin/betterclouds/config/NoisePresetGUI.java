package com.qendolin.betterclouds.config;

import com.qendolin.betterclouds.duck.ListOptionDuck;
import com.qendolin.betterclouds.duck.StringControllerDuck;
import com.qendolin.betterclouds.gui.CustomButtonOption;
import com.qendolin.betterclouds.gui.SelectDropdownController;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.gui.controllers.string.StringController;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.qendolin.betterclouds.config.ConfigGUI.*;

public class NoisePresetGUI {
    public final Option<Integer> selectedNoisePreset;
    public final ListOption<String> noiseBuilder;
    public final Option<String> presetTitle;
    public final Option<String> description;
    public final LabelOption configInvalidLabel;
    public final ButtonOption copyPresetButton;
    public final ButtonOption removePresetButton;

    public final List<Tuple<OptionGroup.Builder, List<Option<?>>>> noiseCategory = new ArrayList<>();

    public final List<Option<?>> noisePresetGroup = new ArrayList<>();
    private final List<Option<?>> noisePresetOptions = new ArrayList<>();
    private final List<Option<?>> noiseErrorMessages = new ArrayList<>();
    private final Config config;
    private final List<NoisePresetConfig> presetsToBeDeleted = new ArrayList<>();

    public Component configLabelString = Component.translatable("betterclouds.config.noisePreset.presetValid", "");

    public NoisePresetGUI(Config defaults, Config config) {
        this.config = config;
        config.addFirstNoisePreset();
        config.sortNoisePresets();

        this.presetTitle = createOption(String.class, "presetTitle", false)
                .binding("", () -> config.noisePreset().title, val -> config.noisePreset().title = val)
                .customController(StringController::new)
                .build();
        this.description = createOption(String.class, "presetDescription", false)
                .binding("", () -> config.noisePreset().description, val -> {
                    config.noisePreset().description = val;
                    setPresetDescription();
                })
                .customController(StringController::new)
                .build();
        this.configInvalidLabel = LabelOption.createBuilder()
                .state(StateManager.createInstant(
                        configLabelString,
                        () -> configLabelString,
                        s -> configLabelString = s
                ))
                .build();
        this.noiseBuilder = ListOption.<String>createBuilder()
                .name(groupLabel("noise.builder"))
                .description(OptionDescription.of(groupDescription("noise.builder")))
                .state(StateManager.createInstant(
                        defaults.noisePreset().octavesToStringList(),
                        () -> config.noisePreset().octavesToStringList(),
                        l -> configInvalidLabel.requestSet(Component.translatable(
                                config.noisePreset().octavesFromStringList(l) ? "betterclouds.config.noisePreset.presetValid" :
                                        "betterclouds.config.noisePreset.presetInvalid",
                                formatLastException()
                        ))
                ))
                .listener((option, _) -> option.applyValue())
                .controller(StringControllerBuilder::create)    // todo: move cursed string manipulation into custom controller
                .collapsed(false)
                .minimumNumberOfEntries(1)
                .maximumNumberOfEntries(10)
                .initial("")
                .build();
        ((ListOptionDuck) noiseBuilder).better_clouds$setForceExpanded(true);

        this.selectedNoisePreset = createOption(int.class, "noisePreset")
                .binding(defaults.selectedNoisePreset, () -> config.selectedNoisePreset, val -> config.selectedNoisePreset = val)
                .customController(opt -> new SelectDropdownController<>(opt, config.noisePresets, (_, preset) -> {
                    if (preset.title.isBlank()) {
                        return Component.translatable(LANG_KEY_PREFIX + ".entry.noisePreset.untitled")
                                .withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(true));
                    } else if (!preset.editable) {
                        return Component.literal(preset.title + " §7(§obuilt-in§r§7)§r");
                    } else {
                        return Component.literal(preset.title);
                    }
                }))
                .listener((opt, _) -> {
                    opt.applyValue();
                    //noinspection rawtypes
                    if (opt.controller() instanceof SelectDropdownController select) {
                        select.updateValues();
                    }
                    syncPresetOptions();
                })
                .build();

        final Component removeButtonRemoveText = Component.translatable(LANG_KEY_PREFIX + ".entry.noisePreset.remove");
        final Component removeButtonRestoreText = Component.translatable(LANG_KEY_PREFIX + ".entry.noisePreset.restore");

        this.removePresetButton = CustomButtonOption.createBuilder()
                .name(() -> presetsToBeDeleted.contains(config.noisePreset()) ? removeButtonRestoreText : removeButtonRemoveText)
                .available(config.noisePresets.size() > 1)
                .action((_, option) -> {
                    if (config.noisePresets.size() <= 1 || !config.noisePreset().editable) {
                        option.setAvailable(false);
                        return;
                    }
                    if (presetsToBeDeleted.contains(config.noisePreset())) {
                        presetsToBeDeleted.remove(config.noisePreset());
                    } else {
                        presetsToBeDeleted.add(config.noisePreset());
                    }
                })
                .build();

        this.copyPresetButton = CustomButtonOption.createBuilder()
                .name(() -> Component.translatable(LANG_KEY_PREFIX + ".entry.noisePreset.copy"))
                .action((_, _) -> {
                    NoisePresetConfig preset = new NoisePresetConfig(config.noisePreset());
                    preset.title = Component.translatable(LANG_KEY_PREFIX + ".entry.noisePreset.copyOf", config.noisePreset().title).getString();
                    preset.markAsCopy();
                    config.noisePresets.addFirst(preset);
                    selectedNoisePreset.requestSet(0);
                    //noinspection rawtypes
                    if (selectedNoisePreset.controller() instanceof SelectDropdownController select) {
                        select.updateValues();
                    }
                    updateNonResponsiveOptions();
                })
                .build();

        noiseCategory.add(new Tuple<>(OptionGroup.createBuilder().name(groupLabel("noise.preset")), noisePresetGroup));
        noiseCategory.add(new Tuple<>(OptionGroup.createBuilder().name(groupLabel("noise.issues")), noiseErrorMessages));

        noisePresetGroup.addAll(Arrays.asList(
                selectedNoisePreset,
                presetTitle,
                description,
                copyPresetButton,
                removePresetButton
        ));

        noiseErrorMessages.add(configInvalidLabel);

        noisePresetOptions.addAll(Arrays.asList(
                presetTitle,
                description
        ));

        updateNonResponsiveOptions();
    }

    private static String formatLastException() {
        return NoisePresetConfig.lastException != null ? NoisePresetConfig.lastException.getMessage() : "";
    }

    private void setPresetDescription() {
        setPresetDescription(description, description.pendingValue().isBlank() ? "Description is empty" : description.pendingValue());
    }

    private void setPresetDescription(Option<String> descriptionOption, String newValue) {
        ((StringControllerDuck) descriptionOption.controller()).better_clouds$setDescription(
                OptionDescription.of(Component.literal(newValue)));
    }

    private void syncPresetOptions() {
        noiseBuilder.requestSet(config.noisePreset().octavesToStringList());
        updateNonResponsiveOptions();
    }

    private void updateNonResponsiveOptions() {
        noiseBuilder.setAvailable(config.noisePreset().editable);
        ((ListOptionDuck) noiseBuilder).better_clouds$setCollapsed(false);

        for (Option<?> option : noisePresetOptions) {
            option.forgetPendingValue();
            option.setAvailable(config.noisePreset().editable);
        }

        setPresetDescription();

        if (removePresetButton != null)
            removePresetButton.setAvailable(config.noisePreset().editable && config.noisePresets.size() > 1);
    }

    public void onSave() {
        for (NoisePresetConfig preset : presetsToBeDeleted) {
            config.noisePresets.remove(preset);
        }
    }
}
