package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.BetterClouds;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.enums.config.EDhApiDepthRange;
import com.seibel.distanthorizons.api.enums.rendering.EDhApiRenderPass;
import com.seibel.distanthorizons.api.methods.events.DhApiEventRegister;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiAfterDhInitEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeRenderEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiColorDepthTextureCreatedEvent;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiCancelableEventParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiEventParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.seibel.distanthorizons.api.objects.DhApiResult;
import org.joml.Matrix4f;

import java.util.Optional;

public class DistantHorizonsCompatImpl extends DistantHorizonsCompat {
    private boolean isDhInitialized = false;
    private DhApiRenderParam lastRenderParam = null;
    protected boolean textureCreateFlag = false;

    public DistantHorizonsCompatImpl() {
        BetterCloudsStatic.getLogger().info("Registering DH Api events");
        // Lambdas didn't work
        DhApiEventRegister.on(DhApiAfterDhInitEvent.class, new DhApiAfterDhInitEvent() {
            @Override
            public void afterDistantHorizonsInit(DhApiEventParam<Void> dhApiEventParam) {
                isDhInitialized = true;
            }
        });
        DhApiEventRegister.on(DhApiBeforeRenderEvent.class, new DhApiBeforeRenderEvent() {
            @Override
            public void beforeRender(DhApiCancelableEventParam<DhApiRenderParam> dhApiEventParam) {
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
        DhApiEventRegister.on(DhApiColorDepthTextureCreatedEvent.class, new DhApiColorDepthTextureCreatedEvent() {
            @Override
            public void onResize(DhApiEventParam<EventParam> dhApiEventParam) {
                textureCreateFlag = true;
            }
        });
    }

    @Override
    public boolean isReady() {
        return isDhInitialized && lastRenderParam != null;
    }

    @Override
    public boolean isEnabled() {
        return isReady() && DhApi.Delayed.configs.graphics().renderingEnabled().getValue();
    }

    @Override
    public Matrix4f getProjectionMatrix() {
        if (!isEnabled()) return null;
        Optional<Matrix4f> irisProjection = IrisCompat.instance().getDhProjectionMatrix();
        if (irisProjection.isPresent()) {
            return irisProjection.get();
        }
        var m = lastRenderParam.dhProjectionMatrix;
        return new Matrix4f(m.m00, m.m01, m.m02, m.m03, m.m10, m.m11, m.m12, m.m13, m.m20, m.m21, m.m22, m.m23, m.m30, m.m31, m.m32, m.m33).transpose();
    }

    @Override
    public Optional<Integer> getDepthTextureId() {
        if (!isEnabled()) return Optional.empty();
        DhApiResult<Integer> result = DhApi.Delayed.renderProxy.getDhDepthTextureGlId();
        if (result.success) {
            return Optional.of(result.payload);
        }
        return Optional.empty();
    }

    @Override
    public boolean isZNeg1To1() {
        return isEnabled() && DhApi.Delayed.renderProxy.getDepthRange().equals(EDhApiDepthRange.NEG_ONE_TO_POS_ONE);
    }

    @Override
    public void disableLodClouds() {
        DhApi.Delayed.configs.graphics().genericRendering().cloudRenderingEnabled().setValue(false, "Better Clouds");
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
