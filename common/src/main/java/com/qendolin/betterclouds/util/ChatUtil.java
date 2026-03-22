package com.qendolin.betterclouds.util;

import com.qendolin.betterclouds.BetterCloudsStatic;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public interface ChatUtil {
    static String debugChatMessageKey(String id) {
        return BetterCloudsStatic.MODID + ".message." + id;
    }

    static void debugChatMessage(String id, Object... args) {
        debugChatMessage(Component.translatable(debugChatMessageKey(id), args));
    }

    static void debugChatMessage(Component message) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.level == null) return;
        client.gui.getChat().addClientSystemMessage(Component.literal("§e[§bBC§b§e]§r ").append(message));
    }
}
