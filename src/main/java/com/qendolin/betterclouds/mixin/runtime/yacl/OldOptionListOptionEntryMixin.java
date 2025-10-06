package com.qendolin.betterclouds.mixin.runtime.yacl;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.qendolin.betterclouds.duck.OptionListEntryExtensionDuck;
import dev.isxander.yacl3.gui.OptionListWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ElementListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({
    "UnusedMixin"
/*? if >=1.21 {*/, "MixinAnnotationTarget"/*?}*/
})
@Mixin(value = OptionListWidget.OptionEntry.class, remap = false)
public abstract class OldOptionListOptionEntryMixin extends ElementListWidget.Entry<OptionListWidget.Entry> implements OptionListEntryExtensionDuck {

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

    @Inject(
        method = {"render", "method_25343", "m_6311_"},
        at = @At("HEAD"),
        remap = false
    )
    private void onBeforeRender(DrawContext context, int index, int y, int x, int w, int h, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (beforeRender != null) {
            beforeRender.onBeforeRender( this, context, x, y, w, h, mouseX, mouseY, hovered, tickDelta);
        }
    }

    @Inject(
        method = {"render", "method_25343", "m_6311_"},
        at = @At("RETURN"),
        remap = false
    )
    private void onAfterRender(DrawContext context, int index, int y, int x, int w, int h, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (afterRender != null) {
            afterRender.onAfterRender(this, context, x, y, w, h, mouseX, mouseY, hovered, tickDelta);
        }
    }

    @Override
    public void betterclouds$setYPadding(int padding) {
        yPadding = padding;
    }

    @Override
    public int betterclouds$getYPadding() {
        return yPadding;
    }

    @ModifyReturnValue(method = "getItemHeight", at = @At(value = "RETURN"))
    private int modifyItemHeight(int original) {
        return original + yPadding;
    }
}
