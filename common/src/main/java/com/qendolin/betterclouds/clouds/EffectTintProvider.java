package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.clouds.fog.FogProvider;
import com.qendolin.betterclouds.compat.EnhancedCelestialsCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.attribute.EnvironmentAttributes;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector4f;

public abstract class EffectTintProvider {

    private static final Vector3f Y = new Vector3f(0.299f, 0.587f, 0.114f);

    public static Vector3f getEffectTint(MinecraftClient client, @Nullable FogProvider.Fog fog, float tickDelta) {
        if (client.world == null || client.player == null)
            return new Vector3f(1.0f, 1.0f, 1.0f);

        Vector3f cloudColor = getCloudsColor(client.world, tickDelta);

        if (EnhancedCelestialsCompat.instance().isEventActive(client.world)) {
            Vector3f tint = EnhancedCelestialsCompat.instance().getEventTint(client.world);
            tint.div(0.2f, 0.2f, 1.0f); // divide be the default value
            cloudColor.mul(tint);
        }

        gammaToLinear(cloudColor);

        float cloudBaseLuma = cloudColor.dot(Y);
        Vector3f cloudBaseChroma = cloudBaseLuma < 0.0001 ? new Vector3f(1.0f) : new Vector3f(cloudColor).div(cloudBaseLuma);

        float cloudLuma = cloudBaseLuma;
        float moon = MathHelper.clamp(-MathHelper.cos(getSunAngleRadians(client.world)), -0.25f, 0.25f) * 2 + 0.5f;
        float moonSize = EnhancedCelestialsCompat.instance().getMoonSize(client.world);
        cloudLuma += moonSize * moon * 0.65f;

        // CrY - Chroma and Luma
        Vector4f cry = new Vector4f(cloudBaseChroma, cloudLuma);

        if (fog != null) { // Fog ON
            Vector3f fogColor = new Vector3f(fog.red(), fog.green(), fog.blue());
            compositeColor(fogColor, cry);
        }

        cry.w *= 1 / 0.9777f; // The new calculation produces slightly darker clouds, this is a 'fix'
        cry.w = MathHelper.clamp(cry.w, 0, 2);

        float saturation = (float) Math.pow(cry.w, 1 / 2.2);
        Vector3f gray = new Vector3f(cry.w);
        Vector3f desaturated = new Vector3f(cry.x, cry.y, cry.z).mul(saturation)
            .add(new Vector3f(gray).mul(1 - saturation));

        Vector3f result = new Vector3f(desaturated).mul(cry.w).min(new Vector3f(1.0f));
        linearToGamma(result);

        if (client.player != null && client.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            float min = result.get(result.minComponent());
            result.div(MathHelper.lerp(GameRenderer.getNightVisionStrength(client.player, tickDelta), 1.0f, min));
        }
        return result;
    }

    private static void compositeColor(Vector3f color, Vector4f cry) {
        gammaToLinear(color);

        float luma = color.dot(Y);
        Vector3f chroma = luma < 0.0001 ? new Vector3f(1.0f) : new Vector3f(color).div(luma);

        cry.set(
            MathHelper.sqrt(chroma.x * cry.x),
            MathHelper.sqrt(chroma.y * cry.y),
            MathHelper.sqrt(chroma.z * cry.z),
            MathHelper.square(MathHelper.sqrt(luma) + MathHelper.sqrt(cry.w)) / 4
        );
    }

    private static Vector3f getCloudsColor(ClientWorld world, float tickDelta) {
        final Vector3f Y = new Vector3f(0.299f, 0.587f, 0.114f);

        // this is from ClientWorld#getCloudsColor
        Vector3f color = new Vector3f(1.0f);
        float rain = world.getRainGradient(tickDelta);
        color.lerp(new Vector3f(color.dot(Y) * 0.6f), rain * 0.95f);

        float sky = getSunAngleDegrees(world) / 360.0f;

        float sun = MathHelper.cos(sky * (float) (Math.PI * 2)) * 2.0F + 0.5F;
        sun = MathHelper.clamp(sun, 0.0F, 1.0F);
        color.mul(sun * 0.9f + 0.1f, sun * 0.9f + 0.1f, sun * 0.85f + 0.15f);

        float thunder = world.getThunderGradient(tickDelta);
        color.lerp(new Vector3f(color.dot(Y) * 0.2f), thunder * 0.95f);

        return color;
    }

    private static void gammaToLinear(Vector3f color) {
        color.set((float) Math.pow(color.x, 2.2), (float) Math.pow(color.y, 2.2), (float) Math.pow(color.z, 2.2));
    }

    private static void linearToGamma(Vector3f color) {
        color.set((float) Math.pow(color.x, 1 / 2.2), (float) Math.pow(color.y, 1 / 2.2), (float) Math.pow(color.z, 1 / 2.2));
    }

    private static float getSunAngleDegrees(ClientWorld world) {
        return world.getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.SUN_ANGLE_VISUAL);
    }

    private static float getSunAngleRadians(ClientWorld world) {
        return (float) Math.toRadians(getSunAngleDegrees(world));
    }
}
