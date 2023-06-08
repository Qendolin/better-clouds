package com.qendolin.betterclouds.gui;

import com.qendolin.betterclouds.ConfigGUI;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.gui.TooltipButtonWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ConfigScreen extends YACLScreen {

    public TooltipButtonWidget hideShowButton;
    protected Screen hiddenScreen;
    protected boolean hidden;

    public ConfigScreen(YetAnotherConfigLib config, Screen parent) {
        super(config, parent);
    }

    @Override
    protected void init() {
        super.init();
        // TODO:
//        remove(undoButton);
//
//        hideShowButton = new TooltipButtonWidget(
//            this,
//            undoButton.getX(),
//            undoButton.getY(),
//            undoButton.getWidth(),
//            undoButton.getHeight(),
//            Text.translatable(ConfigGUI.LANG_KEY_PREFIX + ".hide"),
//            Text.empty(),
//            btn -> setHidden(!hidden)
//        );
//        addDrawableChild(hideShowButton);
//        hideShowButton.active = client != null && client.world != null;
//
//        cancelResetButton.active = false;
//
//        remove(optionList);
//        optionList = new CustomOptionListWidget(this, client, width, height);
//        addSelectableChild(optionList);

        hiddenScreen = new HiddenScreen(title, hideShowButton);
    }

    public void setHidden(boolean hidden) {
        assert client != null;
        this.hidden = hidden;
        if (hidden) {
            hideShowButton.setMessage(Text.translatable(ConfigGUI.LANG_KEY_PREFIX + ".show"));
            client.setScreen(hiddenScreen);
        } else {
            hideShowButton.setMessage(Text.translatable(ConfigGUI.LANG_KEY_PREFIX + ".hide"));
            client.setScreen(this);
        }
    }

    @Override
    public void tick() {
        super.tick();

        // TODO:
//        if (Screen.hasShiftDown()) {
//            cancelResetButton.active = true;
//            cancelResetButton.setTooltip(Text.translatable(ConfigGUI.LANG_KEY_PREFIX + ".reset.tooltip"));
//        } else {
//            cancelResetButton.active = false;
//            cancelResetButton.setTooltip(Text.translatable(ConfigGUI.LANG_KEY_PREFIX + ".reset.tooltip.holdShift"));
//        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (hidden) {
            hideShowButton.render(context, mouseX, mouseY, delta);
            // TODO:
//            hideShowButton.renderHoveredTooltip(context);
            return;
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context) {
        if (client == null || client.world == null) {
            super.renderBackground(context);
        } else {
            context.fill(0, 0, width / 3, height, 0x6b000000);
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

    private static class HiddenScreen extends Screen {
        public HiddenScreen(Text title, ButtonWidget showButton) {
            super(title);
            addDrawableChild(showButton);
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return false;
        }

        @Override
        public void renderBackground(DrawContext context) {
            if (client == null || client.world == null) {
                super.renderBackground(context);
            } else {
                context.fill(0, 0, width / 3, height, 0x6B000000);
            }
        }
    }
}
