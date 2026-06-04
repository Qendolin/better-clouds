package com.qendolin.betterclouds.mixin.required.yacl;

import com.qendolin.betterclouds.gui.CustomScrollableNavigationBar;
import dev.isxander.yacl3.gui.tab.ScrollableNavigationBar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ScrollableNavigationBar.class, remap = false)
public abstract class ScrollableNavigationBarMixin {

    @Inject(method = "extractWidgetRenderState", at = @At("HEAD"))
    private void betterclouds$drawTranslucentBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (!CustomScrollableNavigationBar.betterclouds$useTranslucentTheme(client)) {
            return;
        }

        if ((Object) this instanceof CustomScrollableNavigationBar custom) {
            context.fill(0, 0, custom.width, 23, 0x6b000000);
            context.fill(0, 23, custom.width, 24, 0xff000000);
        }
    }
}
