package com.qendolin.betterclouds.rendering;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.qendolin.betterclouds.compat.IrisCompat;
import com.qendolin.betterclouds.config.Config;
import net.minecraft.client.Minecraft;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;

public class VanillaRenderTarget {

    private static final Supplier<String> RENDER_PASS_LABEL = () -> "BetterClouds";
    private final Minecraft client;
    private final boolean useIris;
    private RenderPass renderPass = null;

    public VanillaRenderTarget(Minecraft client, Config config) {
        this.client = client;
        useIris = IrisCompat.instance().isShadersEnabled() && config.useIrisFBO;
    }

    public void begin() {
        if (useIris) {
            IrisCompat.instance().bindFramebuffer();
            return;
        }

        var framebuffer = client.levelRenderer.cloudsTarget();
        if (framebuffer == null)
            framebuffer = client.gameRenderer.mainRenderTarget();

        assert framebuffer.getColorTextureView() != null;
        renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                RENDER_PASS_LABEL,
                framebuffer.getColorTextureView(),
                Optional.empty(),
                framebuffer.getDepthTextureView(),
                OptionalDouble.empty()
        );
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
