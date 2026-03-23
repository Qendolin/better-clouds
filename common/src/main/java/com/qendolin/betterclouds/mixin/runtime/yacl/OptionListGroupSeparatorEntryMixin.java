package com.qendolin.betterclouds.mixin.runtime.yacl;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.qendolin.betterclouds.duck.OptionListEntryExtensionDuck;
import dev.isxander.yacl3.gui.OptionListWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnusedMixin")
@Mixin(value = OptionListWidget.GroupSeparatorEntry.class, remap = false)
public abstract class OptionListGroupSeparatorEntryMixin extends ContainerObjectSelectionList.Entry<OptionListWidget.Entry> implements OptionListEntryExtensionDuck {

    @Shadow protected abstract void updateHeight();

    @Unique
    private BeforeRenderCallback beforeRender;
    @Unique
    private AfterRenderCallback afterRender;
    @Unique
    private int yPadding = 0;

    @Override
    public void betterclouds$onBeforeRender(BeforeRenderCallback callback) {
        this.beforeRender = callback;
    }

    @Override
    public void betterclouds$onAfterRender(AfterRenderCallback callback) {
        this.afterRender = callback;
    }

    @Inject(method = "extractContent", at = @At("HEAD"), remap = false)
    private void onBeforeRender(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (beforeRender != null) {
            int x = ((OptionListWidget.Entry) (Object) this).getX();
            int y = ((OptionListWidget.Entry) (Object) this).getY();
            int w = ((OptionListWidget.Entry) (Object) this).getWidth();
            int h = ((OptionListWidget.Entry) (Object) this).getHeight();
            beforeRender.onBeforeRender(this, context, x, y, w, h, mouseX, mouseY, hovered, tickDelta);
        }
    }

    @Inject(method = "extractContent", at = @At("RETURN"), remap = false)
    private void onAfterRender(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (afterRender != null) {
            int x = ((OptionListWidget.Entry) (Object) this).getX();
            int y = ((OptionListWidget.Entry) (Object) this).getY();
            int w = ((OptionListWidget.Entry) (Object) this).getWidth();
            int h = ((OptionListWidget.Entry) (Object) this).getHeight();
            afterRender.onAfterRender(this, context, x, y, w, h, mouseX, mouseY, hovered, tickDelta);
        }
    }

    @Override
    public void betterclouds$setYPadding(int padding) {
        yPadding = padding;
        updateHeight();
    }

    @Override
    public int betterclouds$getYPadding() {
        return yPadding;
    }

    @ModifyReturnValue(method = "getYPadding", at = @At("RETURN"))
    private int modifyYPadding(int original) {
        return original + yPadding;
    }
}
