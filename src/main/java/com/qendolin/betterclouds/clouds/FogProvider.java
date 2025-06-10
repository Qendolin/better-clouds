package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.compat.SodiumExtraCompat;
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.util.RenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

//? if <1.21.3 {
/*import com.mojang.blaze3d.systems.RenderSystem;
*///?}

public abstract class FogProvider {

    @Nullable
    public static RenderHelper.Fog getFog(MinecraftClient client, Config config, float tickDelta) {
        if (client.world == null) return null;

        Camera camera = client.gameRenderer.getCamera();
        float cloudDistance = config.blockDistance();

        SodiumExtraCompat.PREVENT_FOG_MODIFICATION.set(true);
        RenderHelper.Fog original = RenderHelper.getFog();
        Vector4f color = new Vector4f(original.red(), original.green(), original.blue(), original.alpha());

        //? if >=1.21.3 {
        if (color.w == 0.0) { // Fog off
            // Need to fix the color, thanks sodium-extras for all the extra work /s
            color = BackgroundRenderer.getFogColor(camera, tickDelta, client.world, client.options.getClampedViewDistance(), client.gameRenderer.getSkyDarkness(tickDelta));
        }
        var adjusted = BackgroundRenderer.applyFog(camera, BackgroundRenderer.FogType.FOG_TERRAIN, color, cloudDistance, shouldUseThickFog(client.world, camera.getPos()), tickDelta);
        float start = adjusted.start();
        float end = adjusted.end();
        FogShape shape = adjusted.shape();
        //?} else {
        /*BackgroundRenderer.applyFog(camera, BackgroundRenderer.FogType.FOG_TERRAIN, cloudDistance, shouldUseThickFog(client.world, camera.getPos()), tickDelta);
        float start = RenderSystem.getShaderFogStart();
        float end = RenderSystem.getShaderFogEnd();
        FogShape shape = RenderSystem.getShaderFogShape();
        if(color.w == 0.0) { // Fog off
            //? if >1.20.1 {
            BackgroundRenderer.applyFogColor();
            //?} else {
            /^BackgroundRenderer.setFogBlack();
            ^///?}
            color.set(RenderSystem.getShaderFogColor());
        }
        *///?}
        SodiumExtraCompat.PREVENT_FOG_MODIFICATION.set(false);

        // Revert any changes
        original.apply();

        if (end == 0.0) {
            // Assume fog is disabled
            return null;
        }

        float range = end - start;
        end *= config.fogEndFactor; // no clamp because fun
        start = Math.max(end - config.fogRangeFactor * range, 0);

        return new RenderHelper.Fog(start, end, shape, color.x, color.y, color.z, color.w);
    }

    private static boolean shouldUseThickFog(ClientWorld world, Vec3d pos) {
        return world.getDimensionEffects().useThickFog(MathHelper.floor(pos.x), MathHelper.floor(pos.z)) ||
            MinecraftClient.getInstance().inGameHud.getBossBarHud().shouldThickenFog();
    }

}
