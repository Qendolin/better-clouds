package com.qendolin.betterclouds.gui;

import com.qendolin.betterclouds.mixin.TabNavigationWidgetAccessor;
import dev.isxander.yacl3.gui.tab.ScrollableNavigationBar;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.TabButtonWidget;

public class CustomScrollableNavigationBar extends ScrollableNavigationBar {

    private final int width;

    public CustomScrollableNavigationBar(int width, TabManager tabManager, Iterable<? extends Tab> tabs) {
        super(width, tabManager, tabs);
        this.width = width;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null || !(client.currentScreen instanceof ConfigScreen)) {
            super.render(context, mouseX, mouseY, delta);
        } else {
            context.fill(0, 0, this.width, 22, 0x6b000000);
            context.fill(0, 22, this.width, 23, 0xff000000);
            for (TabButtonWidget tabButtonWidget : ((TabNavigationWidgetAccessor) this).getTabButtons()) {
                tabButtonWidget.render(context, mouseX, mouseY, delta);
            }
        }
    }
}
