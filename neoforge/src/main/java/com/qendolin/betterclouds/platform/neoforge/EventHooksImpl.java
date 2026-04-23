package com.qendolin.betterclouds.platform.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import com.qendolin.betterclouds.config.PresetLoader;
import com.qendolin.betterclouds.platform.EventHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;
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

    private static void invokeAddReloadListener(Object event, PreparableReloadListener reloader) {
        try {
            Method method = findMethod(event.getClass(), "addListener", 2);
            if (method != null) {
                for (PresetLoader<?> loader : PresetLoader.ALL_PRESETS) {
                    method.invoke(event, loader.id, reloader);
                }
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
    public void onClientStarted(Consumer<Minecraft> callback) {
        modEventBus.addListener(FMLLoadCompleteEvent.class, _ -> {
            Minecraft client = Minecraft.getInstance();
            client.execute(() -> callback.accept(client));
        });
    }

    @Override
    public void onWorldJoin(Consumer<Minecraft> callback) {
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingIn.class, _ -> callback.accept(Minecraft.getInstance()));
    }

    @Override
    public void onClientResourcesReload(Supplier<PreparableReloadListener> supplier) {
        modEventBus.addListener(AddClientReloadListenersEvent.class, event -> invokeAddReloadListener(event, supplier.get()));
    }

    @Override
    public void onClientTick(Consumer<Minecraft> callback) {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, _ -> {
            Minecraft client = Minecraft.getInstance();
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
