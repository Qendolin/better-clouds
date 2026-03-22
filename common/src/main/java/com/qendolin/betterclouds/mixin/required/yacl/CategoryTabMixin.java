package com.qendolin.betterclouds.mixin.required.yacl;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.qendolin.betterclouds.compat.ReflectAccess;
import com.qendolin.betterclouds.config.ConfigGUI;
import com.qendolin.betterclouds.duck.CustomCategoryTabDuck;
import com.qendolin.betterclouds.duck.CustomOptionListWidgetDuck;
import com.qendolin.betterclouds.gui.ConfigScreen;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.utils.GuiUtils;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Mixin(value = YACLScreen.CategoryTab.class, remap = false)
public abstract class CategoryTabMixin implements CustomCategoryTabDuck {

    @Shadow @Final private YACLScreen screen;
    @Shadow @Final public Button undoButton;
    @Shadow @Final public Button cancelResetButton;
    @Shadow @Final public Button saveFinishedButton;

    @Shadow public abstract void updateButtons();

    @Unique
    private boolean override;

    @Unique
    private Button hideShowButton;

    @Override
    public void betterclouds$applyOverride() {
        undoButton.active = false;
        undoButton.visible = false;
        override = true;

        try {
            Field optionListField = getClass().getDeclaredField("optionList");
            optionListField.setAccessible(true);
            Object optionList = optionListField.get(this);
            Method getListMethod = optionList.getClass().getDeclaredMethod("getType");
            getListMethod.setAccessible(true);
            Object optionListObject = getListMethod.invoke(optionList);
            ((CustomOptionListWidgetDuck) optionListObject).betterclouds$applyOverride();
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new ReflectAccess.IncompatibleModDependencyException("YACL", e);
        }

        Minecraft client = Minecraft.getInstance();

        hideShowButton = Button.builder(Component.translatable(ConfigGUI.LANG_KEY_PREFIX + ".hide"),
                btn -> hideOrShow())
            .pos(undoButton.getX(), undoButton.getY())
            .size(undoButton.getWidth(), undoButton.getHeight())
            .build();
        hideShowButton.active = client.level != null;
    }

    @Unique
    private void hideOrShow() {
        Minecraft client = Minecraft.getInstance();
        if (client.screen == screen) {
            hideShowButton.setMessage(Component.translatable(ConfigGUI.LANG_KEY_PREFIX + ".show"));
            Screen hiddenScreen = new ConfigScreen.HiddenScreen(screen.getTitle(), hideShowButton);
            client.setScreen(hiddenScreen);
        } else {
            hideShowButton.setMessage(Component.translatable(ConfigGUI.LANG_KEY_PREFIX + ".hide"));
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

        if (Minecraft.getInstance().hasShiftDown()) {
            cancelResetButton.active = true;
            cancelResetButton.setTooltip(Tooltip.create(Component.translatable(ConfigGUI.LANG_KEY_PREFIX + ".reset.tooltip")));
        } else {
            cancelResetButton.active = false;
            cancelResetButton.setTooltip(Tooltip.create(Component.translatable(ConfigGUI.LANG_KEY_PREFIX + ".reset.tooltip.holdShift")));
        }
        cancelResetButton.setMessage(pendingChanges ? GuiUtils.translatableFallback("yacl.gui.cancel", CommonComponents.GUI_CANCEL) : Component.translatable("controls.reset"));

        saveFinishedButton.setMessage(pendingChanges ? Component.translatable("yacl.gui.save") : GuiUtils.translatableFallback("yacl.gui.done", CommonComponents.GUI_DONE));
        saveFinishedButton.setTooltip(Tooltip.create(pendingChanges ? Component.translatable("yacl.gui.save.tooltip") : Component.translatable("yacl.gui.finished.tooltip")));
    }

    @Inject(
        method = "tick",
        at = @At("TAIL")
    )
    private void onTick(CallbackInfo ci) {
        if (!override) return;

        updateButtons();
    }

    @Inject(method = "visitChildren", at = @At("TAIL"), remap = true)
    private void onForEachChild(Consumer<AbstractWidget> consumer, CallbackInfo ci) {
        if (!override) return;

        consumer.accept(hideShowButton);
    }
}
