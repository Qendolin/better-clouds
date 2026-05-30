package com.qendolin.betterclouds.rendering.blaze3d;

import com.qendolin.betterclouds.rendering.CloudRenderer;
import com.qendolin.betterclouds.rendering.PrepareResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.material.FogType;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.jspecify.annotations.NonNull;

public class Blaze3DRenderer extends CloudRenderer {
    public Blaze3DRenderer(Minecraft client) {
        super(client);
    }

    @Override
    public @NonNull PrepareResult prepare(Matrix4f viewMat, Matrix4f projMat, int ticks, float tickDelta, Vector3d cam) {
        if (client.gameRenderer.mainCamera().getFluidInCamera() != FogType.NONE) {
            return PrepareResult.NO_RENDER;
        }

        return PrepareResult.FALLBACK;
    }

    @Override
    public void render(int ticks, float tickDelta, Vector3d cam, Vector3d frustumPos, Frustum frustum) {

    }

    @Override
    public void reload(ResourceManager resourceManager) {

    }

    @Override
    public void close() {

    }
}
