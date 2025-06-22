package com.qendolin.betterclouds.clouds.fog;

//? if >=1.21.6 {
import com.qendolin.betterclouds.compat.SodiumExtraCompat;
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.duck.FogRendererDuck;
import com.qendolin.betterclouds.mixin.required.GameRendererAccessor;
import com.qendolin.betterclouds.util.RenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4f;

class FogProvider1216 implements FogProvider {

    public Fog getFogInternal(MinecraftClient client, Config config, float tickDelta, Camera camera, float distance) {
        assert client.world != null;

        FogRenderer fogRenderer = ((GameRendererAccessor) client.gameRenderer).getFogRenderer();
        FogRendererDuck.FogApplyResult result = ((FogRendererDuck) fogRenderer).betterclouds$applyFog(
            camera,
            32, // fog color breaks past 32
            shouldUseThickFog(client.world, camera.getPos()),
            client.getRenderTickCounter(),
            client.gameRenderer.getSkyDarkness(tickDelta),
            client.world);

        float start = result.fogData().environmentalStart;
        float end = result.fogData().cloudEnd;
        Vector4f color = result.color();

        return new Fog(start, end, color.x, color.y, color.z, color.w);
    }

    private static boolean shouldUseThickFog(ClientWorld world, Vec3d pos) {
        return world.getDimensionEffects().useThickFog(MathHelper.floor(pos.x), MathHelper.floor(pos.z)) ||
               MinecraftClient.getInstance().inGameHud.getBossBarHud().shouldThickenFog();
    }
}

//?}