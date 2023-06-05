package com.qendolin.betterclouds.clouds;

import com.mojang.blaze3d.systems.RenderSystem;
import com.qendolin.betterclouds.Main;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Box;
import org.joml.Vector3d;

public class Debug {
    public static int profileInterval = 0;
    public static boolean frustumCulling = false;
    public static boolean generatorPause = false;

    public static void drawFrustumCulling(Vector3d cam, Frustum frustum, Vector3d frustumPos, ChunkedGenerator generator, float cloudsHeight) {
        BufferBuilder vertices = Tessellator.getInstance().getBuffer();
        vertices.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        ShaderProgram prevShader = RenderSystem.getShader();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Frustum frustumAtOrigin = new Frustum(frustum);
        frustumAtOrigin.setPosition(frustumPos.x - generator.originX(), frustumPos.y, frustumPos.z - generator.originZ());
        for (ChunkedGenerator.ChunkIndex chunk : generator.chunks()) {
            Box bounds = chunk.bounds(cloudsHeight, Main.getConfig().sizeXZ, Main.getConfig().sizeY);
            if (frustumAtOrigin.isVisible(bounds)) {
                drawBox(cam, vertices, bounds, 0.6f, 1f, 0.5f, 1f);
            } else {
                drawBox(cam, vertices, bounds, 1f, 0.6f, 0.5f, 1f);
            }
        }
        Tessellator.getInstance().draw();
        RenderSystem.setShader(() -> prevShader);
    }

    public static void drawBox(Vector3d cam, VertexConsumer vertexConsumer, Box box, float red, float green, float blue, float alpha) {
        // I was having some issues with WorldRenderer#drawBox and sodium, so I've copied a modified version here
        float minX = (float) (box.minX - cam.x);
        float minY = (float) (box.minY - cam.y);
        float minZ = (float) (box.minZ - cam.z);
        float maxX = (float) (box.maxX - cam.x);
        float maxY = (float) (box.maxY - cam.y);
        float maxZ = (float) (box.maxZ - cam.z);
        vertexConsumer.vertex(minX, minY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, minY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, minY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, maxY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, minY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, minY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, minY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, maxY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, maxY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, maxY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, maxY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, maxY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, maxY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, minY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, minY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, minY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, minY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, minY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, maxY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, maxY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, minY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, maxY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, maxY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, maxY, maxZ).color(red, green, blue, alpha).next();
    }
}
