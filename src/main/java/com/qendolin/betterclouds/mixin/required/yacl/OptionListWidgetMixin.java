package com.qendolin.betterclouds.mixin.required.yacl;

import com.qendolin.betterclouds.compat.YACLCompat;
import com.qendolin.betterclouds.duck.OptionListEntryExtensionDuck;
import com.qendolin.betterclouds.duck.CustomOptionListWidgetDuck;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.OptionListWidget;
import dev.isxander.yacl3.gui.controllers.LabelController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.EntryListWidget;
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
public abstract class OptionListWidgetMixin extends EntryListWidget<OptionListWidget.Entry> implements CustomOptionListWidgetDuck   {

    @Shadow(remap = false) public abstract void refreshOptions();

    @Unique
    private boolean override = false;

    //? if >1.20.1 {
    public OptionListWidgetMixin(MinecraftClient client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
    }
    //?} else {
    /*public OptionListWidgetMixin(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom, itemHeight);
    }
    *///?}

    @Override
    public void betterclouds$applyOverride() {
        override = true;
        refreshOptions();
        //? if <1.20.6 {
        /*setRenderBackground(client.world == null);
        *///?}
        //? if <1.20.4 {
        /*setRenderHorizontalShadows(client.world == null);
        *///?}
    }

    @Inject(method = "refreshOptions", at=@At("TAIL"), remap = false)
    private void onRefreshOptions(CallbackInfo ci) {
        if (!override) return;

        for (OptionListWidget.Entry child : children()) {
            if(child instanceof OptionListWidget.OptionEntry entry && child instanceof OptionListEntryExtensionDuck duck) {
                if(entry.option.controller() instanceof LabelController) {
                    duck.betterclouds$onBeforeRender((self, context, x, y, width, height, mouseX, mouseY, hovered, tickDelta) -> {
                        if (client.world == null) return;
                        if (!((OptionListWidget.OptionEntry) self).isViewable()) return;
                        Dimension<Integer> dim = ((OptionListWidget.OptionEntry) self).widget.getDimension();
                        context.fill(dim.x(), dim.y(), dim.xLimit(), dim.yLimit(), 0x6b000000);
                    });
                }
            } else if(child instanceof OptionListWidget.GroupSeparatorEntry && child instanceof OptionListEntryExtensionDuck duck) {
                duck.betterclouds$setYPadding(2);
                duck.betterclouds$onBeforeRender((self, context, x, y, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta) -> {
                    if (client.world == null) return;
                    if (!((OptionListWidget.GroupSeparatorEntry) self).isViewable()) return;
                    if(YACLCompat.isVersion3_8_0()) {
                        context.fill(x, y + 3, x + entryWidth + 1, y + entryHeight - 2, 0x6b000000);
                    } else {
                        context.fill(x, y + 1, x + entryWidth + 1, y + entryHeight, 0x6b000000);
                    }
                });
            }
        }

        // I cannot believe that this works
        var padding = ((OptionListWidget) (Object) this).new Entry() {
            @Override
            public List<? extends Element> children() {
                return List.of();
            }

            @Override
            public List<? extends Selectable> selectableChildren() {
                return List.of();
            }

            //? if >=1.21.9 {
            public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            }
            //?} else {
            /*public void renderContent(DrawContext drawContext, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            }
            *///?}

            // <3.8.0
            @Deprecated
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            }

            @Deprecated
            public int getItemHeight() {
                // not sure that this works, but whatever
                return 4;
            }
        };
        //? if >1.20.1 {
        try {
            // For >=3.8.0
            Method setHeightMethod = padding.getClass().getMethod("setHeight", int.class);
            setHeightMethod.invoke(padding, 4);
        } catch (NoSuchMethodException ignored) {
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        //?}

        addEntry(padding);

        try {
            // For <3.8.0
            Method recacheViewableChildrenMethod = getClass().getMethod("recacheViewableChildren");
            recacheViewableChildrenMethod.invoke(this);
        } catch (NoSuchMethodException ignored) {
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
