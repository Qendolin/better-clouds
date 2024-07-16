package com.qendolin.betterclouds.gui;

import dev.isxander.yacl3.api.PlaceholderCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.utils.OptionUtils;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

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
                    return new PlaceholderTab(placeholder, this);
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
    //? if >1.20.1 {
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    //?} else {
    /*public void renderBackground(DrawContext context) {
    *///?}
    //? if >=1.21 {
        if (client == null || client.world == null) {
            this.renderPanoramaBackground(context, delta);
            this.applyBlur(delta);
        }
    //?} else {
        /*if (client == null || client.world == null) {
            //? if >1.20.1 {
            /^super.renderBackground(context, mouseX, mouseY, delta);
            ^///?} else {
            super.renderBackground(context);
            //?}
        }
    *///?}
        this.renderDarkening(context);
    }

    //? if <1.21 {
    /*@Override
    public void renderBackgroundTexture(DrawContext context) {
        if (client == null || client.world == null) {
            super.renderBackgroundTexture(context);
        }
        //? if <1.20.2
        renderDarkening(context);
    }
    *///?}

    //? if >=1.21
    @Override
    public void renderInGameBackground(DrawContext context) {
        this.renderDarkening(context);
    }

    //? if >=1.21
    @Override
    protected void renderDarkening(DrawContext context) {
        context.fill(width / 3 * 2 + 1, tabArea.getTop(), width, tabArea.getBottom(), 0x6b000000);
    }

    @Override
    public void finishOrSave() {
        close();
    }

    @Override
    public void close() {
        config.saveFunction().run();
        super.close();
    }

    public static class HiddenScreen extends Screen {
        private final ButtonWidget showButton;

        public HiddenScreen(Text title, ButtonWidget showButton) {
            super(title);
            this.showButton = showButton;
            addDrawableChild(showButton);
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return false;
        }

        //? if >=1.21
        @Override
        public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
            // nothing
        }

        //? if >=1.21
        @Override
        public void renderInGameBackground(DrawContext context) {
            // nothing
        }

        //? if >=1.21
        @Override
        protected void renderDarkening(DrawContext context) {
            // nothing
        }

        @Nullable
        @Override
        public Element getFocused() {
            return showButton;
        }
    }
}