package com.qendolin.betterclouds.mixin.provider;

import com.qendolin.betterclouds.compat.*;
import com.qendolin.betterclouds.mixin.duck.WorldDuck;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

public abstract class CloudinessProvider {

    public static float getCloudiness(ClientLevel world, float tickDelta) {
        if (world == null)
            return 1.0f;
        float weather = Math.max(0.6f * getTrueRainGradient(world, tickDelta), getTrueThunderGradient(world, tickDelta));
        float cloudiness = weather * 0.3f + 0.5f;
        cloudiness *= SereneSeasonsCompat.instance().getCloudinessFactor(world);
        return Mth.clamp(cloudiness, 0.0f, 1.0f);
    }


    private static float getTrueRainGradient(ClientLevel world, float tickDelta) {
        if (ModLoaded.HEAD_IN_THE_CLOUDS) {
            return ((WorldDuck) world).betterclouds$getOriginalRainGradient(tickDelta);
        }
        return world.getRainLevel(tickDelta);
    }

    private static float getTrueThunderGradient(ClientLevel world, float tickDelta) {
        if (ModLoaded.HEAD_IN_THE_CLOUDS) {
            return ((WorldDuck) world).betterclouds$getOriginalThunderGradient(tickDelta);
        }
        return world.getThunderLevel(tickDelta);
    }
}
