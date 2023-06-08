package com.qendolin.betterclouds.gui;

import com.google.common.collect.ImmutableList;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import dev.isxander.yacl3.gui.utils.GuiUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class SelectController<T> implements Controller<Integer> {

    private final Option<Integer> option;
    private final BiFunction<Integer, T, Text> valueFormatter;
    private List<T> values;
    private final List<T> refValues;
    private List<Text> formattedValues;

    public SelectController(Option<Integer> option, List<T> values, BiFunction<Integer, T, Text> valueFormatter) {
        this.option = option;
        this.valueFormatter = valueFormatter;
        this.refValues = values;
        updateValues();
    }

    public void updateValues() {
        this.values = ImmutableList.copyOf(refValues);
        this.formattedValues = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            formattedValues.add(valueFormatter.apply(i, values.get(i)));
        }
    }

    public void cycle(int direction) {
        int index = option.pendingValue();
        if (direction > 0) {
            index = (index + 1) % values.size();
        } else if (direction < 0) {
            index = index <= 0 ? values.size() - 1 : index - 1;
        }
        option.requestSet(index);
    }

    public List<Text> formatValues() {
        formattedValues.set(getSelectedIndex(), formatValue());
        return ImmutableList.copyOf(formattedValues);
    }

    public int getSelectedIndex() {
        return option().pendingValue();
    }

    @Override
    public Text formatValue() {
        int index = option.pendingValue();
        return valueFormatter.apply(index, values.get(index));
    }

    @Override
    public Option<Integer> option() {
        return option;
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
        return new SelectElement<>(this, screen, widgetDimension);
    }

    public static class SelectElement<T> extends ControllerWidget<SelectController<T>> {

        private final int ARROW_SPACE = 8;
        private Dimension<Integer> expandedBounds;
        private Dimension<Integer> arrowBounds;
        private boolean mouseInteracted;
        private long hoveringStart = 0;
        protected static final Text UP_ARROW = Text.literal("▲");
        protected static final Text DOWN_ARROW = Text.literal("▼");

        public SelectElement(SelectController<T> control, YACLScreen screen, Dimension<Integer> dim) {
            super(control, screen, dim);
            updateValues();
        }

        public void updateValues() {
            updateExpandedBounds();
        }

        protected void updateExpandedBounds() {
            Dimension<Integer> dim = getDimension();
            int lines = getLineCount();
            int lineHeight = getLineHeight();
            int yStart = dim.yLimit() - 1;
            int ySpan = lines * lineHeight + 1;
            expandedBounds = Dimension.ofInt(dim.x(), yStart, dim.width(), ySpan);
        }

        protected int getLineCount() {
            return Math.min(control.formatValues().size(), 5);
        }

        protected int getLineHeight() {
            return textRenderer.fontHeight + getLinePadding();
        }

        protected int getLinePadding() {
            return 2;
        }

        @Override
        public void setDimension(Dimension<Integer> dim) {
            super.setDimension(dim);
            updateExpandedBounds();
            updateArrowBounds();
        }

        protected void updateArrowBounds() {
            Dimension<Integer> dim = getDimension();
            arrowBounds = Dimension.ofInt(dim.xLimit() - ARROW_SPACE - 2 * getXPadding(), dim.y(), ARROW_SPACE + 2 * getXPadding(), dim.height());
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            if (isHovered() && hoveringStart == 0) {
                hoveringStart = Util.getEpochTimeMs();
            } else if (!isHovered()) {
                hoveringStart = 0;
            }
            if (mouseInteracted && !isHovered()) mouseInteracted = false;

            drawList(context);
        }

        @Override
        protected void drawHoveredControl(DrawContext context, int mouseX, int mouseY, float delta) {
            Dimension<Integer> dim = getDimension();
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            int arrowWidth = textRenderer.getWidth(UP_ARROW);
            matrices.translate(getDimension().xLimit() - getXPadding() - ARROW_SPACE / 2f, dim.y() + dim.height() / 2f, 0);
            matrices.scale(1.5f, 1f, 1);
            int hoveredArrow = getHoveredArrow(mouseX, mouseY);
            context.drawText(textRenderer, UP_ARROW, -arrowWidth / 2, -textRenderer.fontHeight + 1, 0xff404040, false);
            context.drawText(textRenderer, DOWN_ARROW, -arrowWidth / 2, 1, 0xff404040, false);
            context.drawText(textRenderer, UP_ARROW, -arrowWidth / 2, -textRenderer.fontHeight + 2, hoveredArrow == -1 ? -1 : 0xffc0c0c0, false);
            context.drawText(textRenderer, DOWN_ARROW, -arrowWidth / 2, 0, hoveredArrow == 1 ? -1 : 0xffc0c0c0, false);
            matrices.pop();
        }

        protected int getHoveredArrow(int mouseX, int mouseY) {
            if (!arrowBounds.isPointInside(mouseX, mouseY)) return 0;
            boolean upper = ((mouseY - arrowBounds.y()) / (float) arrowBounds.height()) < 0.5f;
            return upper ? -1 : 1;
        }

        @Override
        protected void drawValueText(DrawContext context, int mouseX, int mouseY, float delta) {
            context.getMatrices().push();
            if (isHovered())
                context.getMatrices().translate(-ARROW_SPACE - getXPadding(), 0, 0);
            super.drawValueText(context, mouseX, mouseY, delta);
            context.getMatrices().pop();
        }

        protected void drawList(DrawContext context) {
            if ((!isMouseInteracted() && !isFocused()) || !isAvailable()) return;

            context.getMatrices().push();
            context.getMatrices().translate(0, 0, 100);

            List<Text> values = control.formatValues();
            Dimension<Integer> dim = getExpandedBounds();

            int padding = getLinePadding();
            int lines = getLineCount();
            int lineHeight = getLineHeight();
            int selected = control.getSelectedIndex();

            int indexFrom = MathHelper.clamp(selected - lines / 2, 0, values.size() - lines);

            context.fill(dim.x() + 1, dim.y() + 1, dim.xLimit() - 1, dim.yLimit() - 1, 0xb0000000);
            drawOutline(context, dim.x(), dim.y(), dim.xLimit(), dim.yLimit(), 1, -1);

            for (int line = 0; line < lines; line++) {
                int i = indexFrom + line;
                Text text = values.get(i);
                int x = dim.xLimit() - textRenderer.getWidth(text) - getXPadding();
                int y = dim.y() + padding + lineHeight * line;
                if (selected == i) {
                    context.fill(dim.x(), y - padding, dim.xLimit(), y + lineHeight - 1, 0x80ffffff);
                }
                context.drawTextWithShadow(textRenderer, text, x, y, getValueColor());
            }
            context.getMatrices().pop();
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean isMouseInteracted() {
            return mouseInteracted;
        }

        public void setMouseInteracted(boolean mouseInteracted) {
            this.mouseInteracted = mouseInteracted;
        }

        public Dimension<Integer> getExpandedBounds() {
            return expandedBounds;
        }

        @Override
        protected Text getValueText() {
            Text valueText = control.formatValue();
            int maxWidth = getDimension().width() - getControlWidth() - getXPadding() - 7 - ARROW_SPACE;
            String shortened = GuiUtils.shortenString(valueText.getString(), textRenderer, maxWidth, "...");
            return Text.literal(shortened)
                .setStyle(valueText.getStyle());
        }

        @Override
        protected int getHoveredControlWidth() {
            return getUnhoveredControlWidth();
        }

        @Override
        protected int getUnhoveredControlWidth() {
            return textRenderer.getWidth(control.option().changed() ? modifiedOptionName : control.option().name());
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!isMouseOver(mouseX, mouseY) || (button != 0 && button != 1) || !isAvailable())
                return false;

            int hoveredArrow = getHoveredArrow((int) mouseX, (int) mouseY);
            if (hoveredArrow != 0) {
                cycle(hoveredArrow);
                setMouseInteracted(true);
                return true;
            }

            playDownSound();
            setMouseInteracted(!isMouseInteracted());
            return true;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            Dimension<Integer> dim = getDimension();
            if (dim == null) return false;

            if (dim.isPointInside((int) mouseX, (int) mouseY)) return true;
            Dimension<Integer> expanded = getExpandedBounds();
            //noinspection RedundantIfStatement
            if (isFocused() && expanded.isPointInside((int) mouseX, (int) mouseY)) return true;
            return false;
        }

        public void cycle(int direction) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), 2.0F, 0.1f));
            control.cycle(direction);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
            if (!isMouseOver(mouseX, mouseY) || !isAvailable()) return false;
            if (hoveringStart == 0 || Util.getEpochTimeMs() - hoveringStart <= 100) return false;
            if (amount == 0) return false;
            cycle(amount > 0 ? -1 : 1);
            setMouseInteracted(true);
            return true;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (!focused)
                return false;

            switch (keyCode) {
                case InputUtil.GLFW_KEY_LEFT:
                    cycle(-1);
                    break;
                case InputUtil.GLFW_KEY_RIGHT:
                    cycle(1);
                    break;
                default:
                    return false;
            }

            setFocused(true);
            return true;
        }
    }
}
