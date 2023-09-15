package com.qendolin.betterclouds.gui;

import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.gui.color.GammaRgbColor;
import com.qendolin.betterclouds.gui.color.LabColor;
import com.qendolin.betterclouds.mixin.BakedOverrideAccessor;
import com.qendolin.betterclouds.mixin.ItemRendererAccessor;
import com.qendolin.betterclouds.mixin.ModelOverrideListAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.IconButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GradientScreen extends Screen {

    private static final Identifier ICONS_TEXTURE = new Identifier(Main.MODID, "textures/gui/betterclouds/gui_icons.png");
    private static final Identifier WIDGETS_TEXTURE = new Identifier( "textures/gui/widgets.png");

    private static final Text TITLE_TEXT = Text.translatable("betterclouds.gui.gradient.title");

    public GradientScreen() {
        super(TITLE_TEXT);
    }

    protected GradientWidget<GammaRgbColor> gradient;
    protected ColorPickerWidget picker;

    @Override
    protected void init() {
        gradient = new GradientWidget<>(new Bounds(100, 100, 300, 40), GammaRgbColor.class, LabColor.class);
        addDrawableChild(gradient);

        List<ColorPickerWidget.ColorSpace<?>> spaces = List.of(ColorPickerWidget.ColorSpace.RGB, ColorPickerWidget.ColorSpace.HSL, ColorPickerWidget.ColorSpace.LCH);

        picker = new ColorPickerWidget(new Bounds(100, 180, 300, 300), ColorPickerWidget.ColorSpace.RGB);
        addDrawableChild(picker);

        final int[] selectedSpace = new int[]{0, 0};
        IconButtonWidget colorSpaceButton = IconButtonWidget.builder(Text.literal("Change Color Space"), ICONS_TEXTURE, button -> {
            selectedSpace[0]++;
            selectedSpace[0] %= spaces.size();
            ColorPickerWidget.ColorSpace<?> space = spaces.get(selectedSpace[0]);
            picker.setSpace(space);
        }).uv(40, 0).textureSize(64,64).xyOffset(0, 2).iconSize(16, 16).build();
        colorSpaceButton.setWidth(20);
        colorSpaceButton.setX(100);
        colorSpaceButton.setY(150);
        colorSpaceButton.setTooltipDelay(1000);
        colorSpaceButton.setTooltip(Tooltip.of(Text.translatable("betterclouds.gui.gradient.button.colorSpace.tooltip")));
        addDrawableChild(colorSpaceButton);

        ColorSwatch colorWidget = new ColorSwatch(new Bounds(125, 150, 200, 20));
        addDrawable(colorWidget);

        ColorInputWidget colorInputField = new ColorInputWidget(new Bounds(125 + 28 + 5, 150 + 6, textRenderer.getWidth("#000000"), 10));
        colorInputField.onChange = color -> {
            picker.setColor(color);
            colorWidget.setColor(color);
            gradient.setSelectedStopColor(color);
        };
        addDrawableChild(colorInputField);

        TextWidget atText = new TextWidget(Text.literal("@"), client.textRenderer);
        atText.setX(colorInputField.getX() + colorInputField.getWidth() + 5);
        atText.setY(colorInputField.getY() - 1);
        atText.setWidth(textRenderer.getWidth("@"));
        addDrawable(atText);

        AtomicBoolean colorChangeLock = new AtomicBoolean();

        TimeInputWidget timeInputField = new TimeInputWidget(new Bounds(atText.getX() + atText.getWidth() + 5, colorInputField.getY(), textRenderer.getWidth("00:00"), 10));
        addDrawableChild(timeInputField);

        ButtonWidget closeButton = ButtonWidget.builder(Text.literal("❌"), button -> {
            close();
        }).dimensions(40, 40, 20, 20).build();
        addDrawableChild(closeButton);

        StopLockButtonWidget lockButton = new StopLockButtonWidget(400-60, 150, button -> {
            if(button instanceof StopLockButtonWidget lock) {
                lock.setLocked(!gradient.selectedLocked());
            }
            boolean lock = !gradient.selectedLocked();
            gradient.lockSelectedStop(lock);
            if(gradient.selectedIndex() == 0) {
                gradient.setStopLocked(gradient.stopCount()-1, lock);
            } else if(gradient.selectedIndex() == gradient.stopCount()-1) {
                gradient.setStopLocked(0, lock);
            }
        });
        lockButton.setTooltipDelay(1000);
        lockButton.setTooltip(Tooltip.of(Text.translatable("betterclouds.gui.gradient.button.lock.tooltip")));
        addDrawableChild(lockButton);

        ButtonWidget duplicateStopButton = ButtonWidget.builder(Text.literal("↔"), button -> {
            int offset = 75;
            if(gradient.selectedPosition() >= 0.5) offset *= -1;
            gradient.addStop(gradient.selectedPosition() + offset / 2400f, gradient.selectedColor(), true);
        }).dimensions(400 - 40, 150, 20, 20).build();
        duplicateStopButton.setTooltipDelay(1000);
        duplicateStopButton.setTooltip(Tooltip.of(Text.translatable("betterclouds.gui.gradient.button.duplicate.tooltip")));
        addDrawableChild(duplicateStopButton);

        ButtonWidget removeStopButton = ButtonWidget.builder(Text.literal("❌"), button -> {
            gradient.removeSelectedStop();
        }).dimensions(400 - 20, 150, 20, 20).build();
        removeStopButton.setTooltipDelay(1000);
        removeStopButton.setTooltip(Tooltip.of(Text.translatable("betterclouds.gui.gradient.button.remove.tooltip")));
        addDrawableChild(removeStopButton);

        colorInputField.onChange = color -> {
            if(colorChangeLock.getAndSet(true)) return;
            picker.setColor(color);
            colorWidget.setColor(color);
            gradient.setSelectedStopColor(color);
            colorChangeLock.set(false);
        };

        gradient.onStopSelected = (index, pos, color) -> {
            if(colorChangeLock.getAndSet(true)) return;
            removeStopButton.active = index != -1 && gradient.stopCount() > 2;
            duplicateStopButton.active = index != -1;
            lockButton.active = index != -1;
            if(lockButton.active) {
                lockButton.setLocked(gradient.selectedLocked());
            }
            timeInputField.setTime(posToTime(pos) * 10);
            picker.setColor(color);
            colorWidget.setColor(color);
            colorInputField.setColor(color);
            colorChangeLock.set(false);
        };

        gradient.onStopMoved = (pos) -> {
            if(colorChangeLock.getAndSet(true)) return;
            timeInputField.setTime(posToTime(pos) * 10);
            colorChangeLock.set(false);
        };

        picker.onChanged = (color) -> {
            if(colorChangeLock.getAndSet(true)) return;
            gradient.setSelectedStopColor(color);
            if(gradient.selectedIndex() == 0) {
                gradient.setStopColor(gradient.stopCount()-1, color);
            } else if(gradient.selectedIndex() == gradient.stopCount()-1) {
                gradient.setStopColor(0, color);
            }
            colorWidget.setColor(color);
            colorInputField.setColor(color);
            colorChangeLock.set(false);
        };

        timeInputField.onChange = (time) -> {
            if(colorChangeLock.getAndSet(true)) return;
            gradient.setSelectedStopPosition(timeToPos(time/10));
            colorChangeLock.set(false);
        };

        gradient.addStop(0, new GammaRgbColor(1, 1, 1, 1), false);
        gradient.addStop(timeToPos(1200-75), new GammaRgbColor(1, 1, 1, 1), false);
        gradient.addStop(timeToPos(1200), GammaRgbColor.hex(225, 178, 157, 255), false);
        gradient.addStop(timeToPos(1200+75), GammaRgbColor.hex(31, 17, 21, 255), false);
        gradient.addStop(timeToPos(1500), GammaRgbColor.hex(52, 56, 60, 255), false);
        gradient.addStop(timeToPos(1800), GammaRgbColor.hex(52, 56, 60, 255), false);
        gradient.addStop(timeToPos(2100), GammaRgbColor.hex(52, 56, 60, 255), false);
        gradient.addStop(timeToPos(2400-75), GammaRgbColor.hex(31, 17, 21, 255), false);
        gradient.addStop(timeToPos(2400), GammaRgbColor.hex(255, 212, 149, 255), false);
        gradient.addStop(timeToPos(2400+75), new GammaRgbColor(1, 1, 1, 1), false);
        gradient.addStop(1, new GammaRgbColor(1, 1, 1, 1), false);
        gradient.selectStop(0);
    }

    private static float timeToPos(int time) {
        if(time >= 600) return time / 2400f - 0.25f;
        return time / 2400f + 0.75f;
    }

    private static int posToTime(float pos) {
        if(pos < 0.75f) return (600 + Math.round(pos * 2400)) % 2400;
        return Math.min(Math.round((pos - 0.75f) * 2400), 5999);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);

        super.render(context, mouseX, mouseY, delta);

        for (int i = 0; i < 5; i++) {
            int x = 100+5 + Math.round((300 - 11) * i / 4f);
            int time = ((i * 6000) + 6000) % 24000;
            drawClock(context, x-8, 100-16-3, time);
            context.drawVerticalLine(x, 100-3, 100+3, Colors.WHITE);
        }
    }

    protected void drawClock(DrawContext context, int x, int y, int time) {
        Item item = Items.CLOCK;
        ItemStack stack = item.getDefaultStack();
        Identifier timePredicate = new Identifier("time");
        BakedModel baseModel = client.getItemRenderer().getModels().getModel(item);
        ModelOverrideListAccessor overrides = (ModelOverrideListAccessor) baseModel.getOverrides();
        Identifier[] conditions = overrides.getConditionTypes();
        float[] values = new float[conditions.length];
        for (int j = 0; j < conditions.length; ++j) {
            Identifier identifier = conditions[j];
            if(identifier.equals(timePredicate)) {
                values[j] = ((time / 24000f) - 0.25f + 1.0f) % 1;
                break;
            }
        }
        BakedModel result = baseModel;
        for (ModelOverrideList.BakedOverride bakedOverride : overrides.getOverrides()) {
            BakedOverrideAccessor accessor = (BakedOverrideAccessor) bakedOverride;
            if (!accessor.callTest(values)) continue;
            BakedModel bakedModel = accessor.getModel();
            if (bakedModel != null) {
                result = bakedModel;
            }
            break;
        }
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y+16, 0);
        matrices.scale(16.0f, -16.0f, 16.0f);

        DiffuseLighting.disableGuiDepthLighting();
        ItemRendererAccessor itemRenderer = (ItemRendererAccessor) client.getItemRenderer();
        VertexConsumer vertexConsumer = context.getVertexConsumers().getBuffer(RenderLayers.getItemLayer(stack, true));
        itemRenderer.callRenderBakedItemModel(result, stack, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, matrices, vertexConsumer);
        context.draw();
        DiffuseLighting.enableGuiDepthLighting();

        matrices.pop();
    }
}
