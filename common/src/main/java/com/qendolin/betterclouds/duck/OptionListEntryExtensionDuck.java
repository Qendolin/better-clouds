package com.qendolin.betterclouds.duck;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ElementListWidget;

public interface OptionListEntryExtensionDuck {
    @FunctionalInterface
    interface BeforeRenderCallback {
        void onBeforeRender(ElementListWidget.Entry<?> self, DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, boolean hovered, float tickDelta);
    }

    @FunctionalInterface
    interface AfterRenderCallback {
        void onAfterRender(ElementListWidget.Entry<?> self, DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, boolean hovered, float tickDelta);
    }

    void betterclouds$onBeforeRender(BeforeRenderCallback callback);

    void betterclouds$onAfterRender(AfterRenderCallback callback);

    default void betterclouds$setYPadding(int padding) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default int betterclouds$getYPadding() { return 0; }
}
