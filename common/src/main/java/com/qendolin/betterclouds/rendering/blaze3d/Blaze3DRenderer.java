package com.qendolin.betterclouds.rendering.blaze3d;

import com.mojang.blaze3d.*;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.*;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.compat.*;
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.generator.ChunkedGenerator;
import com.qendolin.betterclouds.mixin.provider.EffectTintProvider;
import com.qendolin.betterclouds.mixin.provider.FogProvider;
import com.qendolin.betterclouds.rendering.*;
import com.qendolin.betterclouds.rendering.opengl.Debug;
import com.qendolin.betterclouds.rendering.opengl.Resources;
import com.qendolin.betterclouds.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.jspecify.annotations.NonNull;

import java.lang.Math;
import java.util.Optional;
import java.util.OptionalDouble;

import static com.qendolin.betterclouds.compat.ProfilerWrapper.getProfiler;

/**
 * Rendering is hard
 */
public class Blaze3DRenderer extends CloudRenderer {
    public static final float[] CUBE_VERTICES = {
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
    public static final short[] CUBE_INDICES = {
            // back face, z = -1
            2, 1, 0,
            0, 3, 2,

            // front face, z = +1
            4, 5, 6,
            6, 7, 4,

            // left face, x = -1
            3, 0, 4,
            4, 7, 3,

            // right face, x = +1
            6, 5, 1,
            1, 2, 6,

            // bottom face, y = -1
            1, 5, 4,
            4, 0, 1,

            // top face, y = +1
            6, 2, 3,
            3, 7, 6
    };

    static final VertexFormat MODEL_FORMAT = VertexFormat.builder(0)
            .addAttribute("LocalPosition", GpuFormat.RGB32_FLOAT)    // xyz position of vertex in cube model (local)
            .build();
    static final VertexFormat POSITION_FORMAT = VertexFormat.builder(1)
            .addAttribute("WorldPosition", GpuFormat.RGB32_FLOAT)    // xyz position of cube center (world)
            .build();
    static final BindGroupLayout SHADER_BIND_GROUP = BindGroupLayout.builder()
            .withSampler("NoiseTexture")
            .withSampler("LightTexture")
            .withUniform("CloudVertexData", UniformType.UNIFORM_BUFFER)
            .withUniform("CloudFragData", UniformType.UNIFORM_BUFFER)
            .build();
    static final BindGroupLayout LOD_BIND_GROUP = BindGroupLayout.builder()
            .withSampler("LodDepthTexture")
            .withUniform("LodProjMat", UniformType.UNIFORM_BUFFER)
            .build();
    // models
    private final ReadOnlyBuffer modelVertexBuffer = new ReadOnlyBuffer("cloudModelVertices");
    // don't forget to close your buffers!
    private final ReadOnlyBuffer modelIndexBuffer = new ReadOnlyBuffer("cloudModelIndices");
    // cloud position xyz, capacity can change, so must recreate every time cloud positions change
    private final ReadOnlyBuffer worldCloudPosBuffer = new ReadOnlyBuffer("cloudPositions");
    // uniforms
    private final WritableBuffer uCloudVertexData = new WritableBuffer("uCloudVertexData", Float.BYTES * 16, GpuBuffer.USAGE_UNIFORM);
    private final WritableBuffer uCloudFragData = new WritableBuffer("uCloudFragData", Float.BYTES * 15, GpuBuffer.USAGE_UNIFORM);
    private final WritableBuffer uLodProjMat = new WritableBuffer("uLodProjMat", Float.BYTES * 16, GpuBuffer.USAGE_UNIFORM);
    // things affected by resource reload
    RenderPipeline CLOUD_RENDERER_PIPELINE;
    TextureWrapper noiseTexture, lightTexture;
    // misc
    private final float[] tempMatrixCopyArr = new float[16];

    public Blaze3DRenderer() {
        createModelBuffers();
    }

    private static GpuDevice gpu() {
        return RenderSystem.getDevice();
    }

    public static boolean isCloudsOpaque() {
        return ConfigManager.instance().shaderPreset().opacity > 0.975;
    }

    @Override
    public void setLevel(ClientLevel level) {
        if (level == null) return;
        super.setLevel(level);
        reloadGenerator();
    }

    public void reload(ResourceManager manager) {
        TextureWrapper.closeAllWrappers();
        buildRenderPipeline();
        loadTextures();
    }

    private void loadTextures() {
        noiseTexture = TextureWrapper.fromMcTexture(
                "NoiseTexture", Resources.NOISE_TEXTURE,
                () -> TextureWrapper.customSampler(AddressMode.REPEAT, AddressMode.REPEAT, FilterMode.LINEAR)
        );
        lightTexture = TextureWrapper.fromMcTexture(
                "LightTexture", Resources.LIGHTING_TEXTURE,
                () -> TextureWrapper.customSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.REPEAT, FilterMode.LINEAR)
        );
    }

    @Override
    public @NonNull PrepareResult prepare(Matrix4f viewMat, Matrix4f projMat, long cloudTicks, long clientTicks, float tickDelta, Vector3d cam) {
        getProfiler().popPush("render_setup");

        if (PipelineParams.paramsChanged()) {
            BetterCloudsStatic.getLogger().info("Pipeline parameters changed, reloading pipeline");
            BetterCloudsStatic.getLogger().info("Current: " + PipelineParams.prevParams);
            reload(null);
        }

        if (worldCloudPosBuffer.size() <= 0) return PrepareResult.NO_RENDER;
        return PrepareResult.RENDER;
    }

    @Override
    public void render(long cloudTicks, float tickDelta, Vector3d cam, Vector3d frustumPos, Frustum frustum) {
        getProfiler().popPush("render_setup");

        Config config = ConfigManager.instance();
        Config generatorConfig = getGeneratorConfig();
        var sp = config.shaderPreset();
        FogProvider.Fog fog = FogProvider.instance.getFog(client, config, tickDelta);

        float cloudTimeSeconds = cloudTicks / 20f + tickDelta / 20f;
        float dayNightFactor = MathUtil.interpolateDayNightFactor(dayTime(), config.shaderPreset().sunriseStartTime, config.shaderPreset().sunriseEndTime, config.shaderPreset().sunsetStartTime, config.shaderPreset().sunsetEndTime);
        float brightness = (1 - dayNightFactor) * config.shaderPreset().nightBrightness + dayNightFactor * config.shaderPreset().dayBrightness;
        Vector3f effectTint = EffectTintProvider.getEffectTint(client, fog, tickDelta, cam);

        float skyAngleRad = EffectTintProvider.getSunAngleRadians(level, cam);
        float sunPathAngleRad = config.shaderPreset().sunPathAngle * Mth.DEG_TO_RAD;
        float sunAxisY = Mth.sin(sunPathAngleRad);
        float sunAxisZ = Mth.cos(sunPathAngleRad);
        Vector3f sunDir = new Vector3f(1, 0, 0).rotateAxis(skyAngleRad + Mth.HALF_PI, 0, sunAxisY, sunAxisZ);
        float mappedTime = MathUtil.mapTimeOfDay(dayTime(), config.shaderPreset().sunriseStartTime, config.shaderPreset().sunriseEndTime, config.shaderPreset().sunsetStartTime, config.shaderPreset().sunsetEndTime);

        MoonPhase moonPhase = level.environmentAttributes().getValue(EnvironmentAttributes.MOON_PHASE, new Vec3(cam.x, cam.y, cam.z));

        // larger value = smaller halo size
        float haloSize = dayTime() < config.shaderPreset().sunsetEndTime ?
                2.05f - config.sunHaloSizeMultiplier :
                (float) (5 * Math.pow(0.06217, config.moonHaloSizeMultiplier * DimensionType.MOON_BRIGHTNESS_PER_PHASE[moonPhase.index()]));    // curve fit for ideal moon halo size based on phase

        uCloudVertexData.write(b -> {
            // size and time
            b.putFloat(config.sizeXZ);
            b.putFloat(config.sizeY);

            b.putFloat(cloudTimeSeconds);
            b.putFloat(sp.colorTransitionEnd);

            // cloud matrix origin position
            b.putFloat((float) -generator.renderOriginX(cam.x));
            b.putFloat((float) cam.y - cloudHeight);
            b.putFloat((float) -generator.renderOriginZ(cam.z));

            // camera position (for sampling the noise texture)
            b.putFloat((float) cam.x);
            b.putFloat((float) cam.z);

            b.putFloat(generatorConfig.blockDistance() - generatorConfig.chunkSize / 2f);
            b.putFloat(generatorConfig.yRange + config.sizeY);
            b.putFloat(config.scaleFalloffMin);

            b.putFloat(config.windEffectFactor);
            b.putFloat(config.windSpeedFactor);

            if (fog == null) {
                // fog off, just use blockDistance
                b.putFloat(config.blockDistance() - 8);
                b.putFloat(config.blockDistance());
            } else {
                // fog start and end
                b.putFloat(fog.start());
                b.putFloat(fog.end());
            }
        });
        uCloudFragData.write(b -> {
            b.putFloat(sp.opacity);
            b.putFloat(sp.opacityFactor);
            b.putFloat(sp.opacityExponent);
            b.putFloat(brightness);

            b.putFloat(sp.topColorRed * effectTint.x);
            b.putFloat(sp.topColorGreen * effectTint.y);
            b.putFloat(sp.topColorBlue * effectTint.z);

            b.putFloat(sp.bottomColorRed * effectTint.x);
            b.putFloat(sp.bottomColorGreen * effectTint.y);
            b.putFloat(sp.bottomColorBlue * effectTint.z);

            b.putFloat(haloSize);

            b.putFloat(sunDir.x);
            b.putFloat(sunDir.y);
            b.putFloat(sunDir.z);
            b.putFloat(mappedTime / 24000);
        });

        if (DhCompat.instance().isEnabled()) {
            DhCompat.instance().getProjectionMatrix().get(tempMatrixCopyArr);
            uLodProjMat.write(b -> {
                for (float value : tempMatrixCopyArr) {
                    b.putFloat(value);
                }
            });
        }
        else if (VoxyCompat.instance.isEnabled()) {
            VoxyCompat.instance.getProjectionMatrix().get(tempMatrixCopyArr);
            uLodProjMat.write(b -> {
                for (float value: tempMatrixCopyArr) {
                    b.putFloat(value);
                }
            });
        }

        getProfiler().popPush("render_clouds");
        RenderTarget cloudsTarget = client.levelRenderer.cloudsTarget();
        if (cloudsTarget == null)
            cloudsTarget = client.gameRenderer.mainRenderTarget();
        if (cloudsTarget.getColorTextureView() == null) {
            // idk this should never happen
            return;
        }

        GpuBufferSlice dynamicTransform = RenderSystem.getDynamicUniforms().writeTransform(
                createCloudModelViewMatrix(cam),
                new Vector4f(1, sp.topColorRed, sp.topColorGreen, sp.topColorBlue)
        );

        IrisFramebuffer.begin();

        try (RenderPass pass = gpu().createCommandEncoder().createRenderPass(
                () -> BetterCloudsStatic.MODID + ":" + "renderClouds",
                cloudsTarget.getColorTextureView(),
                Optional.empty(),
                cloudsTarget.getDepthTextureView(),
                OptionalDouble.empty()
        )) {
            pass.setPipeline(CLOUD_RENDERER_PIPELINE);

            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("CloudVertexData", uCloudVertexData.gpuBuffer());
            pass.setUniform("CloudFragData", uCloudFragData.gpuBuffer());
            pass.setUniform("DynamicTransforms", dynamicTransform);
            pass.setUniform("LodProjMat", uLodProjMat.gpuBuffer());

            noiseTexture.bindTo(pass);
            lightTexture.bindTo(pass);

            TextureWrapper dhDepthTexture = DhCompat.instance().getDepthTexture();
            TextureWrapper voxyDepthTexture = VoxyCompat.instance.getOpaqueDepthTexture();
            if (dhDepthTexture != null)
                dhDepthTexture.bindTo(pass);
            else if (voxyDepthTexture != null)
                voxyDepthTexture.bindTo(pass);

            pass.setVertexBuffer(0, modelVertexBuffer.gpuBuffer().slice());
            pass.setVertexBuffer(1, worldCloudPosBuffer.gpuBuffer().slice());
            pass.setIndexBuffer(modelIndexBuffer.gpuBuffer(), IndexType.SHORT);

            frustum.prepare(frustumPos.x - generator.originX(), frustumPos.y, frustumPos.z - generator.originZ());

            if (!config.useFrustumCulling || !gpu().getDeviceInfo().features().nonZeroFirstInstance())
                pass.drawIndexed(CUBE_INDICES.length, generator.points().size(), 0, 0, 0);
            else
                drawWithFrustumCulling(pass, frustum);
        }

        IrisFramebuffer.end();
    }

    private Config getGeneratorConfig() {
        Config config = generator.config();
        if (config != null) return config;
        return ConfigManager.instance();
    }

    public void buildRenderPipeline() {
        PipelineParams params = PipelineParams.getParameters();
        CLOUD_RENDERER_PIPELINE = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(BetterCloudsStatic.MODID, "blaze_3d_renderer"))
                .withVertexShader(Identifier.fromNamespaceAndPath(BetterCloudsStatic.MODID, "blaze3d/clouds"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(BetterCloudsStatic.MODID, "blaze3d/clouds"))
                .withVertexBinding(0, MODEL_FORMAT)
                .withVertexBinding(1, POSITION_FORMAT)
                .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                .withShaderDefine("CELESTIAL_BODY_HALO", params.celestialBodyHalo() ? 1 : 0)
                .withShaderDefine("NEAR_CLOUD_FADE", params.nearCloudFade() ? 1 : 0)
                .withShaderDefine("NEAR_FADE_DIST", 40)
                .withShaderDefine("LOD_ENABLED", params.distantHorizons() || params.voxy() ? 1 : 0)
                .withShaderDefine("REVERSE_Z", params.iris() ? 0 : 1)
                .withShaderDefine("Z_NEG1_TO_1", params.zNeg1To1() ? 1 : 0)
                .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                .withBindGroupLayout(SHADER_BIND_GROUP)
                .withBindGroupLayout(LOD_BIND_GROUP)
                .withCull(false)
                .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, params.cloudsOpaque()))
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .build();
    }

    private void drawWithFrustumCulling(RenderPass pass, Frustum frustumAtOrigin) {
        // This algorithm loops over chunks, which are in a line-by-line order.
        // When a visible chunk is found it's marked as a run start. The run continues until
        // the next non-visible chunk is found. At the end of a run the entire run is rendered as once.
        // This is possible due to the memory layout of the instance buffers.
        Config config = ConfigManager.instance();
        Debug.frustumCulledBoxes.clear();

        int runStart = -1;
        int runCount = 0;
        for (ChunkedGenerator.ChunkIndex chunk : generator.chunks()) {
            AABB bounds = chunk.bounds(cloudHeight, config.sizeXZ, config.sizeY);
            if (!frustumAtOrigin.isVisible(bounds)) {
                Debug.addFrustumCulledBox(bounds, false);
                if (runCount != 0) {
                    pass.drawIndexed(CUBE_INDICES.length, runCount, 0, 0, runStart);
                }
                runStart = -1;
                runCount = 0;
            } else {
                Debug.addFrustumCulledBox(bounds, true);
                if (runStart == -1) runStart = chunk.start();
                runCount += chunk.count();
            }
        }
        if (runCount != 0) {
            pass.drawIndexed(CUBE_INDICES.length, runCount, 0, 0, runStart);
        }
        if (Debug.frustumCulling) {
            try (var ignored = Minecraft.getInstance().levelRenderer.collectPerFrameRenderThreadGizmos()) {
                int visible = 0xFF99FF80;
                int culled = 0xFFFF9977;

                for (Pair<AABB, Boolean> box : Debug.frustumCulledBoxes) {
                    Gizmos.cuboid(box.getFirst(), GizmoStyle.stroke(box.getSecond() ? visible : culled, 1.0f))
                            .setAlwaysOnTop();
                }
            }
        }
    }

    public void uploadPointsToBuffer() {
        if (generator.points().isEmpty()) return;

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

    /**
     * Aligns the model view matrix with the generator origin
     */
    private Matrix4f createCloudModelViewMatrix(Vector3d cam) {
        Matrix4f modelView = new Matrix4f(CloudRenderCoordinator.instance.capturedViewMat);
        modelView.m33(0);
        modelView.m23(0);
        modelView.m13(0);
        modelView.m03(0);
        modelView.translate((float) generator.renderOriginX(cam.x), (float) (cloudHeight - cam.y), (float) generator.renderOriginZ(cam.z));
        modelView.m33(1);
        return modelView;
    }

    @Override
    public void onClose() {
        generator.close();
        modelVertexBuffer.close();
        modelIndexBuffer.close();
        worldCloudPosBuffer.close();
        uCloudVertexData.close();
        uCloudFragData.close();
        uLodProjMat.close();
        noiseTexture.close();
        lightTexture.close();
    }

    public void createModelBuffers() {
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
}
