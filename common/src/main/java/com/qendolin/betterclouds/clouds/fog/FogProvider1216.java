package com.qendolin.betterclouds.clouds.fog;

import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.util.RenderHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.joml.Vector4f;

class FogProvider1216 implements FogProvider {

    public Fog getFogInternal(Minecraft client, Config config, float tickDelta, Camera camera, float distance) {
        assert client.level != null;

        RenderHelper.FogDataAndColor fogDataAndColor = RenderHelper.getFogDataAndColor();
        if (fogDataAndColor == null)
            return null;

        float start = fogDataAndColor.fogData().environmentalStart;
        float end = fogDataAndColor.fogData().cloudEnd;
        Vector4f color = fogDataAndColor.color();

        return new Fog(start, end, color.x, color.y, color.z, color.w);
    }
}
