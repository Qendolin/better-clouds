package com.qendolin.betterclouds.rendering.blaze3d;

import com.mojang.blaze3d.*;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.*;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.config.compat.ShaderPresetConfig;
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

import java.util.Optional;
import java.util.OptionalDouble;

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
            .withUniform("uTime", UniformType.UNIFORM_BUFFER)
            .withUniform("uPartialTime", UniformType.UNIFORM_BUFFER)
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

    private final ChunkedGenerator generator = new ChunkedGenerator(getWorldSeed());

    // models
    private final ReadOnlyBuffer modelVertexBuffer = new ReadOnlyBuffer("cloudModelVertices");
    private final ReadOnlyBuffer modelIndexBuffer = new ReadOnlyBuffer("cloudModelIndices");

    // cloud position xyz, capacity can change, so must recreate every time cloud positions change
    private final ReadOnlyBuffer worldCloudPosBuffer = new ReadOnlyBuffer("cloudPositions");

    // Uniforms
    private final WritableBuffer uTimeBuffer = new WritableBuffer("uTime", Float.BYTES, GpuBuffer.USAGE_UNIFORM);
    private final WritableBuffer uPartialTimeBuffer = new WritableBuffer("uPartialTime", Float.BYTES, GpuBuffer.USAGE_UNIFORM);
    private final WritableBuffer uTintBuffer = new WritableBuffer("uTint", Float.BYTES * 4, GpuBuffer.USAGE_UNIFORM);     // rgba
    private RenderTarget cloudsTarget;

    public Blaze3DRenderer(Minecraft client) {
        super(client);
        reloadModelBuffers();
        reloadOptionBuffers();
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

        cloudsTarget = client.levelRenderer.cloudsTarget();
        if (worldCloudPosBuffer.size() == 0 || cloudsTarget == null) {
            return PrepareResult.NO_RENDER;
        }

        updateCloudHeight(cam);
        return PrepareResult.RENDER;
    }

    public ChunkedGenerator getGenerator() {
        return generator;
    }

    @Override
    public void render(int ticks, float tickDelta, Vector3d cam, Vector3d frustumPos, Frustum frustum) {
        startTiming();

        getProfiler().popPush("render_setup");
        uTimeBuffer.write(b -> b.putFloat(ticks));
        uPartialTimeBuffer.write(b -> b.putFloat(tickDelta));

        getProfiler().popPush("render_clouds");
        try (RenderPass pass = gpu().createCommandEncoder().createRenderPass(
                () -> BetterCloudsStatic.MODID + ":" + "renderClouds",
                cloudsTarget.getColorTextureView(),
                Optional.empty(),
                cloudsTarget.getDepthTextureView(),
                OptionalDouble.empty()
        )) {
            pass.setPipeline(CLOUD_PIPELINE);
            pass.setUniform("uTime", uTimeBuffer.gpuBuffer());
            pass.setUniform("uPartialTime", uPartialTimeBuffer.gpuBuffer());
            pass.setUniform("uTint", uTintBuffer.gpuBuffer());
            pass.setVertexBuffer(0, modelVertexBuffer.gpuBuffer().slice());
            pass.setVertexBuffer(1, worldCloudPosBuffer.gpuBuffer().slice());
            pass.setIndexBuffer(modelIndexBuffer.gpuBuffer(), IndexType.SHORT);
        }
        stopTiming();
    }

    public void updateCloudPositionsBuffer() {
        worldCloudPosBuffer.recreate(
                generator.points().size() * 3 * Float.BYTES,
                GpuBuffer.USAGE_VERTEX,
                b -> {
                    for (ChunkedGenerator.Point p : generator.points()) {
                        b.putFloat(p.x());
                        b.putFloat(p.y());
                        b.putFloat(p.z());
                    }
                }
        );
    }

    @Override
    public void close() {
        super.close();
        generator.close();
        modelVertexBuffer.close();
        modelIndexBuffer.close();
        worldCloudPosBuffer.close();
    }

    public void reloadModelBuffers() {
        modelVertexBuffer.recreate(
                CUBE_VERTICES.length * Float.BYTES,
                GpuBuffer.USAGE_VERTEX,
                b -> {
                    for (float f : CUBE_VERTICES)
                        b.putFloat(f);
                }
        );

        modelIndexBuffer.recreate(
                CUBE_INDICES.length * Short.BYTES,
                GpuBuffer.USAGE_INDEX,
                b -> {
                    for (short s : CUBE_INDICES)
                        b.putShort(s);
                }
        );
    }

    public void reloadOptionBuffers() {
        Config options = ConfigManager.instance();
        ShaderPresetConfig sp = options.shaderPreset();

        uTintBuffer.write(b -> {
            b.putFloat(sp.tintRed);
            b.putFloat(sp.tintGreen);
            b.putFloat(sp.tintBlue);
        });
    }
}
