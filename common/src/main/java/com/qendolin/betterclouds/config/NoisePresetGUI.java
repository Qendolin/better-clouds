package com.qendolin.betterclouds.config;

import com.qendolin.betterclouds.gui.CustomButtonOption;
import com.qendolin.betterclouds.gui.SelectDropdownController;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
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
    public final ButtonOption copyPresetButton;
    public final ButtonOption removePresetButton;

    public final List<Tuple<OptionGroup.Builder, List<Option<?>>>> noiseCategory = new ArrayList<>();

    public final List<Option<?>> noisePresetGroup = new ArrayList<>();
    private final Config config;
    private final List<NoisePresetConfig> presetsToBeDeleted = new ArrayList<>();

    public NoisePresetGUI(Config defaults, Config config) {
        this.config = config;
        config.addFirstNoisePreset();
        config.sortNoisePresets();

        this.selectedNoisePreset = createOption(int.class, "noisePreset")
                .binding(defaults.selectedNoisePreset, () -> config.selectedNoisePreset, val -> config.selectedNoisePreset = val)
                .customController(opt -> new SelectDropdownController<>(opt, config.noisePresets, (_, preset) -> {
                    if (preset.title.isBlank()) {
                        return Component.translatable(LANG_KEY_PREFIX + ".entry.noisePreset.untitled")
                                .withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(true));
                    } else if (!preset.editable) {
                        return Component.literal(preset.title)
                                .withStyle(style -> style.withItalic(true));
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

        this.noiseBuilder = ListOption.<String>createBuilder()
                .name(groupLabel("noise.builder"))
                .description(OptionDescription.of(groupDescription("noise.builder")))
                .state(StateManager.createInstant(
                        defaults.noisePreset().octavesToStringList(),
                        () -> config.noisePreset().octavesToStringList(),
                        l -> config.noisePreset().octavesFromStringList(l)
                ))
                .listener((option, _) -> option.applyValue())
                .controller(StringControllerBuilder::create)    // todo: move cursed string manipulation into custom controller
                .initial("")
                .build();

        noiseBuilder.setAvailable(config.noisePreset().editable);

        final Component removeButtonRemoveText = Component.translatable(LANG_KEY_PREFIX + ".entry.shaderPreset.remove");
        final Component removeButtonRestoreText = Component.translatable(LANG_KEY_PREFIX + ".entry.shaderPreset.restore");

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
        updateNonResponsiveOptions();
        this.copyPresetButton = CustomButtonOption.createBuilder()
                .name(() -> Component.translatable(LANG_KEY_PREFIX + ".entry.shaderPreset.copy"))
                .action((_, _) -> {
                    NoisePresetConfig preset = new NoisePresetConfig(config.noisePreset());
                    preset.title = Component.translatable(LANG_KEY_PREFIX + ".entry.shaderPreset.copyOf", config.noisePreset().title).getString();
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

        noisePresetGroup.addAll(Arrays.asList(
                selectedNoisePreset,
                copyPresetButton,
                removePresetButton
        ));
    }

    private void syncPresetOptions() {
        if (noiseBuilder == null) {
            updateNonResponsiveOptions();
            return;
        }
        noiseBuilder.requestSet(config.noisePreset().octavesToStringList());
        noiseBuilder.setAvailable(config.noisePreset().editable);
        updateNonResponsiveOptions();
    }

    private void updateNonResponsiveOptions() {
        if (removePresetButton != null) {
            removePresetButton.setAvailable(config.noisePreset().editable && config.noisePresets.size() > 1);
        }
    }

    public void onSave() {
        for (NoisePresetConfig preset : presetsToBeDeleted) {
            config.noisePresets.remove(preset);
        }
    }
}
