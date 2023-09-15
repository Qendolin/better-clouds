package com.qendolin.betterclouds.gui;

import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.gui.color.GammaRgbColor;
import com.qendolin.betterclouds.gui.color.IColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class ColorSwatch implements Drawable {
    private static final Identifier ICONS_TEXTURE = new Identifier(Main.MODID, "textures/gui/betterclouds/gui_icons.png");


    public final Bounds bounds;
    protected IColor<?, ?> color;
    protected GammaRgbColor srgb;

    protected MutableText redValueText;
    protected MutableText greenValueText;
    protected MutableText blueValueText;
    protected final Text commaText = Text.literal(",");
    protected MutableText hexValueText;

    public ColorSwatch(Bounds bounds) {
        this.bounds = bounds;
        setColor(null);
    }

    public void setColor(IColor<?, ?> color) {
        if(color == null) {
            this.color = null;
            this.srgb = new GammaRgbColor(0, 0, 0, 1);
        } else {
            this.color = color.copy();
            srgb = color.to(GammaRgbColor.class);
            srgb.toGamut();
        }
        updateText();
    }

    protected void updateText() {
        int r = Math.round(srgb.red * 0xff);
        int g = Math.round(srgb.green * 0xff);
        int b = Math.round(srgb.blue * 0xff);
        redValueText = Text.literal(String.format("%3d", r));
        greenValueText = Text.literal(String.format("%3d", g));
        blueValueText = Text.literal(String.format("%3d", b));

        hexValueText = Text.literal("#").styled(style -> style.withColor(Formatting.GRAY))
            .append(Text.literal(String.format("%02x", r)).styled(style -> style.withColor(Formatting.RED)))
            .append(Text.literal(String.format("%02x", g)).styled(style -> style.withColor(Formatting.GREEN)))
            .append(Text.literal(String.format("%02x", b)).styled(style -> style.withColor(Formatting.BLUE)));
    }

    public IColor<?, ?> color() {
        return color;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTexture(ICONS_TEXTURE, bounds.x(), bounds.y(), 0, 32, 28, 20, 64, 64);

        context.fill(bounds.x()+3, bounds.y()+3, bounds.x()+17, bounds.y()+17, srgb.pack());

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

//        int textY = bounds.y() + 10 - textRenderer.fontHeight/2;
//        int commaX = bounds.x() + 28 + 5 + 17;
//        //noinspection DataFlowIssue
//        context.drawText(textRenderer, hexValueText, bounds.x() + 28 + 5, textY, Formatting.GRAY.getColorValue(), false);


//        //noinspection DataFlowIssue
//        context.drawText(textRenderer, redValueText, commaX - textRenderer.getWidth(redValueText) , textY, Formatting.RED.getColorValue(), false);
//        context.drawText(textRenderer, commaText, commaX, textY, Colors.WHITE, false);
//        commaX += 24;
//        //noinspection DataFlowIssue
//        context.drawText(textRenderer, greenValueText, commaX - textRenderer.getWidth(greenValueText) , textY, Formatting.GREEN.getColorValue(), false);
//        context.drawText(textRenderer, commaText, commaX, textY, Colors.WHITE, false);
//        commaX += 24;
//        //noinspection DataFlowIssue
//        context.drawText(textRenderer, blueValueText, commaX - textRenderer.getWidth(blueValueText) , textY, Formatting.BLUE.getColorValue(), false);
//        commaX += 24;
    }


}
