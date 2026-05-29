package com.qendolin.betterclouds.gui;

import com.qendolin.betterclouds.duck.TabNavigationWidgetExtensionDuck;
import com.qendolin.betterclouds.mixin.required.yacl.PopupControllerScreenAccessor;
import dev.isxander.yacl3.gui.controllers.PopupControllerScreen;
import dev.isxander.yacl3.gui.tab.ScrollableNavigationBar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;

public class CustomScrollableNavigationBar extends ScrollableNavigationBar {

    public final int width;

    public CustomScrollableNavigationBar(int width, TabManager tabManager, Iterable<? extends Tab> tabs) {
        super(width, tabManager, tabs);
        this.width = width;
        ((TabNavigationWidgetExtensionDuck) this).betterclouds$setRenderBackground(false);
    }

    public static boolean betterclouds$useTranslucentTheme(Minecraft client) {
        if (client.level == null) {
            return false;
        }
        if (client.gui.screen() instanceof ConfigScreen) {
            return true;
        }
        if (client.gui.screen() instanceof PopupControllerScreen popupScreen) {
            return ((PopupControllerScreenAccessor) popupScreen).getBackgroundYaclScreen() instanceof ConfigScreen;
        }
        return false;
    }
}
