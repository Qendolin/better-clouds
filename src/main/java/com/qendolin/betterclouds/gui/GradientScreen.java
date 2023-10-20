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
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class GradientScreen extends Screen {

    private static final Identifier ICONS_TEXTURE = new Identifier(Main.MODID, "textures/gui/betterclouds/gui_icons.png");

    private static final Text TITLE_TEXT = Text.translatable("betterclouds.gui.gradient.title");
    private static final int TOOLTIP_DELAY = 1000;

    private static final List<ColorPickerWidget.ColorSpace<?>> SPACES = List.of(ColorPickerWidget.ColorSpace.RGB, ColorPickerWidget.ColorSpace.HSL, ColorPickerWidget.ColorSpace.LCH);


    public GradientScreen() {
        super(TITLE_TEXT);
    }

    protected GradientWidget<GammaRgbColor> gradient;
    protected ColorPickerWidget picker;
    protected boolean dirty = true;
    protected NativeImageBackedTexture cache;
    protected Identifier cacheId;
    protected Map<TimelineWidget.TimelineKeyframe, KeyframeData> config = new HashMap<>();

    protected static class KeyframeData {
        public List<GradientWidget.GradientStop<GammaRgbColor>> stops = new ArrayList<>();
        public int focusedIndex = 0;
    }

    @Override
    protected void init() {
        gradient = new GradientWidget<>(new Bounds(100, 100, 300, 40), GammaRgbColor.class, LabColor.class);
        addDrawableChild(gradient);

        picker = new ColorPickerWidget(new Bounds(100, 180, 300, 300), ColorPickerWidget.ColorSpace.RGB);
        addDrawableChild(picker);

        TimelineWidget timeline = new TimelineWidget(new Bounds(100, 20, 300, 40));
        addDrawableChild(timeline);

        IconButtonWidget colorSpaceButton = createColorSpaceButton();
        addDrawableChild(colorSpaceButton);

        ColorSwatch colorWidget = new ColorSwatch(new Bounds(125, 150, 200, 20));
        addDrawable(colorWidget);

        Bounds directInputBounds = new Bounds(125 + 28 + 5, 150 + 6, 0, 10);
        ColorInputWidget colorInputField = createColorInputField(directInputBounds);
        addDrawableChild(colorInputField);

        directInputBounds.setX(directInputBounds.limitX() + 5);
        TextWidget atText = createAtText(directInputBounds);
        addDrawable(atText);

        directInputBounds.setX(directInputBounds.limitX() + 5);
        TimeInputWidget timeInputField = createTimeInputField(directInputBounds);
        addDrawableChild(timeInputField);

        addDrawableChild(createCloseButton());

        StopLockButtonWidget lockButton = createStopLockButton();
        addDrawableChild(lockButton);

        ButtonWidget duplicateStopButton = createDuplicateStopButton();
        addDrawableChild(duplicateStopButton);

        ButtonWidget removeStopButton = createRemoveStopButton();
        addDrawableChild(removeStopButton);

        AtomicBoolean change = new AtomicBoolean();

        colorInputField.onChange = color -> {
            dirty = true;
            if(change.getAndSet(true)) return;
            picker.setColor(color);
            colorWidget.setColor(color);
            gradient.setStopColor(gradient.focusedIndex(), color);
            change.set(false);
        };

        gradient.onStopSelected = (index, data) -> {
            dirty = true;
            float pos = 0;
            GammaRgbColor color = null;

            if(data != null) {
                pos = data.position();
                color = data.color();
            }

            removeStopButton.active = index != -1;
            duplicateStopButton.active = index != -1;
            lockButton.active = index != -1;
            timeInputField.active = index != -1;
            colorInputField.active = index != -1;
            picker.active = index != -1;
            if(lockButton.active) {
                lockButton.setLocked(gradient.isStopLocked(index));
            }
            timeInputField.setTime(posToTime(pos) * 10);
            picker.setColor(color);
            colorWidget.setColor(color);
            colorInputField.setColor(color);
        };

        gradient.onStopChanged = (index, data) -> {
            dirty = true;
            if(change.getAndSet(true)) return;
            timeInputField.setTime(posToTime(data.position) * 10);
            change.set(false);
        };

        picker.onChanged = (color) -> {
            dirty = true;
            if(change.getAndSet(true)) return;
            gradient.setStopColor(gradient.focusedIndex(), color);
            colorWidget.setColor(color);
            colorInputField.setColor(color);
            change.set(false);
        };

        timeInputField.onChange = (time) -> {
            dirty = true;
            if(change.getAndSet(true)) return;
            gradient.setStopPosition(gradient.focusedIndex(), timeToPos(time/10));
            change.set(false);
        };

        timeline.onStopSelected = (index, data) -> {
            dirty = true;
            if(change.getAndSet(true)) return;
            while (gradient.count() > 0) {
                var removed = gradient.removeStop(0);
                if(removed == null) break;
            }
            if(data != null) {
                for (GradientWidget.GradientStop<GammaRgbColor> stop : config.get(data).stops) {
                    gradient.addStop(stop);
                }
                gradient.focusStop(config.get(data).focusedIndex);
            }
            change.set(false);
        };

        timeline.onStopAdded = (index, data) -> {
            dirty = true;
            if(change.getAndSet(true)) return;
            config.put(data, new KeyframeData());
            while (gradient.count() > 0) {
                GradientWidget.GradientStopElement<GammaRgbColor> removed = gradient.removeStop(0);
                if(removed == null) break;
            }
//            if(timeline.count() > 1) {
//                TimelineWidget.TimelineKeyframe prev = timeline.getStop((index-1+timeline.count())%timeline.count());
//                TimelineWidget.TimelineKeyframe next = timeline.getStop((index+1)%timeline.count());
//                for (GradientWidget.GradientStop<GammaRgbColor> stop : config.get(prev).stops) {
//                    gradient.addStop(stop.position, stop.color());
//                }
//                for (GradientWidget.GradientStop<GammaRgbColor> stop : config.get(next).stops) {
//                    gradient.addStop(stop.position, stop.color());
//                }
//            }

            change.set(false);
        };

        timeline.onStopChanged = (index, data) -> {
            dirty = true;
        };

        timeline.onStopRemoved = (index, data) -> {
            dirty = true;
            if(change.getAndSet(true)) return;
            config.remove(data);
            while (gradient.count() > 0) {
                var removed = gradient.removeStop(0);
                if(removed == null) break;
            }
            change.set(false);
        };

        gradient.onStopAdded = (index, data) -> {
            if(timeline.focusedStop() == null) return;
            config.get(timeline.focusedStop()).stops.add(data);
        };

        gradient.onStopRemoved = (index, data) -> {
            if(timeline.focusedStop() == null) return;
            config.get(timeline.focusedStop()).stops.remove(data);
        };

        timeline.focusStop(timeline.addStop(0));
        gradient.addStop(0, new GammaRgbColor(1, 1, 1));
        timeline.addStop(0.2f);
        timeline.focusStop(timeline.addStop(0.25f));
        gradient.addStop(0, GammaRgbColor.hex(0xfff77c));
        gradient.addStop(0.1f, GammaRgbColor.hex(0xd37232));
        gradient.addStop(1, GammaRgbColor.hex(0x181b32));
        timeline.focusStop(timeline.addStop(0.3f));
        gradient.addStop(0.5f, GammaRgbColor.hex(0x0e0f16));
        gradient.addStop(0.75f, GammaRgbColor.hex(0x181b32));
        gradient.addStop(0.9f, GammaRgbColor.hex(0x2c334a));
        gradient.addStop(1.0f, GammaRgbColor.hex(0xafcbdb));
        timeline.focusStop(timeline.addStop(0.7f));
        gradient.addStop(0.5f, GammaRgbColor.hex(0x0e0f16));
        gradient.addStop(0.75f, GammaRgbColor.hex(0x181b32));
        gradient.addStop(0.9f, GammaRgbColor.hex(0x2c334a));
        gradient.addStop(1.0f, GammaRgbColor.hex(0xafcbdb));
        timeline.focusStop(timeline.addStop(0.75f));
        gradient.addStop(0, GammaRgbColor.hex(0xfff77c));
        gradient.addStop(0.1f, GammaRgbColor.hex(0xd37232));
        gradient.addStop(1, GammaRgbColor.hex(0x181b32));
        timeline.focusStop(timeline.addStop(0.8f));
        gradient.addStop(0, GammaRgbColor.hex(0xffffff));
        gradient.addStop(1, GammaRgbColor.hex(0xbed9ff));
        timeline.focusStop(timeline.addStop(0.9f));
        gradient.addStop(0, GammaRgbColor.hex(0xffffff));
        gradient.addStop(1, GammaRgbColor.hex(0xffffff));

        cache = new NativeImageBackedTexture(32, 32, false);
        cacheId = client.getTextureManager().registerDynamicTexture("cloud_gradient", cache);
    }

    @NotNull
    private ButtonWidget createRemoveStopButton() {
        ButtonWidget removeStopButton = ButtonWidget.builder(Text.literal("❌"), button -> {
            gradient.removeStop(gradient.focusedIndex());
        }).dimensions(400 - 20, 150, 20, 20).build();
        removeStopButton.setTooltipDelay(TOOLTIP_DELAY);
        removeStopButton.setTooltip(Tooltip.of(Text.translatable("betterclouds.gui.gradient.button.remove.tooltip")));
        return removeStopButton;
    }

    @NotNull
    private ButtonWidget createDuplicateStopButton() {
        ButtonWidget duplicateStopButton = ButtonWidget.builder(Text.literal("↔"), button -> {
            int offset = 75;
            if(gradient.focusedStop().position() >= 0.5) offset *= -1;
            int index = gradient.addStop(gradient.focusedStop().position() + offset / 2400f, gradient.focusedStop().color());
            gradient.focusStop(index);
        }).dimensions(400 - 40, 150, 20, 20).build();
        duplicateStopButton.setTooltipDelay(TOOLTIP_DELAY);
        duplicateStopButton.setTooltip(Tooltip.of(Text.translatable("betterclouds.gui.gradient.button.duplicate.tooltip")));
        return duplicateStopButton;
    }

    @NotNull
    private StopLockButtonWidget createStopLockButton() {
        StopLockButtonWidget lockButton = new StopLockButtonWidget(400-60, 150, button -> {
            if(button instanceof StopLockButtonWidget lock) {
                lock.setLocked(!gradient.focusedStop().locked());
            }
            boolean lock = !gradient.focusedStop().locked();
            gradient.setStopLocked(gradient.focusedIndex(), lock);
            if(gradient.focusedIndex() == 0) {
                gradient.setStopLocked(gradient.count()-1, lock);
            } else if(gradient.focusedIndex() == gradient.count()-1) {
                gradient.setStopLocked(0, lock);
            }
        });
        lockButton.setTooltipDelay(TOOLTIP_DELAY);
        lockButton.setTooltip(Tooltip.of(Text.translatable("betterclouds.gui.gradient.button.lock.tooltip")));
        return lockButton;
    }

    private ButtonWidget createCloseButton() {
        return ButtonWidget.builder(Text.literal("❌"), button -> {
            close();
        }).dimensions(40, 40, 20, 20).build();
    }

    @NotNull
    private TimeInputWidget createTimeInputField(Bounds directInputBounds) {
        directInputBounds.setWidth(textRenderer.getWidth("00:00"));
        return new TimeInputWidget(new Bounds(directInputBounds.x(), directInputBounds.y(), directInputBounds.width(), 10));
    }

    @NotNull
    private TextWidget createAtText(Bounds directInputBounds) {
        TextWidget atText = new TextWidget(Text.literal("@"), client.textRenderer);
        atText.setX(directInputBounds.x());
        atText.setY(directInputBounds.y() - 1);
        atText.setWidth(textRenderer.getWidth("@"));
        directInputBounds.setWidth(atText.getWidth());
        return atText;
    }

    @NotNull
    private ColorInputWidget createColorInputField(Bounds directInputBounds) {
        directInputBounds.setSize(textRenderer.getWidth("#000000"), 10);
        return new ColorInputWidget(directInputBounds);
    }

    @NotNull
    private IconButtonWidget createColorSpaceButton() {
        final int[] selectedSpace = new int[]{0};
        IconButtonWidget colorSpaceButton = IconButtonWidget.builder(Text.literal("Change Color Space"), ICONS_TEXTURE, button -> {
            selectedSpace[0]++;
            selectedSpace[0] %= SPACES.size();
            ColorPickerWidget.ColorSpace<?> space = SPACES.get(selectedSpace[0]);
            picker.setSpace(space);
        }).uv(40, 0).textureSize(64,64).xyOffset(0, 2).iconSize(16, 16).build();
        colorSpaceButton.setWidth(20);
        colorSpaceButton.setX(100);
        colorSpaceButton.setY(150);
        colorSpaceButton.setTooltipDelay(TOOLTIP_DELAY);
        colorSpaceButton.setTooltip(Tooltip.of(Text.translatable("betterclouds.gui.gradient.button.colorSpace.tooltip")));
        return colorSpaceButton;
    }

    @Override
    public void close() {
        super.close();
        cache.close();
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

        if(dirty) {
            dirty = false;
            List<GradientWidget.GradientGenerator<GammaRgbColor, LabColor>> yGenerators = new ArrayList<>(32);
            for (int x = 0; x < 32; x++) {
                yGenerators.add(new GradientWidget.GradientGenerator<>(32, GammaRgbColor.class, LabColor.class, new GammaRgbColor(0,0,0)));
            }
            for (TimelineWidget.TimelineKeyframe keyframe : config.keySet().stream().sorted().toList()) {
                var xGenerator = new GradientWidget.GradientGenerator<>(32, GammaRgbColor.class, LabColor.class, new GammaRgbColor(0,0,0));
                for (GradientWidget.GradientStop<GammaRgbColor> stop : config.get(keyframe).stops) {
                    xGenerator.addStop(stop);
                }
                xGenerator.generate();
                GammaRgbColor[] row = xGenerator.getAll();
                for (int x = 0; x < 32; x++) {
                    yGenerators.get(x).addStop(new GradientWidget.GradientStop<>(keyframe.position, row[x]));
                }
            }

            for (int x = 0; x < 32; x++) {
                yGenerators.get(x).generate();
            }

            for (int x = 0; x < 32; x++) {
                for (int y = 0; y < 32; y++) {
                    cache.getImage().setColor(x, (y+32+8)%32, yGenerators.get(x).get(y).packABGR());
                }
            }

            cache.upload();
        }

        context.drawTexture(cacheId, 450, 10, 0, 0, 64, 64, 64, 64);

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
