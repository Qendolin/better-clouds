package com.qendolin.betterclouds.mixin.required;

import com.qendolin.betterclouds.mixin.duck.TabNavigationWidgetExtensionDuck;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TabNavigationBar.class)
public class TabNavigationWidgetMixin implements TabNavigationWidgetExtensionDuck {
    @Unique
    private boolean betterclouds$renderBackground = true;

    @Override
    public void betterclouds$setRenderBackground(boolean renderBackground) {
        this.betterclouds$renderBackground = renderBackground;
    }
}
