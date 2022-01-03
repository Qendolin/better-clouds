package com.qendolin.betterclouds;

import com.qendolin.betterclouds.config.ConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import java.util.List;

public class ModMenuIntegration implements ModMenuApi {
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ConfigScreen<>(parent, Main.CONFIG, ModMenuIntegration::onClose);
    }

    private static void onClose(boolean save, Config config, List<ConfigScreen.EntryValueSetter<?>> valueSetters) {
        ConfigScreen.onCloseDefault(save, config, valueSetters);
        config.hasChanged = save;
    }
}
