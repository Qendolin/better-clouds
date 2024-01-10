package com.qendolin.betterclouds.clouds;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class Debug {
    public static int profileInterval = 0;
    public static boolean frustumCulling = false;
    public static boolean generatorPause = false;
    // -1 to disable, 0 to initialize, paused tick otherwise
    public static int animationPause = -1;
    public static boolean generatorForceUpdate = false;

    public static final List<Pair<Box, Boolean>> frustumCulledBoxes = new ArrayList<>();

    public static void clearFrustumCulledBoxed() {
        if (frustumCulling) {
            frustumCulledBoxes.clear();
        } else if (!frustumCulledBoxes.isEmpty()) {
            frustumCulledBoxes.clear();
        }
    }

    public static void addFrustumCulledBox(Box box, boolean visible) {
        if (!frustumCulling) return;
        frustumCulledBoxes.add(new Pair<>(box, visible));
    }

    public static void drawFrustumCulledBoxes(Vector3d cam) {
        if (!frustumCulling) return;
        BufferBuilder vertices = Tessellator.getInstance().getBuffer();
        vertices.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        ShaderProgram prevShader = RenderSystem.getShader();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        for (Pair<Box, Boolean> pair : frustumCulledBoxes) {
            Box box = pair.getLeft();
            if (pair.getRight()) {
                drawBox(cam, vertices, box, 0.6f, 1f, 0.5f, 1f);
            } else {
                drawBox(cam, vertices, box, 1f, 0.6f, 0.5f, 1f);
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
