package com.qendolin.betterclouds.gui;

import com.google.common.collect.ImmutableList;
import com.qendolin.betterclouds.gui.color.GammaRgbColor;
import com.qendolin.betterclouds.gui.color.IColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ColorInputWidget extends TextFieldWidget {

    public Consumer<GammaRgbColor> onChange = null;

    protected static final Style POUND_STYLE = Style.EMPTY.withColor(Formatting.GRAY);
    protected static final Style RED_STYLE = Style.EMPTY.withColor(Formatting.RED);
    protected static final Style GREEN_STYLE = Style.EMPTY.withColor(Formatting.GREEN);
    protected static final Style BLUE_STYLE = Style.EMPTY.withColor(Formatting.BLUE);
    protected static final Style GHOST_STYLE = Style.EMPTY.withColor(Formatting.DARK_GRAY);

    protected static final Style[] CHAR_STYLES = new Style[]{POUND_STYLE, RED_STYLE, RED_STYLE, GREEN_STYLE, GREEN_STYLE, BLUE_STYLE, BLUE_STYLE};

    protected String prevChangeValue;
    protected String ghost;
    protected Text ghostText;
    private final TextRenderer textRenderer;

    public ColorInputWidget(Bounds bounds) {
        super(MinecraftClient.getInstance().textRenderer, bounds.x(), bounds.y(), bounds.width(), bounds.height(), Text.empty());
        textRenderer = MinecraftClient.getInstance().textRenderer;
        setTextPredicate(ColorInputWidget::isValidHexColor);
        setChangedListener(this::onChange);
        setRenderTextProvider(this::renderText);
        setDrawsBackground(false);
        setMaxLength(7);
        setColor(null);
    }

    protected OrderedText renderText(String s, int firstChar) {
        if(s.isEmpty()) return OrderedText.empty();

        MutableText text = Text.empty();

        for (int i = 0; i < s.length(); i++) {
            String ch = String.valueOf(s.charAt(i));
            text.append(Text.literal(ch).setStyle(CHAR_STYLES[firstChar+i]));
        }

        return text.asOrderedText();
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        if(isVisible()) {
            if(ghost.startsWith(getText())) {
                context.drawTextWithShadow(this.textRenderer, this.ghostText, this.getX(), this.getY(), Colors.WHITE);
            }
        }
        super.renderButton(context, mouseX, mouseY, delta);
    }

    public void setColor(IColor<?, ?> color) {
        if(color == null) {
            color = new GammaRgbColor(0, 0, 0, 1);
        }
        GammaRgbColor srgb = color.to(GammaRgbColor.class);
        srgb.toGamut();
        String text = "#" + srgb.toHex().substring(0, 6);
        setText(text);
        ghost = text;
        ghostText = Text.literal(ghost).setStyle(GHOST_STYLE);
    }

    private void onChange(String s) {
        if(Objects.equals(s, prevChangeValue)) return;
        prevChangeValue = s;
        GammaRgbColor color = parse(s);
        if(color != null && onChange != null) {
            onChange.accept(color);
        }
    }

    private GammaRgbColor parse(String s) {
        if(s.length() != 7) return null;
        try {
            int r = Integer.parseInt(s.substring(1,3), 16);
            int g = Integer.parseInt(s.substring(3,5), 16);
            int b = Integer.parseInt(s.substring(5,7), 16);

            return new GammaRgbColor(r / 255f, g / 255f, b / 255f);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    public void write(String text) {
        text = text.toLowerCase();
        if (text.startsWith("0x")) text = text.substring(2);
        else if (text.startsWith("#")) text = text.substring(1);
        boolean allSelected = getSelectedText().startsWith("#");
        if(allSelected) text = "#" + text;
        super.write(text);
    }

    protected static boolean isValidHexColor(String s) {
        if (s.length() > 7) return false;
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if(i == 0 && ch != '#') return false;
            boolean isHex = ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f'));
            if(i != 0 && !isHex) return false;
        }
        return true;
    }
}
