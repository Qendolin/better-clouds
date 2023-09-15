package com.qendolin.betterclouds.gui;

import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.gui.color.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

public class ColorPickerWidget implements Element, Drawable, Selectable {

    protected static final String LANG_KEY_COLOR_PREFIX = "betterclouds.gui.gradient.color.";

    protected static final Identifier ICONS_TEXTURE = new Identifier(Main.MODID, "textures/gui/betterclouds/gui_icons.png");

    public Consumer<XYZColor> onChanged;

    protected boolean focused = false;
    protected boolean hovered = false;

    protected ColorSpace<?> space = ColorSpace.LCH;

    protected XYZColor reference;
    protected IColor<?, ?> color;

    public final Bounds bounds;
    private final Bounds[] sliderBounds;

    private boolean dirty = true;
    private final GammaRgbColor[][] cache;
    private final boolean[][] cacheInGamut;
    private Bounds selectedSlider = null;

    public ColorPickerWidget(Bounds bounds, ColorSpace<?> space) {
        this.bounds = bounds;
        this.sliderBounds = new Bounds[4];
        for (int i = 0; i < 4; i++) {
            this.sliderBounds[i] = new Bounds(bounds).inset(0, 1, 0, 25+1).offset(0, i * 16 + 3).setHeight(10);
        }
        this.cache = new GammaRgbColor[4][sliderBounds[0].width()];
        this.cacheInGamut = new boolean[4][sliderBounds[0].width()];
        setColor(null);
        setSpace(space);
    }

    public void setSpace(ColorSpace<?> space) {
        this.space = space;
        this.reference = this.color.toXYZ();
        this.color = this.space.convert.apply(this.reference);
        this.color.toGamut();
        this.dirty = true;
    }

    public ColorSpace<?> getSpace() {
        return space;
    }

    public void setColor(IColor<?, ?> color) {
        if(color == null) {
            color = new GammaRgbColor(0, 0, 0, 1);
        }
        this.reference = color.toXYZ();
        this.color = this.space.convert.apply(this.reference);
        this.dirty = true;
    }

    public XYZColor getColor() {
        return reference;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = isMouseOver(mouseX, mouseY);

        XYZColor prev = reference;
        reference = color.toXYZ();
        if(!prev.equals(reference) && onChanged != null) onChanged.accept(reference);

        if(dirty) {
            buildCache();
        }
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        GammaRgbColor srgbColor = reference.copy().to(GammaRgbColor.class);
        srgbColor.toGamut();
        boolean bright = isBright(srgbColor);

        for (int i = 0; i < color.coordsMeta().length; i++) {
            IColor.Meta meta = color.coordsMeta()[i];
            Bounds bounds = sliderBounds[i];

            boolean sliderActive = overlapsSlider(i, mouseX, mouseY) || selectedSlider == bounds;

            context.drawText(textRenderer, Text.translatable(LANG_KEY_COLOR_PREFIX + meta.nameKey()), bounds.x() - 20, bounds.y() + 1, Colors.WHITE, true);

            context.drawBorder(bounds.x() - 1, bounds.y() - 1, bounds.width() + 2, 12, sliderActive ? Colors.WHITE : Colors.BLACK);
            context.fill(bounds.x(), bounds.y(), bounds.limitX(), bounds.limitY(), bright ? 0xff505050 : 0xffd0d0d0);
            for (int x = 0; x < bounds.width(); x++) {
                if (cacheInGamut[i][x]) {
                    context.drawVerticalLine(bounds.x() + x, bounds.y() - 1, bounds.limitY(), cache[i][x].pack());
                } else {
                    int checker = (x % 2) * 2;
                    context.drawVerticalLine(bounds.x() + x, bounds.y() - 1 + checker, bounds.limitY() - 2 + checker, cache[i][x].pack());
                }
            }

            GammaRgbColor centerColor = cache[i][bounds.width()/2].copy();
            boolean centerBright = isBright(centerColor);

            float value = color.getCoord(i) * meta.displayScale();
            // sometimes conversion results in very small negative numbers
            // that get displayed as -0 which doesn't look nice
            if(value < 0 && value > -0.0001) value = 0;
            Text valueText = Text.literal(meta.format().apply(value));
            int valueWidth = textRenderer.getWidth(valueText);
            context.drawText(textRenderer, valueText, bounds.centerX() - valueWidth/2+1, bounds.y() + 2, centerBright ? 0xffffffff : 0xff000000, false);
            context.drawText(textRenderer, valueText, bounds.centerX() - valueWidth/2, bounds.y() + 1, centerBright ? Colors.BLACK : Colors.WHITE, false);

            float pos = (color.getCoord(i) - meta.min()) / (meta.max() - meta.min());
            int x = Math.round(pos * (bounds.width() - 1));
            context.drawTexture(ICONS_TEXTURE, bounds.x()+x - 2, bounds.y() - 3, 8, 16, 32, sliderActive ? 16 : 0, 8, 16, 64, 64);
        }
    }

    private void buildCache() {
        for (int i = 0; i < color.coordsMeta().length; i++) {
            Bounds bounds = sliderBounds[i];
            IColor.Meta meta = color.coordsMeta()[i];
            IColor<?, ?> min = color.copy();
            min.setCoord(i, meta.min());
            IColor<?, ?> max = color.copy();
            max.setCoord(i, meta.max());

            for (int x = 0; x < bounds.width(); x++) {
                float f = x / (float)(bounds.width()-1);
                IColor<?, ?> mix = min.lerp(max, f, IColor.ArcMode.INCREASE);
                GammaRgbColor srgb = mix.to(GammaRgbColor.class);
                boolean in = srgb.toGamut();
                cache[i][x] = srgb;
                cacheInGamut[i][x] = in;
            }
        }

        dirty = false;
    }

    private boolean isBright(GammaRgbColor srgb) {
        float luma = (srgb.red*2+srgb.blue+srgb.green*3)/6;
        return luma > 0.5;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
        if(focused && selectedSlider == null) {
            selectedSlider = sliderBounds[0];
        } else if(!focused) {
            selectedSlider = null;
        }
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Nullable
    @Override
    public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        if (!this.isFocused()) {
            return GuiNavigationPath.of(this);
        }
        return null;
    }

    public ScreenRect getNavigationFocus() {
        return new ScreenRect(bounds.x(), bounds.y(), bounds.width(), bounds.height());
    }

    @Override
    public SelectionType getType() {
        return focused ? SelectionType.FOCUSED : hovered ? SelectionType.HOVERED : SelectionType.NONE;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return bounds.overlaps(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        if(setSliderToMousePos(mouseX, mouseY)) return true;
        selectedSlider = null;

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(selectedSlider == null) return false;
        float step = 1f/selectedSlider.width();
        if((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) step *= 10;
        if((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) step /= 10;
        if(keyCode == GLFW.GLFW_KEY_LEFT) {
            setSliderPos(getSliderPos() - step);
            return true;
        } else if(keyCode == GLFW.GLFW_KEY_RIGHT) {
            setSliderPos(getSliderPos() + step);
            return true;
        } else if(keyCode == GLFW.GLFW_KEY_UP) {
            selectedSlider = sliderBounds[Math.max(0, selectedSliderIndex() - 1)];
            return true;
        } else if(keyCode == GLFW.GLFW_KEY_DOWN) {
            selectedSlider = sliderBounds[Math.min(color.coordsMeta().length-1, selectedSliderIndex() + 1)];
            return true;
        }
        return false;
    }

    private int selectedSliderIndex() {
        if(selectedSlider == null) return -1;
        for (int i = 0; i < sliderBounds.length; i++) {
            if(selectedSlider == sliderBounds[i]) return i;
        }
        return -1;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if(button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        if(setSliderToMousePos(mouseX, mouseY)) return true;

        return false;
    }

    private boolean setSliderToMousePos(double mouseX, double mouseY) {
        for (int i = 0; i < color.coordsMeta().length; i++) {
            if(!overlapsSlider(i, mouseX, mouseY)) continue;
            IColor.Meta m = color.coordsMeta()[i];
            float pos = getMouseSliderPosition(i, mouseX);
            color.setCoord(i, Interpolate.linear(m.min(), m.max(), pos));
            selectedSlider = sliderBounds[i];
            dirty = true;
            return true;
        }

        return false;
    }

    private float getSliderPos() {
        if(selectedSlider == null) return 0;
        int index = selectedSliderIndex();
        IColor.Meta m = color.coordsMeta()[index];
        return (color.getCoord(index) - m.min()) / (m.max() - m.min());
    }

    private void setSliderPos(float pos) {
        if(selectedSlider == null) return;
        pos = MathHelper.clamp(pos, 0, 1);
        int index = selectedSliderIndex();
        IColor.Meta m = color.coordsMeta()[index];
        color.setCoord(index, Interpolate.linear(m.min(), m.max(), pos));
        dirty = true;
    }

    private boolean overlapsSlider(int i, double mouseX, double mouseY) {
        Bounds bounds = sliderBounds[i];
        bounds.expand(1, 2);
        boolean result = bounds.overlaps(mouseX, mouseY);
        bounds.inset(1, 2);
        return result;
    }

    private float getMouseSliderPosition(int i, double mouseX) {
        Bounds bounds = sliderBounds[i];
        float pos = ((float) mouseX - bounds.x()) / (bounds.width() - 1f);
        pos = MathHelper.clamp(pos, 0, 1);
        return pos;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }

    public record ColorSpace<T extends IColor<T, ?>>(Class<T> space, Function<XYZColor, T> convert, String name) {
        public static final ColorSpace<LchColor> LCH = new ColorSpace<>(LchColor.class, color -> color.to(LchColor.class), "lch");
        public static final ColorSpace<GammaRgbColor> RGB = new ColorSpace<>(GammaRgbColor.class, color -> color.to(GammaRgbColor.class), "rgb");
        public static final ColorSpace<HslColor> HSL = new ColorSpace<>(HslColor.class, color -> color.to(HslColor.class), "hsl");
    }

}
