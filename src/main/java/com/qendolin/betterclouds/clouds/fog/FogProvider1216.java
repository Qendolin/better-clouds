package com.qendolin.betterclouds.clouds.fog;

//? if >=1.21.6 {
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.util.RenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import org.joml.Vector4f;

class FogProvider1216 implements FogProvider {

    public Fog getFogInternal(MinecraftClient client, Config config, float tickDelta, Camera camera, float distance) {
        assert client.world != null;

        RenderHelper.FogDataAndColor fogDataAndColor = RenderHelper.getFogDataAndColor();
        if (fogDataAndColor == null)
            return null;

        float start = fogDataAndColor.fogData().environmentalStart;
        float end = fogDataAndColor.fogData().cloudEnd;
        Vector4f color = fogDataAndColor.color();

        return new Fog(start, end, color.x, color.y, color.z, color.w);
    }
}

//?}