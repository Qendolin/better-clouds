package com.qendolin.betterclouds.mixin.required;

import com.qendolin.betterclouds.duck.TabNavigationWidgetExtensionDuck;
import net.minecraft.client.gui.widget.TabNavigationWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

//? if <1.20.6 {
/*import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
*///?}

@Mixin(TabNavigationWidget.class)
public class TabNavigationWidgetMixin implements TabNavigationWidgetExtensionDuck {
    @Unique
    private boolean renderBackground = true;

    @Override
    public void betterclouds$setRenderBackground(boolean renderBackground) {
        this.renderBackground = renderBackground;
    }

    //? if <1.20.6 {
    /*@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"), expect = 1)
    private void onRenderBackground(DrawContext instance, int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
        if (renderBackground) {
            original.call(instance, x1, y1, x2, y2, color);
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIFFIIII)V"), expect = 1)
    private void onRenderBackground(DrawContext instance, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, Operation<Void> original) {
        if (renderBackground) {
            original.call(instance, texture, x, y, u, v, width, height, textureWidth, textureHeight);
        }
    }
    *///?}
}
