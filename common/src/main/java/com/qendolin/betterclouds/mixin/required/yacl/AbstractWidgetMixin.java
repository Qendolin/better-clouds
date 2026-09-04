package com.qendolin.betterclouds.mixin.required.yacl;

import com.qendolin.betterclouds.gui.ConfigScreen;
import com.qendolin.betterclouds.gui.CustomButtonOption;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.controllers.PopupControllerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin {
    @Shadow
    @Final
    protected Minecraft client;

    @Unique
    private static void betterclouds$drawOutline(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2, int width, int color) {
        context.fill(x1, y1, x2, y1 + width, color);
        context.fill(x2, y1, x2 - width, y2, color);
        context.fill(x1, y2, x2, y2 - width, color);
        context.fill(x1, y1, x1 + width, y2, color);
    }

    @Inject(method = "drawButtonRect", at = @At("HEAD"), cancellable = true)
    private void onDrawButtonRect(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, boolean hovered, boolean enabled, CallbackInfo ci) {
        if (!betterclouds$useTranslucentTheme()) {
            return;
        }
        ci.cancel();

        if (x1 > x2) {
            int xx1 = x1;
            x1 = x2;
            x2 = xx1;
        }
        if (y1 > y2) {
            int yy1 = y1;
            y1 = y2;
            y2 = yy1;
        }

        graphics.fill(x1, y1, x2, y2, hovered ? 0xa1000000 : 0x60000000);
    }

    @Unique
    private boolean betterclouds$useTranslucentTheme() {
        if (client == null || client.level == null) {
            return false;
        }
        if (client.gui.screen() instanceof ConfigScreen) {
            return true;
        }
        if (client.gui.screen() instanceof PopupControllerScreen popupScreen) {
            return ((PopupControllerScreenAccessor) popupScreen).getBackgroundYaclScreen() instanceof ConfigScreen;
        }
        return false;
    }
}
