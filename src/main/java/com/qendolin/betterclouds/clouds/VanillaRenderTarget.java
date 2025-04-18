package com.qendolin.betterclouds.clouds;


import com.qendolin.betterclouds.compat.IrisCompat;
import com.qendolin.betterclouds.config.Config;
import net.minecraft.client.MinecraftClient;

//? if >=1.21.5 {
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.OptionalDouble;
import java.util.OptionalInt;
//?} else {
/*import net.minecraft.client.render.RenderPhase;
import com.qendolin.betterclouds.mixin.runtime.RenderPhaseAccessor;
*///?}

public class VanillaRenderTarget {

    private final MinecraftClient client;

    private final boolean useIris;
    //? if >=1.21.5 {
    private RenderPass renderPass = null;
    //?} else {
    /*private RenderPhase renderPhase = null;
     *///?}

    public VanillaRenderTarget(MinecraftClient client, Config config) {
        this.client = client;
        useIris = IrisCompat.instance().isShadersEnabled() && config.useIrisFBO;
    }

    public void begin() {
        if (useIris) {
            IrisCompat.instance().bindFramebuffer();
            return;
        }

        var framebuffer = client.worldRenderer.getCloudsFramebuffer();
        if (framebuffer == null)
            framebuffer = client.getFramebuffer();

        //? if >=1.21.5 {
        renderPass = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(framebuffer.getColorAttachment(), OptionalInt.empty(), framebuffer.getDepthAttachment(), OptionalDouble.empty());
        //?} else {
        /*framebuffer.beginWrite(false);
        renderPhase = RenderPhaseAccessor.getCloudsTarget();
        if(renderPhase != null)
            renderPhase.startDrawing();
        *///?}
    }

    public void end() {
        if (useIris) {
            // ummm, idk
            return;
        }

        //? if >=1.21.5 {
        if (renderPass != null) {
            renderPass.close();
        }
        //?} else {
        /*if(renderPhase != null) {
            renderPhase.endDrawing();
        }
        *///?}
    }
}
