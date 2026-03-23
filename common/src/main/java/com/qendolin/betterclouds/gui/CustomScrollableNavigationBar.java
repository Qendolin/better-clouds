package com.qendolin.betterclouds.gui;

import com.qendolin.betterclouds.duck.TabNavigationWidgetExtensionDuck;
import com.qendolin.betterclouds.mixin.required.yacl.PopupControllerScreenAccessor;
import dev.isxander.yacl3.gui.controllers.PopupControllerScreen;
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

    private static boolean betterclouds$useTranslucentTheme(Minecraft client) {
        if (client.level == null) {
            return false;
        }
        if (client.screen instanceof ConfigScreen) {
            return true;
        }
        if (client.screen instanceof PopupControllerScreen popupScreen) {
            return ((PopupControllerScreenAccessor) popupScreen).getBackgroundYaclScreen() instanceof ConfigScreen;
        }
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        Minecraft client = Minecraft.getInstance();
        if (betterclouds$useTranslucentTheme(client)) {
            context.fill(0, 0, this.width, 23, 0x6b000000);
            context.fill(0, 23, this.width, 24, 0xff000000);
        }
        super.extractRenderState(context, mouseX, mouseY, delta);
    }
}
