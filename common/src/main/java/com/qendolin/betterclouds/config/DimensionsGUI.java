package com.qendolin.betterclouds.config;

import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.StateManager;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

import static com.qendolin.betterclouds.config.ConfigGUI.groupDescription;
import static com.qendolin.betterclouds.config.ConfigGUI.groupLabel;

public class DimensionsGUI {

    public final ListOption.Builder<String> compatDimensionsListGroup;

    private List<String> values;

    private final Config config;

    public DimensionsGUI(Config defaults, Config config) {
        this.config = config;

        values = config.enabledDimensions.stream().map(key -> key.identifier().toString()).toList();

        compatDimensionsListGroup = ListOption.<String>createBuilder()
            .name(groupLabel("compat.dimensions"))
            .description(OptionDescription.of(groupDescription("compat.dimensions")))
            // Instant doesn't really work, probably a YACL bug
            .state(StateManager.createInstant(
                defaults.enabledDimensions.stream().map(key -> key.identifier().toString()).toList(),
                () -> values,
                this::setValues
            ))
            .listener((option, value) -> {
                option.applyValue();
            })
            .controller(StringControllerBuilder::create)
            .initial(BuiltinDimensionTypes.OVERWORLD.identifier().toString());
    }

    private void setValues(List<String> values) {
        this.values = values;
        this.config.enabledDimensions = values.stream()
            .map(value -> {
                if (!value.contains(":")) return null;
                return Identifier.tryParse(value);
            })
            .filter(Objects::nonNull)
            .distinct()
            .map(value -> ResourceKey.create(Registries.DIMENSION_TYPE, value))
            .toList();
    }
}
