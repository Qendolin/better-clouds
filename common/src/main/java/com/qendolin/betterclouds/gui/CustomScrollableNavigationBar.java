package com.qendolin.betterclouds.gui;

import com.qendolin.betterclouds.duck.TabNavigationWidgetExtensionDuck;
import dev.isxander.yacl3.gui.tab.ScrollableNavigationBar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;

public class CustomScrollableNavigationBar extends ScrollableNavigationBar {

    private final int width;

    public CustomScrollableNavigationBar(int width, TabManager tabManager, Iterable<? extends Tab> tabs) {
        super(width, tabManager, tabs);
        this.width = width;
        ((TabNavigationWidgetExtensionDuck) this).betterclouds$setRenderBackground(false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.screen instanceof ConfigScreen) {
            context.fill(0, 0, this.width, 23, 0x6b000000);
            context.fill(0, 23, this.width, 24, 0xff000000);
        }
        super.extractRenderState(context, mouseX, mouseY, delta);
    }
}
