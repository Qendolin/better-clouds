package com.qendolin.betterclouds.gui;

import com.qendolin.betterclouds.ConfigGUI;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.api.utils.MutableDimension;
import dev.isxander.yacl3.gui.*;
import dev.isxander.yacl3.gui.tab.ListHolderWidget;
import dev.isxander.yacl3.gui.tab.TabExt;
import dev.isxander.yacl3.gui.utils.GuiUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CustomCategoryTab implements TabExt {

    private final MinecraftClient client;
    private final ConfigScreen screen;
    private final ConfigCategory category;
    private final Tooltip tooltip;
    private final Supplier<ScreenRect> areaGetter;

    private ListHolderWidget<OptionListWidget> optionList;
    private final ButtonWidget saveFinishedButton;
    private final ButtonWidget cancelResetButton;
    private final ButtonWidget hideShowButton;
    private final SearchFieldWidget searchField;
    private OptionDescriptionWidget descriptionWidget;

    public CustomCategoryTab(MinecraftClient client, ConfigScreen screen, Supplier<ScreenRect> areaGetter, ConfigCategory category) {
        this.client = client;
        this.screen = screen;
        this.category = category;
        this.tooltip = Tooltip.of(category.tooltip());
        this.areaGetter = areaGetter;

        int columnWidth = screen.width / 3;
        int padding = columnWidth / 20;
        columnWidth = Math.min(columnWidth, 400);
        int paddedWidth = columnWidth - padding * 2;
        MutableDimension<Integer> actionDim = Dimension.ofInt(screen.width / 3 * 2 + screen.width / 6, screen.height - padding - 20, paddedWidth, 20);

        saveFinishedButton = ButtonWidget.builder(Text.literal("Done"), btn -> screen.finishOrSave())
            .position(actionDim.x() - actionDim.width() / 2, actionDim.y())
            .size(actionDim.width(), actionDim.height())
            .build();

        actionDim.expand(-actionDim.width() / 2 - 2, 0).move(-actionDim.width() / 2 - 2, -22);
        cancelResetButton = ButtonWidget.builder(Text.literal("Cancel"), btn -> screen.cancelOrReset())
            .position(actionDim.x() - actionDim.width() / 2, actionDim.y())
            .size(actionDim.width(), actionDim.height())
            .build();
        cancelResetButton.active = false;

        actionDim.move(actionDim.width() + 4, 0);
        hideShowButton = ButtonWidget.builder(Text.translatable(ConfigGUI.LANG_KEY_PREFIX + ".hide"),
                btn -> hideOrShow())
            .position(actionDim.x() - actionDim.width() / 2, actionDim.y())
            .size(actionDim.width(), actionDim.height())
            .build();
        hideShowButton.active = client.world != null;

        searchField = new SearchFieldWidget(
            screen,
            client.textRenderer,
            screen.width / 3 * 2 + screen.width / 6 - paddedWidth / 2 + 1,
            hideShowButton.getY() - 22,
            paddedWidth - 2, 18,
            Text.translatable("gui.recipebook.search_hint"),
            Text.translatable("gui.recipebook.search_hint"),
            searchQuery -> optionList.getList().updateSearchQuery(searchQuery)
        );

        optionList = new ListHolderWidget<>(
            () -> new ScreenRect(areaGetter.get().position(), areaGetter.get().width() / 3 * 2 + 1, areaGetter.get().height()),
            new CustomOptionListWidget(screen, category, client, 0, 0, screen.width / 3 * 2 + 1, screen.height, desc -> {
                descriptionWidget.setOptionDescription(desc);
            })
        );

        descriptionWidget = new OptionDescriptionWidget(
            () -> new ScreenRect(
                screen.width / 3 * 2 + padding,
                areaGetter.get().getTop() + padding,
                paddedWidth,
                searchField.getY() - 1 - areaGetter.get().getTop() - padding * 2
            ),
            null
        );

        updateButtons();
    }

    public void hideOrShow() {
        if(client.currentScreen == screen) {
            hideShowButton.setMessage(Text.translatable(ConfigGUI.LANG_KEY_PREFIX + ".show"));
            Screen hiddenScreen = new ConfigScreen.HiddenScreen(screen.getTitle(), hideShowButton);
            client.setScreen(hiddenScreen);
        } else {
            hideShowButton.setMessage(Text.translatable(ConfigGUI.LANG_KEY_PREFIX + ".hide"));
            client.setScreen(screen);
        }
    }

    @Override
    public Text getTitle() {
        return category.name();
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
        consumer.accept(optionList);
        consumer.accept(saveFinishedButton);
        consumer.accept(cancelResetButton);
        consumer.accept(hideShowButton);
        consumer.accept(searchField);
        consumer.accept(descriptionWidget);
    }

    @Override
    public void refreshGrid(ScreenRect screenRectangle) {

    }

    @Override
    public void tick() {
        updateButtons();
        searchField.tick();
        descriptionWidget.tick();
    }

    @Nullable
    @Override
    public Tooltip getTooltip() {
        return tooltip;
    }

    private void updateButtons() {
        boolean pendingChanges = screen.pendingChanges();

        if (Screen.hasShiftDown()) {
            cancelResetButton.active = true;
            cancelResetButton.setTooltip(Tooltip.of(Text.translatable(ConfigGUI.LANG_KEY_PREFIX + ".reset.tooltip")));
        } else {
            cancelResetButton.active = false;
            cancelResetButton.setTooltip(Tooltip.of(Text.translatable(ConfigGUI.LANG_KEY_PREFIX + ".reset.tooltip.holdShift")));
        }

        saveFinishedButton.setMessage(pendingChanges ? Text.translatable("yacl.gui.save") : GuiUtils.translatableFallback("yacl.gui.done", ScreenTexts.DONE));
        saveFinishedButton.setTooltip(Tooltip.of(pendingChanges ? Text.translatable("yacl.gui.save.tooltip") : Text.translatable("yacl.gui.finished.tooltip")));
        cancelResetButton.setMessage(pendingChanges ? GuiUtils.translatableFallback("yacl.gui.cancel", ScreenTexts.CANCEL) : Text.translatable("controls.reset"));
        cancelResetButton.setTooltip(Tooltip.of(pendingChanges ? Text.translatable("yacl.gui.cancel.tooltip") : Text.translatable("yacl.gui.reset.tooltip")));
    }

}
