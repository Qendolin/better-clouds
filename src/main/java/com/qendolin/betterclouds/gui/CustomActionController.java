package com.qendolin.betterclouds.gui;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ActionController;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class CustomActionController extends ActionController {
    public CustomActionController(ButtonOption option) {
        super(option, Text.of(null));
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
        return new CustomActionControllerElement(this, screen, widgetDimension);
    }

    public static class CustomActionControllerElement extends ActionControllerElement {

        public CustomActionControllerElement(ActionController control, YACLScreen screen, Dimension<Integer> dim) {
            super(control, screen, dim);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            hovered = isMouseOver(mouseX, mouseY);

            Text name = control.option().changed() ? modifiedOptionName : control.option().name();

            drawButtonRect(context, getDimension().x(), getDimension().y(), getDimension().xLimit(), getDimension().yLimit(), isHovered(), isAvailable());
            context.getMatrices().push();
            context.getMatrices().translate(getDimension().x() + getDimension().width() / 2f - textRenderer.getWidth(name) / 2f, getTextY(), 0);
            context.drawTextWithShadow(textRenderer, name, 0, 0, getValueColor());
            context.getMatrices().pop();

            if (isHovered()) {
                drawHoveredControl(context, mouseX, mouseY, delta);
            }
        }

        @Override
        protected void drawValueText(DrawContext context, int mouseX, int mouseY, float delta) {

        }
    }
}
