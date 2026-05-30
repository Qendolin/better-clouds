package com.qendolin.betterclouds.compat;

import com.seibel.distanthorizons.api.methods.events.DhApiEventRegister;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiAfterColorDepthTextureCreatedEvent;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiEventParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiTextureCreatedParam;

class DistantHorizons6CompatImpl extends DistantHorizons3CompatImpl {
    public DistantHorizons6CompatImpl() {
        super();
        DhApiEventRegister.on(DhApiAfterColorDepthTextureCreatedEvent.class,
            new DhApiAfterColorDepthTextureCreatedEvent() {
                @Override
                public void onResize(DhApiEventParam<DhApiTextureCreatedParam> event) {
                    textureCreateFlag = true;
                }
            });
    }
}
