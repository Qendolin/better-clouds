package com.qendolin.betterclouds;

//? if fabric {
/*import net.fabricmc.api.ModInitializer;

public final class Entrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        Main.initializeClient();
    }
}*/
//?} elif neoforge {
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(Main.MODID)
public final class Entrypoint {
    public Entrypoint() {
        Main.initializeClient();

        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () ->
            (modContainer, parent) -> ConfigGUI.create(parent));
    }
}
//?}