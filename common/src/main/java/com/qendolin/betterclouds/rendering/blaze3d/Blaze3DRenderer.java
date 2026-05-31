package com.qendolin.betterclouds.rendering.blaze3d;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.generator.ChunkedGenerator;
import com.qendolin.betterclouds.mixin.provider.CloudinessProvider;
import com.qendolin.betterclouds.rendering.CloudRenderer;
import com.qendolin.betterclouds.rendering.PrepareResult;
import com.qendolin.betterclouds.rendering.opengl.Debug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FogType;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.qendolin.betterclouds.compat.ProfilerWrapper.getProfiler;

/**
 * Rendering is hard
 */
public class Blaze3DRenderer extends CloudRenderer {
    final float[] CUBE_VERTICES = {
            // x,    y,    z
            -0.5f, -0.5f, -0.5f, // 0: left  bottom back
            0.5f, -0.5f, -0.5f, // 1: right bottom back
            0.5f, 0.5f, -0.5f, // 2: right top    back
            -0.5f, 0.5f, -0.5f, // 3: left  top    back

            -0.5f, -0.5f, 0.5f, // 4: left  bottom front
            0.5f, -0.5f, 0.5f, // 5: right bottom front
            0.5f, 0.5f, 0.5f, // 6: right top    front
            -0.5f, 0.5f, 0.5f  // 7: left  top    front
    };
    final short[] CUBE_INDICES = {
            // back face, z = -1
            0, 1, 2,
            2, 3, 0,

            // front face, z = +1
            4, 5, 6,
            6, 7, 4,

            // left face, x = -1
            4, 0, 3,
            3, 7, 4,

            // right face, x = +1
            1, 5, 6,
            6, 2, 1,

            // bottom face, y = -1
            4, 5, 1,
            1, 0, 4,

            // top face, y = +1
            3, 2, 6,
            6, 7, 3
    };

    final VertexFormat POSITION_FORMAT = VertexFormat.builder(1)
            .addAttribute("WorldPosition", GpuFormat.RGB32_FLOAT)    // xyz position of cube center (world)
            .build();
    final VertexFormat MODEL_FORMAT = VertexFormat.builder(1)
            .addAttribute("LocalPosition", GpuFormat.RGB32_FLOAT)    // xyz position of vertex in cube model (local)
            .build();
    final BindGroupLayout SHADER_PARAMS = BindGroupLayout.builder()
            .withUniform("uTint", UniformType.UNIFORM_BUFFER)
            .build();
    final RenderPipeline CLOUD_PIPELINE = RenderPipeline.builder()
            .withLocation(Identifier.fromNamespaceAndPath(BetterCloudsStatic.MODID, "blaze3DRenderer"))
            .withVertexShader(Identifier.fromNamespaceAndPath(BetterCloudsStatic.MODID, "shaders/blaze3d/clouds.vsh"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(BetterCloudsStatic.MODID, "shaders/blaze3d/clouds.fsh"))
            .withVertexBinding(0, POSITION_FORMAT)
            .withVertexBinding(1, MODEL_FORMAT)
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .withBindGroupLayout(SHADER_PARAMS)
            .withCull(false)
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .build();

    private ChunkedGenerator generator;
    private GpuBuffer modelBuffer, indexBuffer, cloudPositionsBuffer;

    public Blaze3DRenderer(Minecraft client) {
        super(client);
        buildModelBuffers();
    }

    private static GpuDevice gpu() {
        return RenderSystem.getDevice();
    }

    @Override
    public @NonNull PrepareResult prepare(Matrix4f viewMat, Matrix4f projMat, int rendererTicks, float tickDelta, Vector3d cam) {
        getProfiler().popPush("render_setup");

        if (client.gameRenderer.mainCamera().getFluidInCamera() != FogType.NONE) {
            return PrepareResult.NO_RENDER;
        }

        float cloudiness = CloudinessProvider.getCloudiness(level, tickDelta);
        Config options = ConfigManager.instance();

        generator.update(cam, options.getCloudTicks(client, rendererTicks), rendererTicks, tickDelta, options, cloudiness);
        if (generator.canSwap()) {
            getProfiler().popPush("swap");
            generator.swap();
            getProfiler().popPush("render_setup");
        }
        if (generator.canGenerate() && !generator.generating() && !Debug.generatorPause) {
            getProfiler().popPush("generate_clouds");
            generator.generate();
            updateCloudPositionsBuffer();
            getProfiler().popPush("render_setup");
        }

        if (cloudPositionsBuffer == null || cloudPositionsBuffer.size() == 0) {
            return PrepareResult.NO_RENDER;
        }

        updateCloudHeight(cam);
        return PrepareResult.RENDER;
    }

    @Override
    public void render(int ticks, float tickDelta, Vector3d cam, Vector3d frustumPos, Frustum frustum) {
        startTiming();
        getProfiler().popPush("render_setup");
        stopTiming();
    }

    public ChunkedGenerator getGenerator() {
        return generator;
    }

    public void closeGpuBuffer(GpuBuffer buffer) {
        if (buffer != null)
            buffer.close();
    }

    public void updateCloudPositionsBuffer() {
        closeGpuBuffer(cloudPositionsBuffer);

        ByteBuffer pb = ByteBuffer.allocateDirect(generator.points().size() * 3 * Float.BYTES)
                .order(ByteOrder.nativeOrder());
        for (ChunkedGenerator.Point p : generator.points()) {
            pb.putFloat(p.x());
            pb.putFloat(p.y());
            pb.putFloat(p.z());
        }
        pb.flip();

        cloudPositionsBuffer = gpu().createBuffer(
                () -> "cloud_positions",
                GpuBuffer.USAGE_VERTEX,
                pb
        );
    }

    public void buildModelBuffers() {
        closeGpuBuffer(modelBuffer);
        closeGpuBuffer(indexBuffer);

        ByteBuffer vb = ByteBuffer.allocateDirect(CUBE_VERTICES.length * Float.BYTES)
                .order(ByteOrder.nativeOrder());
        for (float u : CUBE_VERTICES) vb.putFloat(u);
        vb.flip();

        modelBuffer = gpu().createBuffer(
                () -> "cube_vertices",
                GpuBuffer.USAGE_VERTEX,
                vb
        );

        ByteBuffer ib = ByteBuffer.allocateDirect(CUBE_INDICES.length * Short.BYTES)
                .order(ByteOrder.nativeOrder());
        for (short s : CUBE_INDICES) ib.putShort(s);
        ib.flip();

        indexBuffer = gpu().createBuffer(
                () -> "cube_element_indices",
                GpuBuffer.USAGE_INDEX,
                ib
        );
    }

    @Override
    public void close() {
        super.close();
        generator.close();
        closeGpuBuffer(modelBuffer);
        closeGpuBuffer(indexBuffer);
        closeGpuBuffer(cloudPositionsBuffer);
    }
}
