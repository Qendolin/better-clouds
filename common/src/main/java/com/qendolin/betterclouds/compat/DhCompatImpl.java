package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.BetterClouds;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.rendering.TextureWrapper;
import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.enums.config.EDhApiDepthRange;
import com.seibel.distanthorizons.api.enums.rendering.EDhApiRenderPass;
import com.seibel.distanthorizons.api.methods.events.DhApiEventRegister;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.*;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.*;
import com.seibel.distanthorizons.api.objects.DhApiResult;
import com.seibel.distanthorizons.common.render.blaze.BlazeDhMetaRenderer;
import org.joml.Matrix4f;

import java.util.Optional;

public class DhCompatImpl extends DhCompat {
    protected boolean textureCreateFlag = false;
    private boolean isDhInitialized = false;
    private DhApiRenderParam lastRenderParam = null;
    private int depthTextureWidth, depthTextureHeight;

    public DhCompatImpl() {
        BetterCloudsStatic.getLogger().info("Registering DH Api events");
        // Lambdas didn't work
        DhApiEventRegister.on(DhApiAfterDhInitEvent.class, new DhApiAfterDhInitEvent() {
            @Override
            public void afterDistantHorizonsInit(DhApiEventParam<Void> dhApiEventParam) {
                if (BetterClouds.isEnabled()) {
                    disableLodClouds();
                }
            }
        });
        DhApiEventRegister.on(DhApiBeforeRenderEvent.class, new DhApiBeforeRenderEvent() {
            @Override
            public void beforeRender(DhApiCancelableEventParam<DhApiRenderParam> dhApiEventParam) {
                isDhInitialized = true;
                if (dhApiEventParam.value.renderPass == EDhApiRenderPass.OPAQUE || dhApiEventParam.value.renderPass == EDhApiRenderPass.OPAQUE_AND_TRANSPARENT) {
                    lastRenderParam = dhApiEventParam.value;
                }
                // With shaders the transparent rendering pass might be deferred and doesn't have a 'valid' dhProjectionMatrix
                // Don't know if that's how it's supposed to be, but I can't use it.

                if (BetterClouds.isEnabled()) {
                    disableLodClouds();
                }
            }
        });

        DhApiEventRegister.on(DhApiAfterColorDepthTextureCreatedEvent.class, new DhApiAfterColorDepthTextureCreatedEvent() {
            @Override
            public void onResize(DhApiEventParam<DhApiTextureCreatedParam> event) {
                depthTextureWidth = event.value.newWidth;
                depthTextureHeight = event.value.newHeight;
            }
        });
    }

    @Override
    public boolean isReady() {
        return isDhInitialized && lastRenderParam != null;
    }

    @Override
    public boolean isEnabled() {
        return isDhInitialized && DhApi.Delayed.configs.graphics().renderingEnabled().getValue();
    }

    @Override
    public Matrix4f getProjectionMatrix() {
        if (lastRenderParam == null) return null;
        return new Matrix4f(lastRenderParam.dhProjectionMatrix.getValuesAsArray()).transpose();
    }

    @Override
    public void putProjMat(float[] values) {
        if (lastRenderParam == null) return;
        var projMat = lastRenderParam.dhProjectionMatrix;
        projMat.transpose();
        projMat.putValuesInArray(values);
    }

    @Override
    public Optional<Integer> getDepthTextureId() {
        DhApiResult<Integer> result = DhApi.Delayed.renderProxy.getDhDepthTextureGlId();
        if (result.success) {
            return Optional.of(result.payload);
        }
        return Optional.empty();
    }

    @Override
    public TextureWrapper getDepthTexture() {
        if (!isDhInitialized) return null;

        // native renderer = whether dh is using opengl renderer
        if (DhApi.Delayed.renderProxy.isNativeRenderer()) {
            if (depthTextureWidth + depthTextureHeight <= 0) return null;

            return getDepthTextureId().map(id -> TextureWrapper.fromBorrowedTexture(
                    "LodDepthTexture", id,
                    depthTextureWidth, depthTextureHeight
            )).orElse(null);
        }
        var wrap = BlazeDhMetaRenderer.INSTANCE.dhDepthTextureWrapper;
        if (wrap == null) return null;

        return TextureWrapper.from(
                "LodDepthTexture", wrap.getName().hashCode(),
                wrap::getTextureView, wrap::getTextureSampler
        ).asBorrowed();
    }

    @Override
    public void disableLodClouds() {
        DhApi.Delayed.configs.graphics().genericRendering().cloudRenderingEnabled().setValue(false, "Better Clouds");
    }

    @Override
    public boolean isZNeg1To1() {
        if (!isDhInitialized) return false;
        return DhApi.Delayed.renderProxy.getDepthRange().equals(EDhApiDepthRange.NEG_ONE_TO_POS_ONE);
    }

    @Override
    public boolean isTextureCreateFlagSet() {
        return textureCreateFlag;
    }

    @Override
    public void resetTextureCreateFlag() {
        textureCreateFlag = false;
    }
}
