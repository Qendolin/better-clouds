package com.qendolin.betterclouds.rendering;

import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.compat.GLCompat;
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PreferredGraphicsApi;

public abstract class GraphicsCompat {
    public static GraphicsCompat instance;
    public static boolean isOpenGL = false;

    public static void initCompat() {
        if (ConfigManager.instance().renderer == Config.Renderer.OPENGL &&
                PreferredGraphicsApi.VULKAN.equals(Minecraft.getInstance().options.preferredGraphicsBackend().get())) {
            ConfigManager.instance().renderer = Config.Renderer.BLAZE3D;
            BetterCloudsStatic.getLogger().warn("Preferred graphics backend is Vulkan, forcing renderer to Blaze3D");
        }

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
