package com.qendolin.betterclouds.util;

import com.qendolin.betterclouds.BetterCloudsStatic;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public interface ChatUtil {
    static String debugChatMessageKey(String id) {
        return BetterCloudsStatic.MODID + ".message." + id;
    }

    static void debugChatMessage(String id, Object... args) {
        debugChatMessage(Text.translatable(debugChatMessageKey(id), args));
    }

    static void debugChatMessage(Text message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return;
        client.inGameHud.getChatHud().addMessage(Text.literal("§e[§bBC§b§e]§r ").append(message));
    }
}
