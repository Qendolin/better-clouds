package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.Main;
import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.methods.events.DhApiEventRegister;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiAfterDhInitEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiAfterRenderEvent;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiEventParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.seibel.distanthorizons.api.objects.DhApiResult;
import org.joml.Matrix4f;

import java.util.Optional;

class DistantHorizonsCompatImpl extends DistantHorizonsCompat {
    private boolean isDhInitialized = false;
    private DhApiRenderParam lastRenderParam = null;

    public DistantHorizonsCompatImpl() {
        Main.LOGGER.info("Registering DH Api events");
        // Lambdas didn't work
        DhApiEventRegister.on(DhApiAfterDhInitEvent.class, new DhApiAfterDhInitEvent() {
            @Override
            public void afterDistantHorizonsInit(DhApiEventParam<Void> dhApiEventParam) {
                isDhInitialized = true;
            }
        });
        DhApiEventRegister.on(DhApiAfterRenderEvent.class, new DhApiAfterRenderEvent() {
            @Override
            public void afterRender(DhApiEventParam<EventParam> dhApiEventParam) {
                lastRenderParam = dhApiEventParam.value;
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
        float[] mat = lastRenderParam.dhProjectionMatrix.getValuesAsArray();
        return new Matrix4f(mat[0], mat[4],  mat[8], mat[12],
                            mat[1], mat[5],  mat[9], mat[13],
                            mat[2], mat[6], mat[10], mat[14],
                            mat[3], mat[7], mat[11], mat[15]);
    }

    @Override
    public Optional<Integer> getDepthTextureId() {
        DhApiResult<Integer> result = DhApi.Delayed.renderProxy.getDhDepthTextureId();
        if(result.success) {
            return Optional.of(result.payload);
        }
        return Optional.empty();
    }

}
