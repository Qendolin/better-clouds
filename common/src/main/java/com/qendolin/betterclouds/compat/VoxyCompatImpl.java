package com.qendolin.betterclouds.compat;

import com.mojang.blaze3d.systems.RenderSystem;
import com.qendolin.betterclouds.rendering.BorrowedGlTexture;
import com.qendolin.betterclouds.rendering.TextureWrapper;
import me.cortex.voxy.client.config.VoxyConfig;
import me.cortex.voxy.client.core.IVoxyRenderSystemHolder;
import me.cortex.voxy.client.core.VoxyRenderSystem;
import me.cortex.voxy.client.iris.IGetIrisVoxyPipelineData;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import org.joml.Matrix4f;

public class VoxyCompatImpl extends VoxyCompat {
    @Override
    public Matrix4f getProjectionMatrix() {
        VoxyRenderSystem rsHolder = IVoxyRenderSystemHolder.getNullable();
        if (rsHolder == null || rsHolder.getViewport() == null)
            return null;
        return rsHolder.getViewport().projection;
    }

    @Override
    public TextureWrapper getOpaqueDepthTexture() {
        WorldRenderingPipeline pipeline = IrisCompat.instance().getPipeline();
        if (!(pipeline instanceof IGetIrisVoxyPipelineData voxyPipeline))
            return null;

        if (voxyPipeline.voxy$getPipelineData() == null || voxyPipeline.voxy$getPipelineData().thePipeline == null)
            return null;

        var depthTex = voxyPipeline.voxy$getPipelineData().thePipeline.fb.getDepthTex();
        if (depthTex == null)
            return null;

        return TextureWrapper.from(
                "VoxyDepthTexture", depthTex.id,
                () -> RenderSystem.getDevice().createTextureView(
                        new BorrowedGlTexture(depthTex.id, depthTex.getWidth(), depthTex.getHeight())
                ),
                TextureWrapper::defaultSampler
        );
    }

    @Override
    public boolean isEnabled() {
        return VoxyConfig.CONFIG.isRenderingEnabled();
    }
}
