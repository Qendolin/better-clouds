package com.qendolin.betterclouds.rendering;

import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.jspecify.annotations.NonNull;

public interface CloudRenderer extends AutoCloseable {
    @NonNull PrepareResult prepare(Matrix4f viewMat, Matrix4f projMat, int ticks, float tickDelta, Vector3d cam);

    void render(int ticks, float tickDelta, Vector3d cam, Vector3d frustumPos, Frustum frustum);
}
