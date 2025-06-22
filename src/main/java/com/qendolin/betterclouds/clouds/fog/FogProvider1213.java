package com.qendolin.betterclouds.clouds.fog;

//? if >=1.21.3 && <1.21.6 {
/*import com.mojang.blaze3d.systems.RenderSystem;
import com.qendolin.betterclouds.compat.SodiumExtraCompat;
import com.qendolin.betterclouds.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;


class FogProvider1213 implements FogProvider {
    @Nullable
    public Fog getFogInternal(MinecraftClient client, Config config, float tickDelta, Camera camera, float distance) {
        assert client.world != null;

        SodiumExtraCompat.PREVENT_FOG_MODIFICATION.set(true);
        var original = RenderSystem.getShaderFog();
        Vector4f color = new Vector4f(original.red(), original.green(), original.blue(), original.alpha());

        if (color.w == 0.0) { // Fog off
            // Need to fix the color, thanks sodium-extras for all the extra work /s
            color = BackgroundRenderer.getFogColor(camera, tickDelta, client.world, client.options.getClampedViewDistance(), client.gameRenderer.getSkyDarkness(tickDelta));
        }
        var adjusted = BackgroundRenderer.applyFog(camera, BackgroundRenderer.FogType.FOG_TERRAIN, color, distance, shouldUseThickFog(client.world, camera.getPos()), tickDelta);
        float start = adjusted.start();
        float end = adjusted.end();

        SodiumExtraCompat.PREVENT_FOG_MODIFICATION.set(false);

        // Revert any changes
        RenderSystem.setShaderFog(original);

        return new Fog(start, end, color.x, color.y, color.z, color.w);
    }

    private static boolean shouldUseThickFog(ClientWorld world, Vec3d pos) {
        return world.getDimensionEffects().useThickFog(MathHelper.floor(pos.x), MathHelper.floor(pos.z)) ||
               MinecraftClient.getInstance().inGameHud.getBossBarHud().shouldThickenFog();
    }
}
*///?}