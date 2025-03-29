package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.compat.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;

public abstract class CloudinessProvider {

    public static float getCloudiness(ClientWorld world, float tickDelta) {
        if (world == null)
            return 1.0f;
        float weather = Math.max(0.6f * getTrueRainGradient(world, tickDelta), getTrueThunderGradient(world, tickDelta));
        float cloudiness = weather * 0.3f + 0.5f;
        cloudiness *= SereneSeasonsCompat.instance().getCloudinessFactor(world);
        cloudiness *= FabricSeasonsCompat.instance().getCloudinessFactor(world);
        return MathHelper.clamp(cloudiness, 0.0f, 1.0f);
    }


    private static float getTrueRainGradient(ClientWorld world, float tickDelta) {
        if (ModLoaded.HEAD_IN_THE_CLOUDS) {
            return ((WorldDuck) world).betterclouds$getOriginalRainGradient(tickDelta);
        }
        return world.getRainGradient(tickDelta);
    }

    private static float getTrueThunderGradient(ClientWorld world, float tickDelta) {
        if (ModLoaded.HEAD_IN_THE_CLOUDS) {
            return ((WorldDuck) world).betterclouds$getOriginalThunderGradient(tickDelta);
        }
        return world.getThunderGradient(tickDelta);
    }
}
