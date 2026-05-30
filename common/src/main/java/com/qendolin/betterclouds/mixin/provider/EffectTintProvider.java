package com.qendolin.betterclouds.mixin.provider;

import com.qendolin.betterclouds.compat.EnhancedCelestialsCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;

public abstract class EffectTintProvider {

    public static final Vector3f Y = new Vector3f(0.299f, 0.587f, 0.114f);

    public static Vector3f getEffectTint(Minecraft client, @Nullable FogProvider.Fog fog, float tickDelta, Vector3d cameraPos) {
        if (client.level == null || client.player == null)
            return new Vector3f(1.0f, 1.0f, 1.0f);

        Vector3f cloudColor = getCloudsColor(client.level, tickDelta, cameraPos);

        if (EnhancedCelestialsCompat.instance().isEventActive(client.level)) {
            Vector3f tint = EnhancedCelestialsCompat.instance().getEventTint(client.level);
            tint.div(0.2f, 0.2f, 1.0f); // divide be the default value
            cloudColor.mul(tint);
        }

        gammaToLinear(cloudColor);

        float cloudBaseLuma = cloudColor.dot(Y);
        Vector3f cloudBaseChroma = cloudBaseLuma < 0.0001 ? new Vector3f(1.0f) : new Vector3f(cloudColor).div(cloudBaseLuma);

        float cloudLuma = cloudBaseLuma;
        float moon = Mth.clamp(-Mth.cos(getSunAngleRadians(client.level, cameraPos)), -0.25f, 0.25f) * 2 + 0.5f;
        float moonSize = EnhancedCelestialsCompat.instance().getMoonSize(client.level);
        cloudLuma += moonSize * moon * 0.65f;

        // CrY - Chroma and Luma
        Vector4f cry = new Vector4f(cloudBaseChroma, cloudLuma);

        if (fog != null) { // Fog ON
            Vector3f fogColor = new Vector3f(fog.red(), fog.green(), fog.blue());
            compositeColor(fogColor, cry);
        }

        cry.w *= 1 / 0.9777f; // The new calculation produces slightly darker clouds, this is a 'fix'
        cry.w = Mth.clamp(cry.w, 0, 2);

        float saturation = (float) Math.pow(cry.w, 1 / 2.2);
        Vector3f gray = new Vector3f(cry.w);
        Vector3f desaturated = new Vector3f(cry.x, cry.y, cry.z).mul(saturation)
                .add(new Vector3f(gray).mul(1 - saturation));

        Vector3f result = new Vector3f(desaturated).mul(cry.w).min(new Vector3f(1.0f));
        linearToGamma(result);

        if (client.player != null && client.player.hasEffect(MobEffects.NIGHT_VISION)) {
            float min = result.get(result.minComponent());
            result.div(Mth.lerp(GameRenderer.nightVisionScale(client.player, tickDelta), 1.0f, min));
        }
        return result;
    }

    public static void compositeColor(Vector3f color, Vector4f cry) {
        gammaToLinear(color);

        float luma = color.dot(Y);
        Vector3f chroma = luma < 0.0001 ? new Vector3f(1.0f) : new Vector3f(color).div(luma);

        cry.set(
                Mth.sqrt(chroma.x * cry.x),
                Mth.sqrt(chroma.y * cry.y),
                Mth.sqrt(chroma.z * cry.z),
                Mth.square(Mth.sqrt(luma) + Mth.sqrt(cry.w)) / 4
        );
    }

    public static Vector3f getCloudsColor(ClientLevel world, float tickDelta, Vector3d cameraPos) {
        final Vector3f Y = new Vector3f(0.299f, 0.587f, 0.114f);

        // this is from ClientWorld#getCloudsColor
        Vector3f color = new Vector3f(1.0f);
        float rain = world.getRainLevel(tickDelta);
        color.lerp(new Vector3f(color.dot(Y) * 0.6f), rain * 0.95f);

        float sky = getSunAngleDegrees(world, cameraPos) / 360.0f;

        float sun = Mth.cos(sky * (float) (Math.PI * 2)) * 2.0F + 0.5F;
        sun = Mth.clamp(sun, 0.0F, 1.0F);
        color.mul(sun * 0.9f + 0.1f, sun * 0.9f + 0.1f, sun * 0.85f + 0.15f);

        float thunder = world.getThunderLevel(tickDelta);
        color.lerp(new Vector3f(color.dot(Y) * 0.2f), thunder * 0.95f);

        return color;
    }

    public static void gammaToLinear(Vector3f color) {
        color.set((float) Math.pow(color.x, 2.2), (float) Math.pow(color.y, 2.2), (float) Math.pow(color.z, 2.2));
    }

    public static void linearToGamma(Vector3f color) {
        color.set((float) Math.pow(color.x, 1 / 2.2), (float) Math.pow(color.y, 1 / 2.2), (float) Math.pow(color.z, 1 / 2.2));
    }

    public static float getSunAngleDegrees(ClientLevel world, Vector3d cameraPos) {
        return world.environmentAttributes().getValue(EnvironmentAttributes.SUN_ANGLE, new Vec3(cameraPos.x, cameraPos.y, cameraPos.z));
    }

    public static float getSunAngleRadians(ClientLevel world, Vector3d cameraPos) {
        return getSunAngleDegrees(world, cameraPos) * Mth.DEG_TO_RAD;
    }
}
