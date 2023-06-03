package com.qendolin.betterclouds.gui;

import dev.isxander.yacl.api.ButtonOption;
import dev.isxander.yacl.api.utils.Dimension;
import dev.isxander.yacl.gui.AbstractWidget;
import dev.isxander.yacl.gui.YACLScreen;
import dev.isxander.yacl.gui.controllers.ActionController;
import net.minecraft.client.util.math.MatrixStack;
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
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            hovered = isMouseOver(mouseX, mouseY);

            Text name = control.option().changed() ? modifiedOptionName : control.option().name();

            drawButtonRect(matrices, getDimension().x(), getDimension().y(), getDimension().xLimit(), getDimension().yLimit(), isHovered(), isAvailable());
            matrices.push();
            matrices.translate(getDimension().x() + getDimension().width() / 2f - textRenderer.getWidth(name) / 2f, getTextY(), 0);
            textRenderer.drawWithShadow(matrices, name, 0, 0, getValueColor());
            matrices.pop();

            if (isHovered()) {
                drawHoveredControl(matrices, mouseX, mouseY, delta);
            }
        }

        @Override
        protected void drawValueText(MatrixStack matrices, int mouseX, int mouseY, float delta) {

        }
    }
}
