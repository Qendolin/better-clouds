package com.qendolin.betterclouds.gui;

import com.qendolin.betterclouds.telemetry.ITelemetry;
import com.qendolin.betterclouds.config.ConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.crash.CrashReport;

public class IssueReportScreen extends Screen {

    private static final Text TITLE = Text.translatable("betterclouds.gui.issueReport.title");
    private static final Text MESSAGE = Text.translatable("betterclouds.gui.issueReport.message");
    private static final Text TOAST_MESSAGE = Text.translatable("betterclouds.gui.issueReport.toast.sent");
    private static final SystemToast.Type TOAST_TYPE = new SystemToast.Type();

    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private final String details;
    private final Throwable cause;
    private MultilineTextWidget messageText;

    public IssueReportScreen(Throwable cause, String details) {
        super(TITLE);
        this.details = details;
        this.cause = cause;
    }

    @Override
    protected void init() {
        super.init();
        assert client != null;

        layout.addHeader(TITLE, textRenderer);
        var body = layout.addBody(DirectionalLayoutWidget.vertical().spacing(8));
        Text text = MutableText.of(MESSAGE.getContent())
            .append(Text.literal("\n\n"))
            .append(Text.literal(details).styled(style -> style.withColor(Formatting.GRAY)));
        messageText = body.add(new MultilineTextWidget(text, this.textRenderer).setCentered(true));

        var footer = layout.addFooter(new GridWidget().setColumnSpacing(5).setRowSpacing(5));
        var footerAdder = footer.createAdder(2);
        Positioner positioner = footerAdder.copyPositioner().alignHorizontalCenter();
        footerAdder.add(ButtonWidget.builder(Text.translatable("gui.no"), (btn) -> {
            close();
        }).width(100).build(), positioner);
        footerAdder.add(ButtonWidget.builder(Text.translatable("gui.yes"), (btn) -> {
            ITelemetry.INSTANCE.sendIssueReport(CrashReport.create(cause, details));
            // Show success regardless of actual result
            client.getToastManager().add(
                SystemToast.create(client, TOAST_TYPE, Text.of("Better Clouds"), TOAST_MESSAGE)
            );
            close();
        }).width(100).build(), positioner);

        footerAdder.add(ButtonWidget.builder(Text.translatable("betterclouds.gui.issueReport.disable"), (btn) -> {
            ConfigManager.instance().issueReportEnabled = false;
            ConfigManager.handler().save();
            close();
        }).width(205).build(), 2, positioner);

        footer.refreshPositions();
        layout.setFooterHeight(footer.getHeight() + 13);
        layout.forEachChild(this::addDrawableChild);
        refreshWidgetPositions();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void refreshWidgetPositions() {
        messageText.setMaxWidth(this.width - 50);
        messageText.setWidth(this.width - 50);
        layout.refreshPositions();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

    }
}
