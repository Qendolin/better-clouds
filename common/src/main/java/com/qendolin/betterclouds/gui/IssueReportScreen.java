package com.qendolin.betterclouds.gui;

import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.telemetry.ITelemetry;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class IssueReportScreen extends Screen {

    private static final Component TITLE = Component.translatable("betterclouds.gui.issueReport.title");
    private static final Component MESSAGE = Component.translatable("betterclouds.gui.issueReport.message");
    private static final Component TOAST_MESSAGE = Component.translatable("betterclouds.gui.issueReport.toast.sent");
    private static final SystemToast.SystemToastId TOAST_TYPE = new SystemToast.SystemToastId();


    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final String details;
    private final Throwable cause;
    private MultiLineTextWidget messageText;

    public IssueReportScreen(Throwable cause, String details) {
        super(TITLE);
        this.details = details;
        this.cause = cause;
    }

    @Override
    protected void init() {
        super.init();
        assert minecraft != null;

        layout.addToHeader(new StringWidget(title, font));
        var body = layout.addToContents(new GridLayout().rowSpacing(8));
        var bodyAdder = body.createRowHelper(1);
        Component text = MutableComponent.create(MESSAGE.getContents())
            .append(Component.literal("\n\n"))
            .append(Component.literal(details).withStyle(style -> style.withColor(ChatFormatting.GRAY)));
        messageText = bodyAdder.addChild(new MultiLineTextWidget(text, this.font).setCentered(true));

        var footer = layout.addToFooter(new GridLayout().columnSpacing(5).rowSpacing(5));
        var footerAdder = footer.createRowHelper(2);
        LayoutSettings positioner = footerAdder.newCellSettings().alignHorizontallyCenter();
        footerAdder.addChild(Button.builder(Component.translatable("gui.no"), (btn) -> {
            onClose();
        }).width(100).build(), positioner);
        footerAdder.addChild(Button.builder(Component.translatable("gui.yes"), (btn) -> {
            ITelemetry.INSTANCE.sendIssueReport(CrashReport.forThrowable(cause, details));
            // Show success regardless of actual result
            minecraft.getToastManager().addToast(
                SystemToast.multiline(minecraft, TOAST_TYPE, Component.nullToEmpty("Better Clouds"), TOAST_MESSAGE)
            );
            onClose();
        }).width(100).build(), positioner);

        footerAdder.addChild(Button.builder(Component.translatable("betterclouds.gui.issueReport.disable"), (btn) -> {
            ConfigManager.instance().issueReportEnabled = false;
            ConfigManager.handler().save();
            onClose();
        }).width(205).build(), 2, positioner);

        footer.arrangeElements();
        layout.setFooterHeight(footer.getHeight() + 13);
        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void repositionElements() {
        messageText.setMaxWidth(this.width - 50);
        messageText.setWidth(this.width - 50);
        layout.arrangeElements();
    }
}
