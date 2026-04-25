package com.qendolin.betterclouds.duck;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

public interface OptionListEntryExtensionDuck {
    void betterclouds$onBeforeRender(BeforeRenderCallback callback);

    void betterclouds$onAfterRender(AfterRenderCallback callback);

    default void betterclouds$setYPadding(int padding) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default int betterclouds$getYPadding() {
        return 0;
    }

    default void betterclouds$setExpanded(boolean expanded) {
    }

    @FunctionalInterface
    interface BeforeRenderCallback {
        void onBeforeRender(ContainerObjectSelectionList.Entry<?> self, GuiGraphicsExtractor context, int x, int y, int width, int height, int mouseX, int mouseY, boolean hovered, float tickDelta);
    }

    @FunctionalInterface
    interface AfterRenderCallback {
        void onAfterRender(ContainerObjectSelectionList.Entry<?> self, GuiGraphicsExtractor context, int x, int y, int width, int height, int mouseX, int mouseY, boolean hovered, float tickDelta);
    }
}
