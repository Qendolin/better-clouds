package com.qendolin.betterclouds.clouds.fog;

import com.qendolin.betterclouds.compat.SodiumExtraCompat;
import com.qendolin.betterclouds.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import org.jetbrains.annotations.Nullable;

public interface FogProvider {
    //? if >=1.20.1 && <1.21.3
    /*FogProvider instance = new FogProvider1201();*/
    //? if >=1.21.3 && <1.21.6
    /*FogProvider instance = new FogProvider1213();*/
    //? if >=1.21.6
    FogProvider instance = new FogProvider1216();

    default Fog getFogInternal(MinecraftClient client, Config config, float tickDelta, Camera camera, float distance) {
        return null;
    }

    @Nullable
    default Fog getFog(MinecraftClient client, Config config, float tickDelta) {
        if (client.world == null) return null;

        Camera camera = client.gameRenderer.getCamera();
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
