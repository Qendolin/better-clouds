package com.qendolin.betterclouds.gui;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.DescriptionWithName;
import dev.isxander.yacl3.gui.OptionListWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.LabelController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class CustomOptionListWidget extends OptionListWidget {

    public CustomOptionListWidget(YACLScreen screen, ConfigCategory category, MinecraftClient client, int x, int y, int width, int height, Consumer<DescriptionWithName> hoverEvent) {
        super(screen, category, client, x, y, width, height, hoverEvent);
    }

    @Override
    public void refreshOptions() {
        super.refreshOptions();
        addEntry(new PaddingEntry());
        for (dev.isxander.yacl3.gui.OptionListWidget.Entry child : children()) {
            if (child instanceof OptionEntry optionEntry && optionEntry.option.controller() instanceof LabelController) {
                addEntryBelow(optionEntry, new ProxyEntry<>(optionEntry)
                    .onBeforeRender((delegate, context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta) -> {
                        if (client.world == null) return;
                        Dimension<Integer> dim = delegate.widget.getDimension();
                        context.fill(dim.x(), dim.y(), dim.xLimit(), dim.yLimit(), 0x6b000000);
                    }));
                removeEntry(optionEntry);
            } else if (child instanceof GroupSeparatorEntry groupSeparatorEntry) {
                addEntryBelow(groupSeparatorEntry, new ProxyEntry<>(groupSeparatorEntry)
                    .onBeforeRender((delegate, context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta) -> {
                        if (client.world == null) return;
                        context.fill(x, y, x + entryWidth, y + 19, 0x6b000000);
                    }));
                removeEntry(groupSeparatorEntry);
            }
        }

        recacheViewableChildren();
        setScrollAmount(0);
        resetSmoothScrolling();
    }

    @Override
    public void renderWidget(DrawContext graphics, int mouseX, int mouseY, float delta) {
        super.renderWidget(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        for (dev.isxander.yacl3.gui.OptionListWidget.Entry child : children()) {
            if (child.mouseScrolled(mouseX, mouseY, horizontal, vertical)) {
                return true;
            }
        }

        this.setScrollAmount(this.getScrollAmount() - (vertical + horizontal) * 20);
        return true;
    }

    // It is super annoying that Entry is not declared as a static class
    public class ProxyEntry<T extends dev.isxander.yacl3.gui.OptionListWidget.Entry> extends dev.isxander.yacl3.gui.OptionListWidget.Entry {
        private final T delegate;

        public BeforeRenderCallback<T> beforeRender;
        public AfterRenderCallback<T> afterRender;

        public ProxyEntry(T delegate) {
            super();
            this.delegate = delegate;
        }

        public ProxyEntry<T> onBeforeRender(BeforeRenderCallback<T> callback) {
            this.beforeRender = callback;
            return this;
        }

        public ProxyEntry<T> onAfterRender(AfterRenderCallback<T> callback) {
            this.afterRender = callback;
            return this;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if (beforeRender != null)
                beforeRender.onBeforeRender(delegate, context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
            delegate.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
            if (afterRender != null)
                afterRender.onAfterRender(delegate, context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return delegate.selectableChildren();
        }

        @Override
        public List<? extends Element> children() {
            return delegate.children();
        }

        @Override
        public Optional<Element> hoveredElement(double mouseX, double mouseY) {
            return delegate.hoveredElement(mouseX, mouseY);
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            delegate.mouseMoved(mouseX, mouseY);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return delegate.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            return delegate.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return delegate.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            return delegate.keyReleased(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char chr, int modifiers) {
            return delegate.charTyped(chr, modifiers);
        }

        @Nullable
        @Override
        public GuiNavigationPath getFocusedPath() {
            return delegate.getFocusedPath();
        }

        @Override
        public ScreenRect getNavigationFocus() {
            return delegate.getNavigationFocus();
        }

        @Override
        public boolean isViewable() {
            return delegate.isViewable();
        }

        @Override
        public boolean isHovered() {
            return Objects.equals(getHoveredEntry(), this);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return delegate.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            return delegate.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean isDragging() {
            return delegate.isDragging();
        }

        @Override
        public void setDragging(boolean dragging) {
            delegate.setDragging(dragging);
        }

        @Override
        @Nullable
        public Element getFocused() {
            return delegate.getFocused();
        }

        @Override
        @Nullable
        public GuiNavigationPath getNavigationPath(GuiNavigation navigation, int index) {
            return delegate.getNavigationPath(navigation, index);
        }

        @Override
        @Nullable
        public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
            return delegate.getNavigationPath(navigation);
        }

        @Override
        public boolean isFocused() {
            return delegate.isFocused();
        }

        @Override
        public void setFocused(@Nullable Element focused) {
            delegate.setFocused(focused);
        }

        @Override
        public void setFocused(boolean focused) {
            delegate.setFocused(focused);
        }

        @Override
        public void drawBorder(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            delegate.drawBorder(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return delegate.isMouseOver(mouseX, mouseY);
        }

        @Override
        public int getItemHeight() {
            return delegate.getItemHeight();
        }

        @Override
        public int getNavigationOrder() {
            return delegate.getNavigationOrder();
        }
    }

    @FunctionalInterface
    public interface BeforeRenderCallback<T extends dev.isxander.yacl3.gui.OptionListWidget.Entry> {
        void onBeforeRender(T delegate, DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta);
    }

    @FunctionalInterface
    public interface AfterRenderCallback<T extends dev.isxander.yacl3.gui.OptionListWidget.Entry> {
        void onAfterRender(T delegate, DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta);
    }

    private class PaddingEntry extends dev.isxander.yacl3.gui.OptionListWidget.Entry {
        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of();
        }

        @Override
        public List<? extends Element> children() {
            return List.of();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        }

        @Override
        public int getItemHeight() {
            return 5;
        }
    }
}
