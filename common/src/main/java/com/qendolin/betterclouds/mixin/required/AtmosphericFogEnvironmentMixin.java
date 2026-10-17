package com.qendolin.betterclouds.mixin.required;

import com.qendolin.betterclouds.rendering.CloudRenderCoordinator;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AtmosphericFogEnvironment.class, priority = 100)   // run after simple fog control and whatnot
public abstract class AtmosphericFogEnvironmentMixin {
    @Inject(at = @At("TAIL"), method = "setupFog")  // after simple fog control modifies fog data
    private void captureFogData(FogData fog, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker, CallbackInfo ci) {
        CloudRenderCoordinator.instance.fogData = fog;
    }
}
