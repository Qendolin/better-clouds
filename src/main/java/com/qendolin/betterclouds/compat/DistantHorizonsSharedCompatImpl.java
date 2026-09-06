package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.BetterClouds;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.seibel.distanthorizons.api.DhApi;
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

public abstract class DistantHorizonsSharedCompatImpl extends DistantHorizonsCompat {
    private boolean isDhInitialized = false;
    private DhApiRenderParam lastRenderParam = null;
    protected boolean textureCreateFlag = false;

    public DistantHorizonsSharedCompatImpl() {
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
        return isDhInitialized && DhApi.Delayed.configs.graphics().renderingEnabled().getValue();
    }

    @Override
    public Matrix4f getProjectionMatrix() {
        Optional<Matrix4f> irisProjection = IrisCompat.instance().getDhProjectionMatrix();
        if (irisProjection.isPresent()) {
            return irisProjection.get();
        }
        float[] mat = getDhProjectionMatrixValues(lastRenderParam);
        return new Matrix4f(mat[0], mat[4], mat[8], mat[12],
            mat[1], mat[5], mat[9], mat[13],
            mat[2], mat[6], mat[10], mat[14],
            mat[3], mat[7], mat[11], mat[15]);
    }

    abstract float[] getDhProjectionMatrixValues(DhApiRenderParam renderParam);

    @Override
    public Optional<Integer> getDepthTextureId() {
        DhApiResult<Integer> result = DhApi.Delayed.renderProxy.getDhDepthTextureId();
        if (result.success) {
            return Optional.of(result.payload);
        }
        return Optional.empty();
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
