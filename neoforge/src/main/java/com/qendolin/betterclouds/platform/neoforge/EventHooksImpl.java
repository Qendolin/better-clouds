package com.qendolin.betterclouds.platform.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import com.qendolin.betterclouds.config.ShaderPresetLoader;
import com.qendolin.betterclouds.platform.EventHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceReloader;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EventHooksImpl extends EventHooks {

    private final IEventBus modEventBus;

    public EventHooksImpl(IEventBus modEventBus) {
        super();
        this.modEventBus = modEventBus;
    }

    private static void invokeAddReloadListener(Object event, ResourceReloader reloader) {
        try {
            Method method = findMethod(event.getClass(), "addListener", 2);
            if (method != null) {
                method.invoke(event, ShaderPresetLoader.ID, reloader);
            }
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }
    }

    private static CommandDispatcher<?> invokeCommandDispatcher(Object event) {
        try {
            Method method = findMethod(event.getClass(), "getDispatcher", 0);
            if (method == null) {
                return null;
            }
            Object value = method.invoke(event);
            if (value instanceof CommandDispatcher<?> dispatcher) {
                return dispatcher;
            }
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }
        return null;
    }

    private static Method findMethod(Class<?> type, String name, int paramCount) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == paramCount) {
                return method;
            }
        }
        return null;
    }

    @Override
    public void onClientStarted(Consumer<MinecraftClient> callback) {
        modEventBus.addListener(FMLLoadCompleteEvent.class, event -> {
            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> callback.accept(client));
        });
    }

    @Override
    public void onWorldJoin(Consumer<MinecraftClient> callback) {
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingIn.class, event -> {
            callback.accept(MinecraftClient.getInstance());
        });
    }

    @Override
    public void onClientResourcesReload(Supplier<ResourceReloader> supplier) {
        modEventBus.addListener(AddClientReloadListenersEvent.class, event -> {
            invokeAddReloadListener(event, supplier.get());
        });
    }

    @Override
    public void onClientTick(Consumer<MinecraftClient> callback) {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> {
            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> callback.accept(client));
        });
    }

    @Override
    public void onClientCommandRegistration(Consumer<CommandDispatcher<?>> callback) {
        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, event -> {
            CommandDispatcher<?> dispatcher = invokeCommandDispatcher(event);
            if (dispatcher != null) {
                callback.accept(dispatcher);
            }
        });
    }
}
