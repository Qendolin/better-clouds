package com.qendolin.betterclouds.config;

import com.qendolin.betterclouds.compat.SereneSeasonsCompat;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.gui.controllers.slider.FloatSliderController;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static com.qendolin.betterclouds.config.ConfigGUI.*;

public class SereneSeasonsGUI {

    public final List<Option<?>> compatSereneSeasonsGroup = new ArrayList<>();

    public final LabelOption info;
    public final Option<Float> earlySpringCloudiness;
    public final Option<Float> midSpringCloudiness;
    public final Option<Float> lateSpringCloudiness;
    public final Option<Float> earlySummerCloudiness;
    public final Option<Float> midSummerCloudiness;
    public final Option<Float> lateSummerCloudiness;
    public final Option<Float> earlyAutumnCloudiness;
    public final Option<Float> midAutumnCloudiness;
    public final Option<Float> lateAutumnCloudiness;
    public final Option<Float> earlyWinterCloudiness;
    public final Option<Float> midWinterCloudiness;
    public final Option<Float> lateWinterCloudiness;

    public SereneSeasonsGUI(SereneSeasonsConfig defaults, SereneSeasonsConfig config) {
        info = LabelOption.create(optionLabel("sereneSeasons.info"));

        earlySpringCloudiness = createOption(float.class, "sereneSeasons.earlySpringCloudiness", false)
            .binding(defaults.earlySpringCloudiness, () -> config.earlySpringCloudiness, val -> config.earlySpringCloudiness = val)
            .customController(opt -> new FloatSliderController(opt, 0, 2, 0.1f, ConfigGUI::formatAsTimes))
            .build();
        midSpringCloudiness = createOption(float.class, "sereneSeasons.midSpringCloudiness", false)
            .binding(defaults.midSpringCloudiness, () -> config.midSpringCloudiness, val -> config.midSpringCloudiness = val)
            .customController(opt -> new FloatSliderController(opt, 0, 2, 0.1f, ConfigGUI::formatAsTimes))
            .build();
        lateSpringCloudiness = createOption(float.class, "sereneSeasons.lateSpringCloudiness", false)
            .binding(defaults.lateSpringCloudiness, () -> config.lateSpringCloudiness, val -> config.lateSpringCloudiness = val)
            .customController(opt -> new FloatSliderController(opt, 0, 2, 0.1f, ConfigGUI::formatAsTimes))
            .build();
        earlySummerCloudiness = createOption(float.class, "sereneSeasons.earlySummerCloudiness", false)
            .binding(defaults.earlySummerCloudiness, () -> config.earlySummerCloudiness, val -> config.earlySummerCloudiness = val)
            .customController(opt -> new FloatSliderController(opt, 0, 2, 0.1f, ConfigGUI::formatAsTimes))
            .build();
        midSummerCloudiness = createOption(float.class, "sereneSeasons.midSummerCloudiness", false)
            .binding(defaults.midSummerCloudiness, () -> config.midSummerCloudiness, val -> config.midSummerCloudiness = val)
            .customController(opt -> new FloatSliderController(opt, 0, 2, 0.1f, ConfigGUI::formatAsTimes))
            .build();
        lateSummerCloudiness = createOption(float.class, "sereneSeasons.lateSummerCloudiness", false)
            .binding(defaults.lateSummerCloudiness, () -> config.lateSummerCloudiness, val -> config.lateSummerCloudiness = val)
            .customController(opt -> new FloatSliderController(opt, 0, 2, 0.1f, ConfigGUI::formatAsTimes))
            .build();
        earlyAutumnCloudiness = createOption(float.class, "sereneSeasons.earlyAutumnCloudiness", false)
            .binding(defaults.earlyAutumnCloudiness, () -> config.earlyAutumnCloudiness, val -> config.earlyAutumnCloudiness = val)
            .customController(opt -> new FloatSliderController(opt, 0, 2, 0.1f, ConfigGUI::formatAsTimes))
            .build();
        midAutumnCloudiness = createOption(float.class, "sereneSeasons.midAutumnCloudiness", false)
            .binding(defaults.midAutumnCloudiness, () -> config.midAutumnCloudiness, val -> config.midAutumnCloudiness = val)
            .customController(opt -> new FloatSliderController(opt, 0, 2, 0.1f, ConfigGUI::formatAsTimes))
            .build();
        lateAutumnCloudiness = createOption(float.class, "sereneSeasons.lateAutumnCloudiness", false)
            .binding(defaults.lateAutumnCloudiness, () -> config.lateAutumnCloudiness, val -> config.lateAutumnCloudiness = val)
            .customController(opt -> new FloatSliderController(opt, 0, 2, 0.1f, ConfigGUI::formatAsTimes))
            .build();
        earlyWinterCloudiness = createOption(float.class, "sereneSeasons.earlyWinterCloudiness", false)
            .binding(defaults.earlyWinterCloudiness, () -> config.earlyWinterCloudiness, val -> config.earlyWinterCloudiness = val)
            .customController(opt -> new FloatSliderController(opt, 0, 2, 0.1f, ConfigGUI::formatAsTimes))
            .build();
        midWinterCloudiness = createOption(float.class, "sereneSeasons.midWinterCloudiness", false)
            .binding(defaults.midWinterCloudiness, () -> config.midWinterCloudiness, val -> config.midWinterCloudiness = val)
            .customController(opt -> new FloatSliderController(opt, 0, 2, 0.1f, ConfigGUI::formatAsTimes))
            .build();
        lateWinterCloudiness = createOption(float.class, "sereneSeasons.lateWinterCloudiness", false)
            .binding(defaults.lateWinterCloudiness, () -> config.lateWinterCloudiness, val -> config.lateWinterCloudiness = val)
            .customController(opt -> new FloatSliderController(opt, 0, 2, 0.1f, ConfigGUI::formatAsTimes))
            .build();

        compatSereneSeasonsGroup.add(info);

        var options = List.of(
            earlySpringCloudiness,
            midSpringCloudiness,
            lateSpringCloudiness,
            earlySummerCloudiness,
            midSummerCloudiness,
            lateSummerCloudiness,
            earlyAutumnCloudiness,
            midAutumnCloudiness,
            lateAutumnCloudiness,
            earlyWinterCloudiness,
            midWinterCloudiness,
            lateWinterCloudiness
        );
        compatSereneSeasonsGroup.addAll(options);
        for (var opt : options) {
            opt.setAvailable(SereneSeasonsCompat.IS_LOADED);
        }


    }

}
