package com.qendolin.betterclouds.clouds;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL32;

//? if >1.21.4 {
/*import com.mojang.blaze3d.vertex.VertexFormat;
*///?}

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.qendolin.betterclouds.Main.glCompat;

public class Debug {
    public static int profileInterval = 0;
    public static boolean frustumCulling = false;
    public static boolean generatorPause = false;
    // -1 to disable, 0 to initialize, paused tick otherwise
    public static int animationPause = -1;
    public static boolean generatorForceUpdate = false;

    public static final List<Pair<Box, Boolean>> frustumCulledBoxes = new ArrayList<>();

    private static BuiltBufferRenderer renderer = null;

    private static void clearFrustumCulledBoxes() {
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

    public static void render(Resources res, Vector3d cam) {
        if (!frustumCulling) {
            clearFrustumCulledBoxes();
            return;
        }
        if (frustumCulledBoxes.isEmpty()) return;

        if(renderer == null) {
            renderer = new BuiltBufferRenderer();
        }

        glCompat.pushDebugGroupDev("Debug Draw");
        //? if >=1.21 {
        BufferBuilder vertices = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        //?} else {
        /*BufferBuilder vertices = Tessellator.getInstance().getBuffer();
        vertices.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        *///?}
        drawFrustumCulledBoxes(vertices, cam);

        res.debugShader().bind();
        res.debugShader().uModelViewMatrix.setMat4(RenderSystem.getModelViewMatrix());
        res.debugShader().uProjectionMatrix.setMat4(RenderSystem.getProjectionMatrix());

        var built = vertices.endNullable();
        if(built != null) {
            //? if >=1.21 {
            var vertexBuffer = built.getBuffer();
            //?} else {
            /*var vertexBuffer = built.getVertexBuffer();
            *///?}
            renderer.render(vertexBuffer);
            //? if >=1.21
            built.close();
        }

        clearFrustumCulledBoxes();
        glCompat.popDebugGroup();
    }

    private static void drawFrustumCulledBoxes(VertexConsumer vertices, Vector3d cam) {
        for (Pair<Box, Boolean> pair : frustumCulledBoxes) {
            Box box = pair.getLeft();
            if (pair.getRight()) {
                drawBox(cam, vertices, box, 0.6f, 1f, 0.5f, 1f);
            } else {
                drawBox(cam, vertices, box, 1f, 0.6f, 0.5f, 1f);
            }
        }

    }

    public static void drawBox(Vector3d cam, VertexConsumer vertices, Box box, float red, float green, float blue, float alpha) {
        float minX = (float) (box.minX - cam.x);
        float minY = (float) (box.minY - cam.y);
        float minZ = (float) (box.minZ - cam.z);
        float maxX = (float) (box.maxX - cam.x);
        float maxY = (float) (box.maxY - cam.y);
        float maxZ = (float) (box.maxZ - cam.z);
        // x 00
        addLine(vertices, minX, minY, minZ, maxX, minY, minZ, red, green, blue, alpha);
        // x 01
        addLine(vertices, minX, minY, maxZ, maxX, minY, maxZ, red, green, blue, alpha);
        // x 11
        addLine(vertices, minX, maxY, maxZ, maxX, maxY, maxZ, red, green, blue, alpha);
        // x 10

        addLine(vertices, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
        // y 00
        addLine(vertices, minX, minY, minZ, minX, maxY, minZ, red, green, blue, alpha);
        // y 01
        addLine(vertices, minX, minY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
        // y 11
        addLine(vertices, maxX, minY, maxZ, maxX, maxY, maxZ, red, green, blue, alpha);
        // y 10
        addLine(vertices, maxX, minY, minZ, maxX, maxY, minZ, red, green, blue, alpha);

        // z 00
        addLine(vertices, minX, minY, minZ, minX, minY, maxZ, red, green, blue, alpha);
        // z 01
        addLine(vertices, minX, maxY, minZ, minX, maxY, maxZ, red, green, blue, alpha);
        // z 11
        addLine(vertices, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
        // z 10
        addLine(vertices, maxX, minY, minZ, maxX, minY, maxZ, red, green, blue, alpha);
    }

    private static void addLine(VertexConsumer vertices, float x1, float y1, float z1, float x2, float y2, float z2, float red, float green, float blue, float alpha) {
        float nx = x2 - x1;
        float ny = y2 - y1;
        float nz = z2 - z1;
        float l = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        nx /= l;
        ny /= l;
        nz /= l;
        addVertex(vertices, x1, y1, z1, nx, ny, nz, red, green, blue, alpha);
        addVertex(vertices, x2, y2, z2, nx, ny, nz, red, green, blue, alpha);
    }

    private static void addVertex(VertexConsumer vertexConsumer, float x, float y, float z, float nx, float ny, float nz, float red, float green, float blue, float alpha) {
        //? if >=1.21 {
        vertexConsumer.vertex(x, y, z).color(red, green, blue, alpha);
        //?} else
        /*vertexConsumer.vertex(x, y, z).color(red, green, blue, alpha).next();*/
    }

    private static class BuiltBufferRenderer {
        private final int vboId;
        private int vboSize = 0;
        private final int vaoId;

        public BuiltBufferRenderer() {
            int prevVao = GL32.glGetInteger(GL32.GL_VERTEX_ARRAY_BINDING);
            vaoId = GL32.glGenVertexArrays();
            GL32.glBindVertexArray(vaoId);
            glCompat.objectLabelDev(glCompat.GL_VERTEX_ARRAY, vaoId, "debug_vao");
            GL32.glEnableVertexAttribArray(0);
            GL32.glEnableVertexAttribArray(1);

            vboId = GL32.glGenBuffers();
            GL32.glBindBuffer(GL32.GL_ARRAY_BUFFER, vboId); // realize id
            glCompat.objectLabelDev(glCompat.GL_BUFFER, vboId, "debug_vbo");
            GL32.glBindBuffer(GL32.GL_ARRAY_BUFFER, 0);

            GL32.glBindVertexArray(prevVao);
        }

        public void render(ByteBuffer buffer) {
            if(buffer.remaining() == 0) return;

            int prevVao = GL32.glGetInteger(GL32.GL_VERTEX_ARRAY_BINDING);

            GL32.glBindVertexArray(vaoId);
            GL32.glBindBuffer(GL32.GL_ARRAY_BUFFER, vboId);

            if(buffer.remaining() > vboSize) {
                vboSize = buffer.remaining();
                GL32.glBufferData(GL32.GL_ARRAY_BUFFER, buffer, GL32.GL_STREAM_DRAW);
                int stride = 3 * Float.BYTES + 4;
                // Position
                GL32.glVertexAttribPointer(0, 3, GL32.GL_FLOAT, false, stride, 0);
                // Color
                GL32.glVertexAttribPointer(1, 4, GL32.GL_UNSIGNED_BYTE, true, stride, 3 * Float.BYTES);
            } else {
                GL32.glBufferSubData(GL32.GL_ARRAY_BUFFER, 0, buffer);
            }

            GL32.glDrawArrays(GL32.GL_LINES, 0, buffer.remaining());
            GL32.glBindVertexArray(prevVao);
        }
    }

    public static boolean isProfilingEnabled() {
        return profileInterval > 0;
    }
}
