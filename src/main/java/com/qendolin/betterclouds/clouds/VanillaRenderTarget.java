package com.qendolin.betterclouds.clouds;


import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.compat.IrisCompat;
import com.qendolin.betterclouds.config.Config;
import net.minecraft.client.MinecraftClient;

import java.util.Arrays;
import java.util.function.Supplier;

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
    private static final Supplier<String> RENDER_PASS_LABEL = () -> "BetterClouds";

    private static final String[] START_DRAWING = new String[]{"setupRenderState", "startDrawing", "method_23516", "m_110185_", "a"};
    private static final String[] END_DRAWING = new String[]{"clearRenderState", "endDrawing", "method_23518", "m_110188_", "b"};

    private final boolean useIris;
    //? if >=1.21.5 {
    private RenderPass renderPass = null;
    //?} else {
    /*private Object renderPhase = null;
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
            /*? if >=1.21.6 {*/
            .createRenderPass(RENDER_PASS_LABEL, framebuffer.getColorAttachmentView(), OptionalInt.empty(), framebuffer.getDepthAttachmentView(), OptionalDouble.empty());
            /*?} else {*/
            /*.createRenderPass(framebuffer.getColorAttachment(), OptionalInt.empty(), framebuffer.getDepthAttachment(), OptionalDouble.empty());
            *//*?}*/

        //?} else {
        /*framebuffer.beginWrite(false);
        renderPhase = RenderPhaseAccessor.getCloudsTarget();
        invokeRenderPhase(renderPhase, START_DRAWING);
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
        /*invokeRenderPhase(renderPhase, END_DRAWING);
        *///?}
    }

    //? if <1.21.5 {
    /*private static void invokeRenderPhase(Object renderPhase, String[] methodNames) {
        ReflectiveOperationException lastException = null;
        for (String methodName : methodNames) {
            try {
                renderPhase.getClass().getMethod(methodName).invoke(renderPhase);
                return;
            } catch (ReflectiveOperationException e) {
                lastException = e;
            }
        }
        BetterCloudsStatic.getLogger().info(Arrays.toString(renderPhase.getClass().getMethods()), lastException);
        BetterCloudsStatic.getLogger().error(lastException);
        throw new RuntimeException("Failed to call RenderPhase." + Arrays.toString(methodNames), lastException);
    }
    *///?}
}
