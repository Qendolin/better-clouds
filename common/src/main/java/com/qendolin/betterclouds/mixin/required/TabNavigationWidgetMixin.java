package com.qendolin.betterclouds.mixin.required;

import com.qendolin.betterclouds.duck.TabNavigationWidgetExtensionDuck;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("MixinAnnotationTarget")
@Mixin(TabNavigationBar.class)
public class TabNavigationWidgetMixin implements TabNavigationWidgetExtensionDuck {
    @Unique
    private boolean renderBackground = true;

    @Override
    public void betterclouds$setRenderBackground(boolean renderBackground) {
        this.renderBackground = renderBackground;
    }
}
