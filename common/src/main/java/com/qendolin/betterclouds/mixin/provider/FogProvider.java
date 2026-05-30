package com.qendolin.betterclouds.mixin.provider;

import com.qendolin.betterclouds.compat.SodiumExtraCompat;
import com.qendolin.betterclouds.config.Config;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public interface FogProvider {
    FogProvider instance = new FogProvider1216();

    default Fog getFogInternal(Minecraft client, Config config, float tickDelta, Camera camera, float distance) {
        return null;
    }

    @Nullable
    default Fog getFog(Minecraft client, Config config, float tickDelta) {
        if (client.level == null) return null;

        Camera camera = client.gameRenderer.mainCamera();
        float cloudDistance = config.blockDistance();

        Fog fog;
        try {
            SodiumExtraCompat.PREVENT_FOG_MODIFICATION.set(true);
            fog = getFogInternal(client, config, tickDelta, camera, cloudDistance);
        } finally {
            SodiumExtraCompat.PREVENT_FOG_MODIFICATION.set(false);
        }

        if (fog == null || fog.end == 0.0 || fog.end < fog.start) {
            // Assume fog is disabled
            return null;
        }

        float start = fog.start;
        float end = fog.end;
        float range = end - start;
        end *= config.fogEndFactor; // no clamp because fun
        start = Math.max(end - config.fogRangeFactor * range, 0);

        return new Fog(start, end, fog.red, fog.green, fog.blue, fog.alpha);
    }

    record Fog(float start, float end, float red, float green, float blue, float alpha) {

    }
}
