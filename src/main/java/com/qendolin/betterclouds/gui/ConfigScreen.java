package com.qendolin.betterclouds.gui;

import dev.isxander.yacl3.api.PlaceholderCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.utils.OptionUtils;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.concurrent.atomic.AtomicBoolean;

public class ConfigScreen extends YACLScreen {

    public ConfigScreen(YetAnotherConfigLib config, Screen parent) {
        super(config, parent);
    }

    @Override
    protected void init() {
        assert client != null;
        tabNavigationBar = new CustomScrollableNavigationBar(this.width, tabManager, config.categories()
            .stream()
            .map(category -> {
                if (category instanceof PlaceholderCategory placeholder)
                    return new PlaceholderTab(placeholder);
                return new CustomCategoryTab(client, this, () -> tabArea, category);
            }).toList());
        tabNavigationBar.selectTab(0, false);
        tabNavigationBar.init();
        ScreenRect navBarArea = tabNavigationBar.getNavigationFocus();
        tabArea = new ScreenRect(0, navBarArea.height() - 1, this.width, this.height - navBarArea.height() + 1);
        tabManager.setTabArea(tabArea);
        addDrawableChild(tabNavigationBar);

        config.initConsumer().accept(this);
    }

    public boolean pendingChanges() {
        AtomicBoolean pendingChanges = new AtomicBoolean(false);
        OptionUtils.consumeOptions(config, (option) -> {
            if (option.changed()) {
                pendingChanges.set(true);
                return true;
            }
            return false;
        });

        return pendingChanges.get();
    }

    @Override
    public void cancelOrReset() {
        super.cancelOrReset();
    }

    @Override
    public void renderInGameBackground(DrawContext context) {
        context.fill(width / 3 * 2 + 1, tabArea.getTop(), width, tabArea.getBottom(), 0x6b000000);
    }

    // YACL incorrectly calls renderBackgroundTexture directly
    @Override
    public void renderBackgroundTexture(DrawContext context) {
        if (client == null || client.world == null) {
            super.renderBackgroundTexture(context);
        }
    }

    @Override
    protected void finishOrSave() {
        close();
    }

    @Override
    public void close() {
        config.saveFunction().run();
        super.close();
    }

    public static class HiddenScreen extends Screen {
        public HiddenScreen(Text title, ButtonWidget showButton) {
            super(title);
            addDrawableChild(showButton);
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return false;
        }

        @Override
        public void renderInGameBackground(DrawContext context) {
            // nothing
        }

        // YACL incorrectly calls renderBackgroundTexture directly
        @Override
        public void renderBackgroundTexture(DrawContext context) {
            if (client == null || client.world == null) {
                super.renderBackgroundTexture(context);
            }
        }
    }
}
