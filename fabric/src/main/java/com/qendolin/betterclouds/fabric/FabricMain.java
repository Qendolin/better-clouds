package com.qendolin.betterclouds.fabric;

import com.qendolin.betterclouds.Main;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class FabricMain implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Main.initialize();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> Commands.register(dispatcher));
    }
}

