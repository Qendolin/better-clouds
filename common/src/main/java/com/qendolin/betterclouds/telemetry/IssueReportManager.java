package com.qendolin.betterclouds.telemetry;

import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.gui.IssueReportScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class IssueReportManager {

    private static final long MAX_SHOW_INTERVAL = 10 * 1000;
    private static long lastShowTime = 0;
    private static IssueReportScreen queuedScreen = null;

    public static boolean enabled() {
        return ConfigManager.instance().issueReportEnabled && !BetterCloudsStatic.IS_DEV;
    }

    public static boolean handle(Throwable e, String details) {
        if(!enabled())
            return false;

        long time = System.currentTimeMillis();
        if(time - lastShowTime < MAX_SHOW_INTERVAL)
            return false;

        Minecraft client = Minecraft.getInstance();
        if(client.screen instanceof IssueReportScreen || queuedScreen != null) {
            return false;
        }

        lastShowTime = time;
        ConfigManager.instance().enabled = false;
        IssueReportScreen screen = new IssueReportScreen(e, details);
        client.execute(() -> {
            if(client.screen == null) {
                client.setScreen(screen);
            } else if(!(client.screen instanceof IssueReportScreen)) {
                queuedScreen = screen;
            }
        });
        return true;
    }

    public static Screen popQueuedScreen() {
        var tmp = queuedScreen;
        queuedScreen = null;
        return tmp;
    }
}
