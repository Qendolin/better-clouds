package com.qendolin.betterclouds;

import com.qendolin.betterclouds.config.gui.ConfigGUI;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {

    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigGUI::create;
    }

}
