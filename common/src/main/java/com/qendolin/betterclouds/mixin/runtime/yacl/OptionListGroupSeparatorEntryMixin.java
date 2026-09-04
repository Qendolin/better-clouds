package com.qendolin.betterclouds.mixin.runtime.yacl;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.qendolin.betterclouds.mixin.duck.ListOptionDuck;
import com.qendolin.betterclouds.mixin.duck.OptionListEntryExtensionDuck;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.gui.OptionListWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({ "UnusedMixin", "RedundantCast", "DataFlowIssue" })
@Mixin(value = OptionListWidget.GroupSeparatorEntry.class, remap = false)
public abstract class OptionListGroupSeparatorEntryMixin extends ContainerObjectSelectionList.Entry<OptionListWidget.Entry> implements OptionListEntryExtensionDuck {

    @Shadow
    @Final
    protected OptionGroup group;
    @Shadow
    protected boolean groupExpanded;
    @Unique
    private BeforeRenderCallback betterclouds$beforeRender;
    @Unique
    private AfterRenderCallback betterclouds$afterRender;
    @Unique
    private int betterclouds$yPadding = 0;

    @Shadow
    protected abstract void updateHeight();

    @Shadow
    protected abstract void updateExpandMinimizeText();

    @Inject(method = "setExpanded", at = @At("HEAD"), cancellable = true, remap = false)
    private void preserveExpandedState(boolean expanded, CallbackInfo ci) {
        if (expanded) {
            return;
        }
        if (group instanceof ListOptionDuck duck && duck.betterclouds$forceExpanded()) {
            ci.cancel();
        }
    }

    @Override
    public void betterclouds$onBeforeRender(BeforeRenderCallback callback) {
        this.betterclouds$beforeRender = callback;
    }

    @Override
    public void betterclouds$onAfterRender(AfterRenderCallback callback) {
        this.betterclouds$afterRender = callback;
    }

    @Inject(method = "extractContent", at = @At("HEAD"), remap = false)
    private void onBeforeRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a, CallbackInfo ci) {
        if (betterclouds$beforeRender != null) {
            int x = ((OptionListWidget.Entry) (Object) this).getX();
            int y = ((OptionListWidget.Entry) (Object) this).getY();
            int w = ((OptionListWidget.Entry) (Object) this).getWidth();
            int h = ((OptionListWidget.Entry) (Object) this).getHeight();
            betterclouds$beforeRender.onBeforeRender(this, graphics, x, y, w, h, mouseX, mouseY, hovered, a);
        }
    }

    @Inject(method = "extractContent", at = @At("RETURN"), remap = false)
    private void onAfterRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a, CallbackInfo ci) {
        if (betterclouds$afterRender != null) {
            int x = ((OptionListWidget.Entry) (Object) this).getX();
            int y = ((OptionListWidget.Entry) (Object) this).getY();
            int w = ((OptionListWidget.Entry) (Object) this).getWidth();
            int h = ((OptionListWidget.Entry) (Object) this).getHeight();
            betterclouds$afterRender.onAfterRender(this, graphics, x, y, w, h, mouseX, mouseY, hovered, a);
        }
    }

    @Override
    public void betterclouds$setYPadding(int padding) {
        betterclouds$yPadding = padding;
        updateHeight();
    }

    @Override
    public int betterclouds$getYPadding() {
        return betterclouds$yPadding;
    }

    @Override
    public void betterclouds$setExpanded(boolean expanded) {
        groupExpanded = expanded;
        updateExpandMinimizeText();
        updateHeight();
    }

    @ModifyReturnValue(method = "getYPadding", at = @At("RETURN"))
    private int modifyYPadding(int original) {
        return original + betterclouds$yPadding;
    }
}
