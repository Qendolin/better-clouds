package com.qendolin.betterclouds.gui;

import com.qendolin.betterclouds.duck.TabNavigationWidgetExtensionDuck;
import dev.isxander.yacl3.gui.tab.ScrollableNavigationBar;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tab.TabManager;

public class CustomScrollableNavigationBar extends ScrollableNavigationBar {

    private final int width;

    public CustomScrollableNavigationBar(int width, TabManager tabManager, Iterable<? extends Tab> tabs) {
        super(width, tabManager, tabs);
        this.width = width;
        ((TabNavigationWidgetExtensionDuck) this).betterclouds$setRenderBackground(false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.currentScreen instanceof ConfigScreen) {
            context.fill(0, 0, this.width, 23, 0x6b000000);
            context.fill(0, 23, this.width, 24, 0xff000000);
        }
        super.render(context, mouseX, mouseY, delta);
    }
}
