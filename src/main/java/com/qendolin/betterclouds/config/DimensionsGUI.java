package com.qendolin.betterclouds.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.gui.controllers.string.IStringController;
import dev.isxander.yacl3.impl.controller.AbstractControllerBuilderImpl;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;

import java.util.ArrayList;
import java.util.List;

import static com.qendolin.betterclouds.config.ConfigGUI.groupDescription;
import static com.qendolin.betterclouds.config.ConfigGUI.groupLabel;

public class DimensionsGUI {

    public final ListOption.Builder<String> compatDimensionsListGroup;

    private List<String> values;

    private final Config config;

    public DimensionsGUI(Config defaults, Config config) {
        this.config = config;

        values = config.enabledDimensions.stream().map(key -> key.getValue().toString()).toList();

        compatDimensionsListGroup = ListOption.<String>createBuilder()
            .name(groupLabel("compat.dimensions"))
            .description(OptionDescription.of(groupDescription("compat.dimensions")))
            // Instant doesn't really work, probably a YACL bug
            .state(StateManager.createInstant(
                defaults.enabledDimensions.stream().map(key -> key.getValue().toString()).toList(),
                () -> values,
                this::setValues
            ))
            .listener((option, value) -> {
                option.applyValue();
            })
            .controller(StringControllerBuilder::create)
            .initial(DimensionTypes.OVERWORLD.getValue().toString());
    }

    private void setValues(List<String> values) {
        this.values = values;
        this.config.enabledDimensions = values.stream()
            .filter(value -> {
                if (!value.contains(":")) return false;
                return Identifier.tryParse(value) != null;
            })
            .distinct()
            .map(value -> RegistryKey.of(RegistryKeys.DIMENSION_TYPE, Identifier.of(value)))
            .toList();
    }
}
