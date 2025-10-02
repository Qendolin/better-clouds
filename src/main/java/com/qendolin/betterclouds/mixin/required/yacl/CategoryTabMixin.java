package com.qendolin.betterclouds.mixin.required.yacl;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.qendolin.betterclouds.compat.ReflectAccess;
import com.qendolin.betterclouds.compat.YACLCompat;
import com.qendolin.betterclouds.config.ConfigGUI;
import com.qendolin.betterclouds.duck.CustomCategoryTabDuck;
import com.qendolin.betterclouds.duck.CustomOptionListWidgetDuck;
import com.qendolin.betterclouds.gui.ConfigScreen;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.utils.GuiUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

@Mixin(value = YACLScreen.CategoryTab.class, remap = false)
public abstract class CategoryTabMixin implements CustomCategoryTabDuck {

    @Shadow @Final private YACLScreen screen;
    @Shadow @Final public ButtonWidget undoButton;
    @Shadow @Final public ButtonWidget cancelResetButton;
    @Shadow @Final public ButtonWidget saveFinishedButton;

    @Shadow public abstract void updateButtons();

    @Unique
    private boolean override;

    @Unique
    private ButtonWidget hideShowButton;

    @Override
    public void betterclouds$applyOverride() {
        undoButton.active = false;
        undoButton.visible = false;
        override = true;

        try {
            Field optionListField = getClass().getDeclaredField("optionList");
            optionListField.setAccessible(true);
            Object optionList = optionListField.get(this);
            Method getListMethod;
            if(YACLCompat.getVersion().compareTo(YACLCompat.Version3_8_0) >= 0) {
                getListMethod = optionList.getClass().getDeclaredMethod("getType");
            } else {
                getListMethod = optionList.getClass().getDeclaredMethod("getList");
            }
            getListMethod.setAccessible(true);
            Object optionListObject = getListMethod.invoke(optionList);
            ((CustomOptionListWidgetDuck) optionListObject).betterclouds$applyOverride();
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new ReflectAccess.IncompatibleModDependencyException("YACL", e);
        }

        MinecraftClient client = MinecraftClient.getInstance();

        hideShowButton = ButtonWidget.builder(Text.translatable(ConfigGUI.LANG_KEY_PREFIX + ".hide"),
                btn -> hideOrShow())
            .position(undoButton.getX(), undoButton.getY())
            .size(undoButton.getWidth(), undoButton.getHeight())
            .build();
        hideShowButton.active = client.world != null;
    }

    @Unique
    public void hideOrShow() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen == screen) {
            hideShowButton.setMessage(Text.translatable(ConfigGUI.LANG_KEY_PREFIX + ".show"));
            Screen hiddenScreen = new ConfigScreen.HiddenScreen(screen.getTitle(), hideShowButton);
            client.setScreen(hiddenScreen);
        } else {
            hideShowButton.setMessage(Text.translatable(ConfigGUI.LANG_KEY_PREFIX + ".hide"));
            client.setScreen(screen);
        }
    }

    @WrapMethod(method = "updateButtons")
    private void updateButtons(Operation<Void> original) {
        if (!override) {
            original.call();
            return;
        }

        boolean pendingChanges = screen.pendingChanges();

        //? if >=1.21.9 {
        if (MinecraftClient.getInstance().isShiftPressed()) {
        //?} else {
        /*if (Screen.hasShiftDown()) {
        *///?}
            cancelResetButton.active = true;
            cancelResetButton.setTooltip(Tooltip.of(Text.translatable(ConfigGUI.LANG_KEY_PREFIX + ".reset.tooltip")));
        } else {
            cancelResetButton.active = false;
            cancelResetButton.setTooltip(Tooltip.of(Text.translatable(ConfigGUI.LANG_KEY_PREFIX + ".reset.tooltip.holdShift")));
        }
        cancelResetButton.setMessage(pendingChanges ? GuiUtils.translatableFallback("yacl.gui.cancel", ScreenTexts.CANCEL) : Text.translatable("controls.reset"));

        saveFinishedButton.setMessage(pendingChanges ? Text.translatable("yacl.gui.save") : GuiUtils.translatableFallback("yacl.gui.done", ScreenTexts.DONE));
        saveFinishedButton.setTooltip(Tooltip.of(pendingChanges ? Text.translatable("yacl.gui.save.tooltip") : Text.translatable("yacl.gui.finished.tooltip")));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (!override) {
            return;
        }

        updateButtons();
    }

    @Inject(method = "forEachChild", at = @At("TAIL"), remap = true)
    private void onForEachChild(Consumer<ClickableWidget> consumer, CallbackInfo ci) {
        if (!override) return;

        consumer.accept(hideShowButton);
    }
}
