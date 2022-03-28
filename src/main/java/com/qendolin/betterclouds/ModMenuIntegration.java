package com.qendolin.betterclouds;

import com.qendolin.betterclouds.config.ConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import java.util.List;

public class ModMenuIntegration implements ModMenuApi {
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ConfigScreen<>(parent, Main.CONFIG)
                .onClose(ModMenuIntegration::onClose)
                .onChange(ModMenuIntegration::onChange);
    }

    private static void onClose(boolean save, Config config, List<ConfigScreen.EntryValueSetter<?>> valueSetters) {
        ConfigScreen.onCloseDefault(save, config, valueSetters);
        config.hasChanged = true;
    }

    private static void onChange(Config config, String setting, ConfigScreen.EntryValueSetter<?> setter, Object prev, Object curr) {
        config.hasChanged = true;
        setter.apply(config);
    }
}
