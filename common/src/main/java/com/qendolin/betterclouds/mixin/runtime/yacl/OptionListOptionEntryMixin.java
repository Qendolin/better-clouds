package com.qendolin.betterclouds.mixin.runtime.yacl;

import com.qendolin.betterclouds.mixin.duck.OptionListEntryExtensionDuck;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.OptionListWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnusedMixin")
@Mixin(value = OptionListWidget.OptionEntry.class, remap = false)
public abstract class OptionListOptionEntryMixin extends ContainerObjectSelectionList.Entry<OptionListWidget.Entry> implements OptionListEntryExtensionDuck {

    @Shadow
    @Final
    public AbstractWidget widget;
    @Unique
    private BeforeRenderCallback better_clouds$beforeRender;
    @Unique
    private AfterRenderCallback better_clouds$afterRender;
    @Unique
    private int better_clouds$yPadding = 0;

    @Shadow
    protected abstract void updateHeight();

    @Override
    public void betterclouds$onBeforeRender(BeforeRenderCallback callback) {
        this.better_clouds$beforeRender = callback;
    }

    @Override
    public void betterclouds$onAfterRender(AfterRenderCallback callback) {
        this.better_clouds$afterRender = callback;
    }

    @Inject(method = "extractContent", at = @At("HEAD"), remap = false)
    private void onBeforeRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a, CallbackInfo ci) {
        if (better_clouds$beforeRender != null) {
            int x = widget.getDimension().x();
            int y = widget.getDimension().y();
            int w = widget.getDimension().width();
            int h = widget.getDimension().height();
            better_clouds$beforeRender.onBeforeRender(this, graphics, x, y, w, h, mouseX, mouseY, hovered, a);
        }
    }

    @Inject(method = "extractContent", at = @At("RETURN"), remap = false)
    private void onAfterRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a, CallbackInfo ci) {
        if (better_clouds$afterRender != null) {
            int x = widget.getDimension().x();
            int y = widget.getDimension().y();
            int w = widget.getDimension().width();
            int h = widget.getDimension().height();
            better_clouds$afterRender.onAfterRender(this, graphics, x, y, w, h, mouseX, mouseY, hovered, a);
        }
    }

    @Override
    public void betterclouds$setYPadding(int padding) {
        better_clouds$yPadding = padding;
        updateHeight();
    }

    @Override
    public int betterclouds$getYPadding() {
        return better_clouds$yPadding;
    }


    @ModifyArg(
            method = "updateHeight",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/isxander/yacl3/gui/OptionListWidget$OptionEntry;setHeight(I)V",
                    remap = true
            ))
    private int modifyYPadding(int original) {
        return original + better_clouds$yPadding;
    }
}
