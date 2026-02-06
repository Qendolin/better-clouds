package com.qendolin.betterclouds.clouds;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.qendolin.betterclouds.compat.IrisCompat;
import com.qendolin.betterclouds.config.Config;
import net.minecraft.client.MinecraftClient;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Supplier;

public class VanillaRenderTarget {

    private final MinecraftClient client;
    private static final Supplier<String> RENDER_PASS_LABEL = () -> "BetterClouds";

    private final boolean useIris;
    private RenderPass renderPass = null;

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

        renderPass = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(RENDER_PASS_LABEL, framebuffer.getColorAttachmentView(), OptionalInt.empty(), framebuffer.getDepthAttachmentView(), OptionalDouble.empty());
    }

    public void end() {
        if (useIris) {
            // ummm, idk
            return;
        }

        if (renderPass != null) {
            renderPass.close();
        }
    }
}
