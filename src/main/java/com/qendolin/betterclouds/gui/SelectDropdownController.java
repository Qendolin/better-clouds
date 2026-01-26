package com.qendolin.betterclouds.gui;

//? if >=1.21.11 {

import com.google.common.collect.ImmutableList;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ControllerPopupWidget;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import dev.isxander.yacl3.gui.utils.GuiUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class SelectDropdownController<T> implements Controller<Integer> {

    private final Option<Integer> option;
    private final BiFunction<Integer, T, Text> valueFormatter;
    private List<T> values;
    private final List<Text> formattedValues;

    private final List<T> refValues;

    public SelectDropdownController(Option<Integer> option, List<T> values, BiFunction<Integer, T, Text> valueFormatter) {
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
    public Text formatValue() {
        int index = option.pendingValue();
        return formatValueAtIndex(index);
    }

    public void updateValues() {
        // Create a snapshot of the current state of refValues
        this.values = ImmutableList.copyOf(refValues);

        this.formattedValues.clear();
        for (int i = 0; i < values.size(); i++) {
            formattedValues.add(valueFormatter.apply(i, values.get(i)));
        }

        // Ensure the selected index is still valid
        int current = option.pendingValue();
        if (current >= values.size() && !values.isEmpty()) {
            option.requestSet(0);
        }
    }

    /**
     * Helper method to format a specific index, unrelated to the current pending value.
     */
    public Text formatValueAtIndex(int index) {
        if (index >= 0 && index < formattedValues.size()) {
            return formattedValues.get(index);
        }
        return Text.literal("Invalid");
    }

    public int getValueCount() {
        return values.size();
    }

    public void cycle(int direction) {
        int count = values.size();
        if (count == 0) return;

        int index = option.pendingValue();
        if (direction > 0) {
            index = (index + 1) % count;
        } else if (direction < 0) {
            index = index - 1;
            if (index < 0) index = count - 1;
        }
        option.requestSet(index);
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
        return new SelectDropdownElement<>(this, screen, widgetDimension);
    }

    // ============================================================================================
    // THE ELEMENT (The widget displayed in the option list)
    // ============================================================================================

    public static class SelectDropdownElement<T> extends ControllerWidget<SelectDropdownController<T>> {
        private SelectDropdownPopup<T> popupWidget;
        private boolean popupVisible = false;

        public SelectDropdownElement(SelectDropdownController<T> control, YACLScreen screen, Dimension<Integer> dim) {
            super(control, screen, dim);
        }

        @Override
        protected void drawValueText(DrawContext context, int mouseX, int mouseY, float delta) {
            Text valueText = control.formatValue();

            // Calculate text position
            int textWidth = textRenderer.getWidth(valueText);
            int x = getDimension().xLimit() - getXPadding() - textWidth;
            int y = getDimension().y() + (getDimension().height() - textRenderer.fontHeight) / 2;

            // Shorten string if it overlaps with label
            int maxWidth = getDimension().width() - getControlWidth() - getXPadding();
            String shortened = GuiUtils.shortenString(valueText.getString(), textRenderer, maxWidth, "...");

            context.drawTextWithShadow(textRenderer, shortened, x, y, getValueColor());
        }

        @Override
        protected int getUnhoveredControlWidth() {
            return textRenderer.getWidth(control.option().changed() ? modifiedOptionName : control.option().name());
        }

        @Override
        protected int getHoveredControlWidth() {
            return getUnhoveredControlWidth();
        }

        @Override
        public boolean onMouseClicked(double mouseX, double mouseY, int button) {
            if (!isMouseOver(mouseX, mouseY) || !isAvailable()) return false;

            if (button == 0) { // Left Click
                togglePopup();
                playDownSound();
                return true;
            } else if (button == 1) { // Right Click
                control.cycle(1);
                playDownSound();
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (isMouseOver(mouseX, mouseY) && isAvailable()) {
                if (verticalAmount != 0) {
                    control.cycle(verticalAmount > 0 ? -1 : 1);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
            if (!isFocused()) return false;

            if (keyCode == InputUtil.GLFW_KEY_ENTER || keyCode == InputUtil.GLFW_KEY_SPACE || keyCode == InputUtil.GLFW_KEY_KP_ENTER) {
                togglePopup();
                return true;
            }
            if (keyCode == InputUtil.GLFW_KEY_LEFT) {
                control.cycle(-1);
                return true;
            }
            if (keyCode == InputUtil.GLFW_KEY_RIGHT) {
                control.cycle(1);
                return true;
            }

            return false;
        }

        private void togglePopup() {
            if (popupVisible) {
                closePopup();
            } else {
                openPopup();
            }
        }

        public void openPopup() {
            if (popupVisible) return;

            this.popupVisible = true;
            this.popupWidget = new SelectDropdownPopup<>(control, screen, getDimension(), this);
            screen.addPopupControllerWidget(this.popupWidget);
        }

        public void closePopup() {
            if (!popupVisible) return;

            screen.clearPopupControllerWidget();
            removePopupWidget();
        }

        public void removePopupWidget() {
            this.popupVisible = false;
            this.popupWidget = null;
        }
    }

    // ============================================================================================
    // THE POPUP (The floating list overlay)
    // ============================================================================================

    public static class SelectDropdownPopup<T> extends ControllerPopupWidget<SelectDropdownController<T>> {
        private static final int MAX_VISIBLE_ITEMS = 7;
        private static final int ITEM_PADDING = 4;

        // We explicitly store the typed controller reference to avoid 'capture of ?' errors
        // when accessing methods specific to SelectDropdownController or generic Options.
        private final SelectDropdownController<T> dropdownController;
        private final SelectDropdownElement<T> parentElement;

        private int firstVisibleIndex = 0;
        private final int itemHeight;
        private Dimension<Integer> popupDimension;

        public SelectDropdownPopup(SelectDropdownController<T> control, YACLScreen screen, Dimension<Integer> dim, SelectDropdownElement<T> parentElement) {
            super(control, screen, dim, parentElement);
            this.dropdownController = control;
            this.parentElement = parentElement;
            this.itemHeight = textRenderer.fontHeight + ITEM_PADDING;

            calculateDimensions(dim);
            centerOnSelection();
        }

        private void calculateDimensions(Dimension<Integer> anchorDim) {
            int visibleCount = Math.min(MAX_VISIBLE_ITEMS, dropdownController.getValueCount());
            int popupHeight = visibleCount * itemHeight + 2; // +2 for border

            // Position below the element, or flip up if near bottom
            int popupY = anchorDim.yLimit() + 1;
            if (popupY + popupHeight > screen.height) {
                popupY = anchorDim.y() - popupHeight - 1;
            }

            this.popupDimension = Dimension.ofInt(
                anchorDim.x(),
                popupY,
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
            if (firstVisibleIndex < 0) firstVisibleIndex = 0;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            if (dropdownController.getValueCount() == 0) return;

            GuiUtils.pushPose(context);

            // Background
            context.fill(popupDimension.x(), popupDimension.y(), popupDimension.xLimit(), popupDimension.yLimit(), 0xFF202020);

            // Border (Manual drawing as YACL version differences exist for drawBorder)
            int borderColor = 0xFFAAAAAA;
            int x = popupDimension.x();
            int y = popupDimension.y();
            int w = popupDimension.width();
            int h = popupDimension.height();
            context.fill(x, y, x + w, y + 1, borderColor); // Top
            context.fill(x, y + h - 1, x + w, y + h, borderColor); // Bottom
            context.fill(x, y, x + 1, y + h, borderColor); // Left
            context.fill(x + w - 1, y, x + w, y + h, borderColor); // Right

            // Items
            int count = Math.min(MAX_VISIBLE_ITEMS, dropdownController.getValueCount());
            int startY = popupDimension.y() + 1;
            int currentSelectedIndex = dropdownController.option().pendingValue();

            for (int i = 0; i < count; i++) {
                int itemIndex = firstVisibleIndex + i;
                if (itemIndex >= dropdownController.getValueCount()) break;

                int itemY = startY + (i * itemHeight);
                boolean isHovered = isMouseOverItem(mouseX, mouseY, itemY);
                boolean isSelected = (itemIndex == currentSelectedIndex);

                // Selection / Hover Highlight
                if (isSelected) {
                    context.fill(popupDimension.x() + 1, itemY, popupDimension.xLimit() - 1, itemY + itemHeight, 0xFF404040);
                } else if (isHovered) {
                    context.fill(popupDimension.x() + 1, itemY, popupDimension.xLimit() - 1, itemY + itemHeight, 0xFF303030);
                }

                // Text
                Text text = dropdownController.formatValueAtIndex(itemIndex);
                int textX = popupDimension.xLimit() - textRenderer.getWidth(text) - 4; // Right align
                int textY = itemY + (itemHeight - textRenderer.fontHeight) / 2;

                context.drawText(textRenderer, text, textX, textY, 0xFFFFFFFF, true);
            }

            GuiUtils.popPose(context);
        }

        private boolean isMouseOverItem(double mouseX, double mouseY, int itemY) {
            return mouseX >= popupDimension.x() && mouseX <= popupDimension.xLimit() &&
                   mouseY >= itemY && mouseY < itemY + itemHeight;
        }

        @Override
        public boolean onMouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) return false;

            if (popupDimension.isPointInside((int)mouseX, (int)mouseY)) {
                // Determine which item was clicked
                int relativeY = (int)mouseY - (popupDimension.y() + 1);
                int clickedIndex = (relativeY / itemHeight) + firstVisibleIndex;

                if (clickedIndex >= 0 && clickedIndex < dropdownController.getValueCount()) {
                    dropdownController.option().requestSet(clickedIndex);
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0f));
                    close();
                    return true;
                }
            } else {
                // Clicked outside the popup box
                close();
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (popupDimension.isPointInside((int)mouseX, (int)mouseY)) {
                if (verticalAmount > 0) {
                    if (firstVisibleIndex > 0) firstVisibleIndex--;
                } else if (verticalAmount < 0) {
                    if (firstVisibleIndex < dropdownController.getValueCount() - MAX_VISIBLE_ITEMS) firstVisibleIndex++;
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return popupDimension.isPointInside((int)mouseX, (int)mouseY);
        }

        @Override
        public void close() {
            parentElement.removePopupWidget();
        }

        @Override
        public Text popupTitle() {
            return Text.empty();
        }
    }
}

//?}