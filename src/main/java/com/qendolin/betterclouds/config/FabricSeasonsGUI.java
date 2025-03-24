package com.qendolin.betterclouds.config;

import com.qendolin.betterclouds.compat.FabricSeasonsCompat;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.gui.controllers.slider.FloatSliderController;
import dev.isxander.yacl3.gui.controllers.string.number.FloatFieldController;

import java.util.ArrayList;
import java.util.List;

import static com.qendolin.betterclouds.config.ConfigGUI.createOption;
import static com.qendolin.betterclouds.config.ConfigGUI.optionLabel;

public class FabricSeasonsGUI {

    public final List<Option<?>> compatFabricSeasonsGroup = new ArrayList<>();

    public final LabelOption info;
    public final Option<Float> transitionDays;
    public final Option<Float> springCloudiness;
    public final Option<Float> summerCloudiness;
    public final Option<Float> fallCloudiness;
    public final Option<Float> winterCloudiness;

    public FabricSeasonsGUI(FabricSeasonsConfig defaults, FabricSeasonsConfig config) {
        info = LabelOption.create(optionLabel("fabricSeasons.info"));

        transitionDays = createOption(float.class, "fabricSeasons.transitionDays", true)
            .binding(defaults.transitionDays, () -> config.transitionDays, val -> config.transitionDays = val)
            .customController(opt -> new FloatFieldController(opt, 0, Float.MAX_VALUE, ConfigGUI::formatAsDays))
            .build();

        springCloudiness = createOption(float.class, "fabricSeasons.springCloudiness", false)
            .binding(defaults.springCloudiness, () -> config.springCloudiness, val -> config.springCloudiness = val)
            .customController(opt -> new FloatSliderController(opt, 0, 2, 0.1f, ConfigGUI::formatAsTimes))
            .build();
        summerCloudiness = createOption(float.class, "fabricSeasons.summerCloudiness", false)
            .binding(defaults.summerCloudiness, () -> config.summerCloudiness, val -> config.summerCloudiness = val)
            .customController(opt -> new FloatSliderController(opt, 0, 2, 0.1f, ConfigGUI::formatAsTimes))
            .build();
        fallCloudiness = createOption(float.class, "fabricSeasons.fallCloudiness", false)
            .binding(defaults.fallCloudiness, () -> config.fallCloudiness, val -> config.fallCloudiness = val)
            .customController(opt -> new FloatSliderController(opt, 0, 2, 0.1f, ConfigGUI::formatAsTimes))
            .build();
        winterCloudiness = createOption(float.class, "fabricSeasons.winterCloudiness", false)
            .binding(defaults.winterCloudiness, () -> config.winterCloudiness, val -> config.winterCloudiness = val)
            .customController(opt -> new FloatSliderController(opt, 0, 2, 0.1f, ConfigGUI::formatAsTimes))
            .build();

        compatFabricSeasonsGroup.add(info);

        var options = List.of(
            transitionDays,
            springCloudiness,
            summerCloudiness,
            fallCloudiness,
            winterCloudiness
        );
        compatFabricSeasonsGroup.addAll(options);
        for (var opt : options) {
            opt.setAvailable(FabricSeasonsCompat.isLoaded());
        }


    }

}
