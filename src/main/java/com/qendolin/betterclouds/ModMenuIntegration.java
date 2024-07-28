package com.qendolin.betterclouds;

//? if fabric {
/*import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {

    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigGUI::create;
    }

}*/
//?} else {
public abstract class ModMenuIntegration {
}
//?}