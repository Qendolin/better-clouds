package com.qendolin.betterclouds.platform.fabric;

import com.qendolin.betterclouds.platform.EventHooks;

//? if fabric {
/*import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class EventHooksImpl extends EventHooks {
    @Override
    public void onClientStarted(Consumer<MinecraftClient> callback) {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> callback.accept(client));
    }

    @Override
    public void onWorldJoin(Consumer<MinecraftClient> callback) {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> callback.accept(client));
    }

    @Override
    public void onClientResourcesReload(Supplier<ResourceReloader> supplier) {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
            .registerReloadListener(supplier.get());
    }

    @Override
    public void onClientCommandRegistration(Consumer<CommandDispatcher<? extends CommandSource>> callback) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> callback.accept(dispatcher));
    }
}*/
//?} else {
public abstract class EventHooksImpl extends EventHooks {
}
//?}

