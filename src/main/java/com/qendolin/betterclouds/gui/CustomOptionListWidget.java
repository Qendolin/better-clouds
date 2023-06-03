package com.qendolin.betterclouds.gui;

import dev.isxander.yacl.api.utils.Dimension;
import dev.isxander.yacl.gui.OptionListWidget;
import dev.isxander.yacl.gui.YACLScreen;
import dev.isxander.yacl.gui.controllers.LabelController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CustomOptionListWidget extends OptionListWidget {
    public CustomOptionListWidget(YACLScreen screen, MinecraftClient client, int width, int height) {
        super(screen, client, width, height);
    }

    @Override
    public void refreshOptions() {
        super.refreshOptions();
        addEntry(new PaddingEntry());
        for (Entry child : children()) {
            if(child instanceof OptionEntry optionEntry && optionEntry.option.controller() instanceof LabelController) {
                addEntryBelow(optionEntry, new ProxyEntry<OptionEntry>(optionEntry)
                    .onBeforeRender((delegate, matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta) -> {
                        if(client.world == null) return;
                        Dimension<Integer> dim = delegate.widget.getDimension();
                        DrawableHelper.fill(matrices, dim.x(), dim.y(), dim.xLimit(), dim.yLimit(), 0x6b000000);
                    }));
                removeEntry(optionEntry);
            } else if(child instanceof GroupSeparatorEntry groupSeparatorEntry) {
                addEntryBelow(groupSeparatorEntry, new ProxyEntry<GroupSeparatorEntry>(groupSeparatorEntry)
                    .onBeforeRender((delegate, matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta) -> {
                        if(client.world == null) return;
                        DrawableHelper.fill(matrices, x, y, x+entryWidth, y+19, 0x6b000000);
                    }));
                removeEntry(groupSeparatorEntry);
            }
        }

        recacheViewableChildren();
        setScrollAmount(0);
        resetSmoothScrolling();
    }

    @Override
    protected void renderBackground(MatrixStack matrices) {
        if (client == null || client.world == null) {
            super.renderBackground(matrices);
            setRenderBackground(true);
        } else {
            setRenderBackground(false);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        for (dev.isxander.yacl.gui.OptionListWidget.Entry child : children()) {
            if (child.mouseScrolled(mouseX, mouseY, amount)) {
                return true;
            }
        }

        this.setScrollAmount(this.getScrollAmount() - amount * 20);
        return true;
    }

    // It is super annoying that Entry is not declared as a static class
    public class ProxyEntry<T extends Entry> extends Entry {
        private final T delegate;

        public BeforeRenderCallback<T> beforeRender;
        public AfterRenderCallback<T> afterRender;
        public BeforePostRenderCallback<T> beforePostRender;
        public AfterPostRenderCallback<T> afterPostRender;

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

        public ProxyEntry<T> onBeforePostRender(BeforePostRenderCallback<T> callback) {
            this.beforePostRender = callback;
            return this;
        }

        public ProxyEntry<T> onAfterPostRender(AfterPostRenderCallback<T> callback) {
            this.afterPostRender = callback;
            return this;
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if(beforeRender != null) beforeRender.onBeforeRender(delegate, matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
            delegate.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
            if(afterRender != null) afterRender.onAfterRender(delegate, matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
        }

        @Override
        public void postRender(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            if(beforePostRender != null) beforePostRender.onBeforePostRender(delegate, matrices, mouseX, mouseY, delta);
            delegate.postRender(matrices, mouseX, mouseY, delta);
            if(afterPostRender != null) afterPostRender.onAfterPostRender(delegate, matrices, mouseX, mouseY, delta);
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
        public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
            return delegate.mouseScrolled(mouseX, mouseY, amount);
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
        public void focusOn(@Nullable Element element) {
            delegate.focusOn(element);
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
        public void setFocused(@Nullable Element focused) {
            delegate.setFocused(focused);
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
        public void setFocused(boolean focused) {
            delegate.setFocused(focused);
        }

        @Override
        public boolean isFocused() {
            return delegate.isFocused();
        }

        @Override
        public void drawBorder(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            delegate.drawBorder(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return delegate.isMouseOver(mouseX, mouseY);
        }

        @Override
        public int getItemHeight() {
            return delegate.getItemHeight();
        }
    }

    @FunctionalInterface
    public interface BeforeRenderCallback<T extends Entry> {
        void onBeforeRender(T delegate, MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta);
    }

    @FunctionalInterface
    public interface AfterRenderCallback<T extends Entry> {
        void onAfterRender(T delegate, MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta);
    }

    @FunctionalInterface
    public interface BeforePostRenderCallback<T extends Entry> {
        void onBeforePostRender(T delegate, MatrixStack matrices, int mouseX, int mouseY, float delta);
    }

    @FunctionalInterface
    public interface AfterPostRenderCallback<T extends Entry> {
        void onAfterPostRender(T delegate, MatrixStack matrices, int mouseX, int mouseY, float delta);
    }

    private class PaddingEntry extends Entry {
        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of();
        }

        @Override
        public List<? extends Element> children() {
            return List.of();
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {}

        @Override
        public int getItemHeight() {
            return 5;
        }
    }
}
