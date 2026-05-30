package com.qendolin.betterclouds.rendering.blaze3d;

import com.qendolin.betterclouds.rendering.CloudRenderer;
import com.qendolin.betterclouds.rendering.PrepareResult;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.jspecify.annotations.NonNull;

public class Blaze3DRenderer implements CloudRenderer {
    @Override
    public @NonNull PrepareResult prepare(Matrix4f viewMat, Matrix4f projMat, int ticks, float tickDelta, Vector3d cam) {
        return PrepareResult.FALLBACK;
    }

    @Override
    public void render(int ticks, float tickDelta, Vector3d cam, Vector3d frustumPos, Frustum frustum) {

    }

    @Override
    public void close() throws Exception {

    }
}
