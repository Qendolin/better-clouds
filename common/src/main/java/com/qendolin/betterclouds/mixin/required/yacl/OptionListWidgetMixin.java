package com.qendolin.betterclouds.mixin.required.yacl;

import com.qendolin.betterclouds.mixin.duck.CustomOptionListWidgetDuck;
import com.qendolin.betterclouds.mixin.duck.OptionListEntryExtensionDuck;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.OptionListWidget;
import dev.isxander.yacl3.gui.controllers.LabelController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Mixin(value = OptionListWidget.class)
public abstract class OptionListWidgetMixin extends AbstractSelectionList<OptionListWidget.Entry> implements CustomOptionListWidgetDuck {

    @Unique
    private boolean better_clouds$override = false;

    public OptionListWidgetMixin(Minecraft client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
    }

    @Shadow(remap = false)
    public abstract void refreshOptions();

    @Override
    public void betterclouds$applyOverride() {
        better_clouds$override = true;
        refreshOptions();
    }

    @Inject(method = "refreshOptions", at = @At("TAIL"), remap = false)
    private void onRefreshOptions(CallbackInfo ci) {
        if (!better_clouds$override) return;

        for (OptionListWidget.Entry child : children()) {
            if (child instanceof OptionListWidget.OptionEntry entry && child instanceof OptionListEntryExtensionDuck duck) {
                if (entry.option.controller() instanceof LabelController) {
                    duck.betterclouds$onBeforeRender((self, context, _, _, _, _, _, _, _, _) -> {
                        if (minecraft.level == null) return;
                        if (!((OptionListWidget.OptionEntry) self).isViewable()) return;
                        Dimension<Integer> dim = ((OptionListWidget.OptionEntry) self).widget.getDimension();
                        context.fill(dim.x(), dim.y(), dim.xLimit(), dim.yLimit(), 0x6b000000);
                    });
                }
            } else if (child instanceof OptionListWidget.GroupSeparatorEntry && child instanceof OptionListEntryExtensionDuck duck) {
                duck.betterclouds$setYPadding(2);
                duck.betterclouds$onBeforeRender((self, context, x, y, entryWidth, entryHeight, _, _, _, _) -> {
                    if (minecraft.level == null) return;
                    if (!((OptionListWidget.GroupSeparatorEntry) self).isViewable()) return;
                    context.fill(x, y + 3, x + entryWidth + 1, y + entryHeight - 2, 0x6b000000);
                });
            }
        }

        // I cannot believe that this works
        OptionListWidget widget = (OptionListWidget) (Object) this;
        var padding = widget.new Entry() {
            @Override
            public @NonNull List<? extends GuiEventListener> children() {
                return List.of();
            }

            @Override
            public @NonNull List<? extends NarratableEntry> narratables() {
                return List.of();
            }

            public void extractContent(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            }
        };
        try {
            Method setHeightMethod = padding.getClass().getMethod("setHeight", int.class);
            setHeightMethod.invoke(padding, 4);
        } catch (NoSuchMethodException ignored) {
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        addEntry(padding);
    }
}
