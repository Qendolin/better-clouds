package com.qendolin.betterclouds.mixin;

import com.qendolin.betterclouds.gui.ConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TabButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TabButtonWidget.class)
public abstract class TabButtonWidgetMixin extends ClickableWidget {

    public TabButtonWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }


    //? if >=1.21 {
    //?} elif >1.20.2 {
    /*@Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    *///?} else {
    /*@Inject(method = "renderButton", at = @At("HEAD"), cancellable = true)
    *///?}
    //? if <1.21 {
    /*private void onRenderButton(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        // I'm gonna go to hell for this
        if (client == null || client.world == null || !(client.currentScreen instanceof ConfigScreen)) {
            return;
        }
        ci.cancel();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int i = active ? -1 : -6250336;
        this.drawMessage(context, textRenderer, i);
        if (this.isCurrentTab()) {
            this.drawCurrentTabLine(context, textRenderer, i);
        }
    }

    @Shadow
    public abstract void drawMessage(DrawContext context, TextRenderer textRenderer, int color);

    @Shadow
    public abstract boolean isCurrentTab();

    @Shadow
    protected abstract void drawCurrentTabLine(DrawContext context, TextRenderer textRenderer, int color);
    *///?}
}
