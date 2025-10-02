package com.qendolin.betterclouds.mixin.runtime.yacl;

//? if >=1.21 {
import com.qendolin.betterclouds.duck.OptionListEntryExtensionDuck;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.OptionListWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ElementListWidget;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnusedMixin")
@Mixin(value = OptionListWidget.OptionEntry.class, remap = false)
public abstract class OptionListOptionEntryMixin extends ElementListWidget.Entry<OptionListWidget.Entry> implements OptionListEntryExtensionDuck {

    @Shadow
    @Final
    public AbstractWidget widget;

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

    @Inject(method = "renderContent", at = @At("HEAD"))
    private void onBeforeRender(DrawContext context, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (beforeRender != null) {
            int x = widget.getDimension().x();
            int y = widget.getDimension().y();
            int w = widget.getDimension().width();
            int h = widget.getDimension().height();
            beforeRender.onBeforeRender(this, context, x, y, w, h, mouseX, mouseY, hovered, tickDelta);
        }
    }

    @Inject(method = "renderContent", at = @At("RETURN"))
    private void onAfterRender(DrawContext context, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (afterRender != null) {
            int x = widget.getDimension().x();
            int y = widget.getDimension().y();
            int w = widget.getDimension().width();
            int h = widget.getDimension().height();
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

    @ModifyArg(method = "updateHeight", at = @At(value = "INVOKE", target = "Ldev/isxander/yacl3/gui/OptionListWidget$OptionEntry;setHeight(I)V"))
    private int modifyYPadding(int original) {
        return original + yPadding;
    }
}
//?}