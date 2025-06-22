package com.qendolin.betterclouds.clouds.fog;

//? if >=1.20.1 && <1.21.3 {
/*import com.mojang.blaze3d.systems.RenderSystem;
import com.qendolin.betterclouds.compat.SodiumExtraCompat;
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.mixin.required.GameRendererAccessor;
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

class FogProvider1201 implements FogProvider {

    @Nullable
    public Fog getFogInternal(MinecraftClient client, Config config, float tickDelta, Camera camera, float distance) {
        assert client.world != null;

        float[] originalColor = RenderSystem.getShaderFogColor();
        float originalStart = RenderSystem.getShaderFogStart();
        float originalEnd = RenderSystem.getShaderFogEnd();
        FogShape originalShape = RenderSystem.getShaderFogShape();
        Vector4f color = new Vector4f(originalColor);

        BackgroundRenderer.applyFog(camera, BackgroundRenderer.FogType.FOG_TERRAIN, distance, shouldUseThickFog(client.world, camera.getPos()), tickDelta);
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

        // Revert any changes
        RenderSystem.setShaderFogStart(originalStart);
        RenderSystem.setShaderFogEnd(originalEnd);
        RenderSystem.setShaderFogShape(originalShape);
        RenderSystem.setShaderFogColor(originalColor[0], originalColor[1], originalColor[2], originalColor[3]);

        return new Fog(start, end, color.x, color.y, color.z, color.w);
    }

    private static boolean shouldUseThickFog(ClientWorld world, Vec3d pos) {
        return world.getDimensionEffects().useThickFog(MathHelper.floor(pos.x), MathHelper.floor(pos.z)) ||
               MinecraftClient.getInstance().inGameHud.getBossBarHud().shouldThickenFog();
    }
}
*///?}