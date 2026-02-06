package com.qendolin.betterclouds.gui;

import com.qendolin.betterclouds.duck.CustomCategoryTabDuck;
import com.qendolin.betterclouds.telemetry.IssueReportManager;
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
        this.tabArea = new ScreenRect(0, 24, this.width, this.height - 24);
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
        tabNavigationBar.init();
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
    public void tick() {
        try {
            super.tick();
        } catch (Exception e) {
            if (!IssueReportManager.handle(e, "An error occurred while processing the config screen: " + e.getMessage())) {
                throw e;
            }
            assert client != null;
            client.execute(() -> client.setScreen(IssueReportManager.popQueuedScreen()));
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (client == null || client.world == null) {
            this.renderPanoramaBackground(context, delta);
            this.applyBlur(context);
        }
        this.renderDarkening(context);
    }

    @Override
    public void renderInGameBackground(DrawContext context) {
        this.renderDarkening(context);
    }

    @Override
    protected void renderDarkening(DrawContext context) {
        if (tabArea == null) return;
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

        @Override
        public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
            // nothing
        }

        @Override
        public void renderInGameBackground(DrawContext context) {
            // nothing
        }

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
