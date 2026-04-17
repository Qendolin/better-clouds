package com.qendolin.betterclouds.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ControllerPopupWidget;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import dev.isxander.yacl3.gui.utils.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class SelectDropdownController<T> implements Controller<Integer> {

    private final Option<Integer> option;
    private final BiFunction<Integer, T, Component> valueFormatter;
    private final List<Component> formattedValues;
    private final List<T> refValues;

    private List<T> values;

    public SelectDropdownController(Option<Integer> option, List<T> values, BiFunction<Integer, T, Component> valueFormatter) {
        this.option = option;
        this.refValues = values;
        this.valueFormatter = valueFormatter;
        this.formattedValues = new ArrayList<>();
        updateValues();
    }

    @Override
    public Option<Integer> option() {
        return option;
    }

    @Override
    public Component formatValue() {
        return formatValueAtIndex(option.pendingValue());
    }

    public void updateValues() {
        values = ImmutableList.copyOf(refValues);

        formattedValues.clear();
        for (int i = 0; i < values.size(); i++) {
            formattedValues.add(valueFormatter.apply(i, values.get(i)));
        }

        int current = option.pendingValue();
        if (current < 0 || current >= values.size()) {
            if (!values.isEmpty()) {
                option.requestSet(0);
            }
        }
    }

    public Component formatValueAtIndex(int index) {
        if (index >= 0 && index < formattedValues.size()) {
            return formattedValues.get(index);
        }

        return Component.literal("Invalid");
    }

    public int getValueCount() {
        return values.size();
    }

    public void cycle(int direction) {
        int count = values.size();
        if (count == 0) {
            return;
        }

        int index = Mth.clamp(option.pendingValue(), 0, count - 1);
        index = (index + direction + count) % count;
        option.requestSet(index);
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
        return new SelectDropdownElement<>(this, screen, widgetDimension);
    }

    public static class SelectDropdownElement<T> extends ControllerWidget<SelectDropdownController<T>> {
        private SelectDropdownPopup<T> popupWidget;
        private boolean popupVisible = false;

        public SelectDropdownElement(SelectDropdownController<T> control, YACLScreen screen, Dimension<Integer> dim) {
            super(control, screen, dim);
        }

        @Override
        protected Component getValueText() {
            Component valueText = control.formatValue();
            int maxWidth = Math.max(0, getDimension().width() - getControlWidth() - getXPadding());
            String shortened = GuiUtils.shortenString(valueText.getString(), textRenderer, maxWidth, "...");

            return Component.literal(shortened).setStyle(valueText.getStyle());
        }

        @Override
        protected int getUnhoveredControlWidth() {
            return textRenderer.width(control.option().changed() ? modifiedOptionName : control.option().name());
        }

        @Override
        protected int getHoveredControlWidth() {
            return getUnhoveredControlWidth();
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (!isMouseOver(event.x(), event.y()) || !isAvailable()) {
                return false;
            }

            if (event.button() == 0) {
                togglePopup();
                playDownSound();
                return true;
            }

            if (event.button() == 1) {
                control.cycle(1);
                playDownSound();
                return true;
            }

            return false;
        }

        @Override
        public boolean keyPressed(@NonNull KeyEvent event) {
            if (!isFocused()) {
                return false;
            }

            return switch (event.key()) {
                case InputConstants.KEY_RETURN, InputConstants.KEY_SPACE, InputConstants.KEY_NUMPADENTER -> {
                    togglePopup();
                    yield true;
                }
                case InputConstants.KEY_LEFT -> {
                    control.cycle(-1);
                    yield true;
                }
                case InputConstants.KEY_RIGHT -> {
                    control.cycle(1);
                    yield true;
                }
                default -> false;
            };
        }

        @Override
        public void unfocus() {
            super.unfocus();
            closePopup();
        }

        private void togglePopup() {
            if (popupVisible) {
                closePopup();
            } else {
                openPopup();
            }
        }

        public void openPopup() {
            if (popupVisible || control.getValueCount() == 0) {
                return;
            }

            popupVisible = true;
            popupWidget = new SelectDropdownPopup<>(control, screen, getDimension(), this);
            screen.addPopupControllerWidget(popupWidget);
        }

        public void closePopup() {
            if (!popupVisible || popupWidget == null) {
                return;
            }

            popupWidget.close();
        }

        public void removePopupWidget() {
            popupVisible = false;
            popupWidget = null;
        }
    }

    public static class SelectDropdownPopup<T> extends ControllerPopupWidget<SelectDropdownController<T>> {
        private static final int MAX_VISIBLE_ITEMS = 7;
        private static final int ITEM_PADDING = 4;

        private final SelectDropdownController<T> dropdownController;
        private final SelectDropdownElement<T> parentElement;
        private final int itemHeight;
        private final Dimension<Integer> popupDimension;
        private int firstVisibleIndex = 0;

        public SelectDropdownPopup(
                SelectDropdownController<T> control,
                YACLScreen screen,
                Dimension<Integer> dim,
                SelectDropdownElement<T> parentElement
        ) {
            super(control, screen, dim, parentElement);
            this.dropdownController = control;
            this.parentElement = parentElement;
            this.itemHeight = textRenderer.lineHeight + ITEM_PADDING;
            this.popupDimension = calculateDimensions(dim);
            centerOnSelection();
        }

        private Dimension<Integer> calculateDimensions(Dimension<Integer> anchorDim) {
            int visibleCount = Math.clamp(dropdownController.getValueCount(), 1, MAX_VISIBLE_ITEMS);
            int popupHeight = visibleCount * itemHeight + 2;
            int popupY = anchorDim.yLimit() + 1;
            int screenHeight = client.getWindow().getGuiScaledHeight();

            if (popupY + popupHeight > screenHeight) {
                popupY = anchorDim.y() - popupHeight - 1;
            }

            return Dimension.ofInt(
                    anchorDim.x(),
                    Math.max(0, popupY),
                    anchorDim.width(),
                    popupHeight
            );
        }

        private void centerOnSelection() {
            int selected = dropdownController.option().pendingValue();
            int halfView = MAX_VISIBLE_ITEMS / 2;

            if (selected > halfView) {
                firstVisibleIndex = Math.min(selected - halfView, dropdownController.getValueCount() - MAX_VISIBLE_ITEMS);
            } else {
                firstVisibleIndex = 0;
            }

            if (firstVisibleIndex < 0) {
                firstVisibleIndex = 0;
            }
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
            if (dropdownController.getValueCount() == 0) {
                return;
            }

            hovered = isMouseOver(mouseX, mouseY);

            int x = popupDimension.x();
            int y = popupDimension.y();
            int xLimit = popupDimension.xLimit();
            int yLimit = popupDimension.yLimit();

            context.fill(x, y, xLimit, yLimit, 0xFF202020);
            context.outline(x, y, popupDimension.width(), popupDimension.height(), 0xFFAAAAAA);

            int count = Math.min(MAX_VISIBLE_ITEMS, dropdownController.getValueCount());
            int startY = y + 1;
            int selectedIndex = dropdownController.option().pendingValue();
            int maxTextWidth = Math.max(0, popupDimension.width() - 8);

            for (int i = 0; i < count; i++) {
                int itemIndex = firstVisibleIndex + i;
                if (itemIndex >= dropdownController.getValueCount()) {
                    break;
                }

                int itemY = startY + (i * itemHeight);
                boolean hoveredItem = isMouseOverItem(mouseX, mouseY, itemY);
                boolean selectedItem = itemIndex == selectedIndex;

                if (selectedItem) {
                    context.fill(x + 1, itemY, xLimit - 1, itemY + itemHeight, 0xFF404040);
                } else if (hoveredItem) {
                    context.fill(x + 1, itemY, xLimit - 1, itemY + itemHeight, 0xFF303030);
                }

                Component text = dropdownController.formatValueAtIndex(itemIndex);
                String shortened = GuiUtils.shortenString(text.getString(), textRenderer, maxTextWidth, "...");
                Component renderedText = Component.literal(shortened).setStyle(text.getStyle());
                int textX = xLimit - textRenderer.width(renderedText) - 4;
                int textY = itemY + (itemHeight - textRenderer.lineHeight) / 2;

                context.text(textRenderer, renderedText, textX, textY, 0xFFFFFFFF);
            }
        }

        private boolean isMouseOverItem(double mouseX, double mouseY, int itemY) {
            return mouseX >= popupDimension.x()
                    && mouseX <= popupDimension.xLimit()
                    && mouseY >= itemY
                    && mouseY < itemY + itemHeight;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (event.button() != 0) {
                return false;
            }

            double mouseX = event.x();
            double mouseY = event.y();

            if (popupDimension.isPointInside((int) mouseX, (int) mouseY)) {
                int relativeY = (int) mouseY - (popupDimension.y() + 1);
                int clickedIndex = (relativeY / itemHeight) + firstVisibleIndex;

                if (clickedIndex >= 0 && clickedIndex < dropdownController.getValueCount()) {
                    dropdownController.option().requestSet(clickedIndex);
                    playDownSound();
                }

                close();
                return true;
            }

            close();
            return true;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (!popupDimension.isPointInside((int) mouseX, (int) mouseY)) {
                return false;
            }

            int maxFirstVisible = Math.max(0, dropdownController.getValueCount() - MAX_VISIBLE_ITEMS);
            if (verticalAmount > 0 && firstVisibleIndex > 0) {
                firstVisibleIndex--;
            } else if (verticalAmount < 0 && firstVisibleIndex < maxFirstVisible) {
                firstVisibleIndex++;
            }

            return verticalAmount != 0;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return popupDimension.isPointInside((int) mouseX, (int) mouseY);
        }

        @Override
        public void close() {
            parentElement.removePopupWidget();
            screen.popupControllerVisible = false;
            if (screen.currentPopupController == this) {
                screen.currentPopupController = null;
            }
            if (Minecraft.getInstance().screen != screen) {
                Minecraft.getInstance().setScreen(screen);
            }
        }

        @Override
        public Component popupTitle() {
            return Component.empty();
        }
    }
}
