package com.qendolin.betterclouds.gui;

import com.qendolin.betterclouds.duck.CustomCategoryTabDuck;
import com.qendolin.betterclouds.telemetry.IssueReportManager;
import dev.isxander.yacl3.api.PlaceholderCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.utils.OptionUtils;
import dev.isxander.yacl3.gui.YACLScreen;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends YACLScreen {

    public ConfigScreen(YetAnotherConfigLib config, Screen parent) {
        super(config, parent);
    }

    @Override
    protected void init() {
        assert minecraft != null;
        this.tabArea = new ScreenRectangle(0, 24, this.width, this.height - 24);
        int currentTab = this.tabNavigationBar != null ? this.tabNavigationBar.getTabs().indexOf(this.tabManager.getCurrentTab()) : 0;
        if (currentTab == -1) {
            currentTab = 0;
        }
        tabNavigationBar = new CustomScrollableNavigationBar(this.width, tabManager, config.categories()
            .stream()
            .map(category -> {
                if (category instanceof PlaceholderCategory placeholder)
                    return new PlaceholderTab(placeholder, this);
                var tab = new CategoryTab(this, category, tabArea);
                ((CustomCategoryTabDuck) tab).betterclouds$applyOverride();
                return tab;
            }).toList());
        tabNavigationBar.selectTab(currentTab, false);
        tabNavigationBar.arrangeElements();
        tabManager.setTabArea(tabArea);
        addRenderableWidget(tabNavigationBar);

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
    public void tick() {
        try {
            super.tick();
        } catch (Exception e) {
            if (!IssueReportManager.handle(e, "An error occurred while processing the config screen: " + e.getMessage())) {
                throw e;
            }
            assert minecraft != null;
            minecraft.execute(() -> minecraft.setScreen(IssueReportManager.popQueuedScreen()));
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        if (minecraft == null || minecraft.level == null) {
            this.extractPanorama(context, delta);
            this.extractBlurredBackground(context);
        }
        this.extractMenuBackground(context);
    }

    @Override
    public void extractTransparentBackground(GuiGraphicsExtractor context) {
        this.extractMenuBackground(context);
    }

    @Override
    protected void extractMenuBackground(GuiGraphicsExtractor context) {
        if (tabArea == null) return;
        context.fill(width / 3 * 2 + 1, tabArea.top(), width, tabArea.bottom(), 0x6b000000);
    }

    @Override
    public void finishOrSave() {
        onClose();
    }

    @Override
    public void onClose() {
        config.saveFunction().run();
        super.onClose();
    }

    public static class HiddenScreen extends Screen {
        private final Button showButton;

        public HiddenScreen(Component title, Button showButton) {
            super(title);
            this.showButton = showButton;
            addRenderableWidget(showButton);
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return false;
        }

        @Override
        public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
            // nothing
        }

        @Override
        public void extractTransparentBackground(GuiGraphicsExtractor context) {
            // nothing
        }

        @Override
        protected void extractMenuBackground(GuiGraphicsExtractor context) {
            // nothing
        }

        @Nullable
        @Override
        public GuiEventListener getFocused() {
            return showButton;
        }
    }
}
