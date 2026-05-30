package com.qendolin.betterclouds.rendering;

import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.compat.GLCompat;
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.config.ConfigManager;

public abstract class GraphicsCompat {
    public static GraphicsCompat instance;
    public static boolean isOpenGL = false;

    public static void initCompat() {
        isOpenGL = ConfigManager.instance().renderer == Config.Renderer.OPENGL;
        if (isOpenGL)
            instance = new GLCompat(BetterCloudsStatic.IS_DEV);
        else
            instance = new GraphicsCompat() {
            };

        instance.init();
    }

    public void init() {

    }

    public boolean isIncompatible() {
        return false;
    }

    public boolean isPartiallyIncompatible() {
        return false;
    }

    public void initDev() {

    }

    public void pushDebugGroupDev(String groupName) {

    }

    public void debugMessage(String s) {

    }

    public void popDebugGroupDev() {

    }

    public void popDebugGroup() {

    }
}
