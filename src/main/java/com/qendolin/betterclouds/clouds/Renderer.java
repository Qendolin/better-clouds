package com.qendolin.betterclouds.clouds;

import com.mojang.blaze3d.systems.RenderSystem;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.clouds.shaders.ShaderParameters;
import com.qendolin.betterclouds.compat.DistantHorizonsCompat;
import com.qendolin.betterclouds.compat.IrisCompat;
import com.qendolin.betterclouds.config.Config;
import com.qendolin.betterclouds.config.ConfigManager;
import com.qendolin.betterclouds.renderdoc.RenderDoc;
import com.qendolin.betterclouds.util.ChatUtil;
import com.qendolin.betterclouds.util.MathUtil;
import com.qendolin.betterclouds.util.RenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL46;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

//? if >=1.21 {
import net.minecraft.block.enums.CameraSubmersionType;
//?} else {
/*import net.minecraft.client.render.CameraSubmersionType;
*///?}

//? if >=1.21.5 {
/*import com.mojang.blaze3d.opengl.GlStateManager;
*///?} else {
import com.mojang.blaze3d.platform.GlStateManager;
//?}

import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static com.qendolin.betterclouds.compat.GLCompat.glCompat;
import static com.qendolin.betterclouds.compat.ProfilerWrapper.getProfiler;
import static org.lwjgl.opengl.GL32.*;

public class Renderer implements AutoCloseable {
    private final MinecraftClient client;
    private ClientWorld world = null;

    private float cloudsHeight;
    private final Matrix4f mvpMatrix = new Matrix4f();
    private final Matrix4f mvMatrix = new Matrix4f();
    private final Matrix4f pMatrix = new Matrix4f();
    private final Matrix4d pInverseMatrix = new Matrix4d();
    private final Matrix4f rotationProjectionMatrix = new Matrix4f();
    private final Matrix4f tempMatrix = new Matrix4f();
    private final Vector3f tempVector = new Vector3f();
    private final Frustum tempFrustum = new Frustum(new Matrix4f().identity(), new Matrix4f().identity());
    private final FrustumCuller frustumCuller = new FrustumCuller();
    private ShaderParameters shaderParameters = null;

    private ByteBuffer cloudBuffer;
    private int cloudBufferId;
    private int cloudBufferWriteOnceId = -1;
    private ChunkGenerator2 chunkGenerator2;
    private int heightTextureArray;
    private int regionOffsetsSsbo;

    private final Resources res = new Resources();

    public Renderer(MinecraftClient client) {
        this.client = client;
    }

    public void setWorld(ClientWorld world) {
        this.world = world;
    }

    public void reload(ResourceManager manager) {
//        float spacing = 8;
        float spacing = ConfigManager.instance().spacing;
//        float blockDistance = 256 * 16; // radius
        float blockDistance = ConfigManager.instance().renderDistance * 16; // radius
        int size = ChunkGenerator2.calculateSize(blockDistance, spacing);
//        int size = 1;
        int clouds = MathHelper.square(size * ChunkGenerator2.GEN_CHUNK_SIZE);
        int bufSize = clouds * 4 * ChunkGenerator2.BYTES_PER_COORD;
        int flags = GL_MAP_WRITE_BIT | glCompat.GL_MAP_PERSISTENT_BIT | glCompat.GL_MAP_COHERENT_BIT;
        cloudBufferId = glGenBuffers();
        glBindBuffer(GL43.GL_ARRAY_BUFFER, cloudBufferId);
        glCompat.bufferStorage(GL43.GL_ARRAY_BUFFER, bufSize, flags);
        ByteBuffer buffer = glMapBufferRange(GL43.GL_ARRAY_BUFFER, 0, bufSize, flags);
        if (buffer == null) throw new IllegalStateException("glMapBufferRange returned null");
        cloudBuffer = buffer; //.asFloatBuffer();
        glCompat.objectLabelDev(glCompat.GL_BUFFER, cloudBufferId, "cloud_buffer_2");

        chunkGenerator2 = new ChunkGenerator2(cloudBuffer, size, spacing);

        heightTextureArray = glGenTextures();
        glBindTexture(GL_TEXTURE_2D_ARRAY, heightTextureArray);
        GL43.glTexStorage3D(GL_TEXTURE_2D_ARRAY, 1, GL_R8, ChunkGenerator2.GEN_CHUNK_SIZE, ChunkGenerator2.GEN_CHUNK_SIZE, chunkGenerator2.index.length);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        regionOffsetsSsbo = glGenBuffers();
        glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, regionOffsetsSsbo);

        BetterCloudsStatic.getLogger().info("Reloading cloud renderer...");
        BetterCloudsStatic.getLogger().debug("[1/6] Reloading shaders");
        shaderParameters = createShaderParameters(ConfigManager.instance());
        res.reloadShaders(manager, shaderParameters);
        BetterCloudsStatic.getLogger().debug("[2/6] Reloading generator");
        res.reloadGenerator(useCubeClouds());
        BetterCloudsStatic.getLogger().debug("[3/6] Reloading textures");
        res.reloadTextures(client);
        BetterCloudsStatic.getLogger().debug("[4/6] Reloading primitive meshes");
        res.reloadMeshPrimitives();
        BetterCloudsStatic.getLogger().debug("[5/6] Reloading framebuffer");
        res.reloadFramebuffer(scaledFramebufferWidth(), scaledFramebufferHeight());
        BetterCloudsStatic.getLogger().debug("[6/6] Reloading timers");
        res.reloadTimer();
        BetterCloudsStatic.getLogger().info("Cloud renderer initialized");
    }

    public Resources resources() {
        return res;
    }

    // Used to be called isFancyMode
    private boolean useCubeClouds() {
        return ConfigManager.instance().sizeY > 0;
    }

    private int scaledFramebufferWidth() {
        return (int) (ConfigManager.instance().preset().upscaleResolutionFactor * client.getFramebuffer().textureWidth);
    }

    private int scaledFramebufferHeight() {
        return (int) (ConfigManager.instance().preset().upscaleResolutionFactor * client.getFramebuffer().textureHeight);
    }

    private ShaderParameters createShaderParameters(Config config) {
        return new ShaderParameters(
            client.options.getCloudRenderModeValue(),
            config.blockDistance(), config.sizeXZ, config.sizeY, config.celestialBodyHalo,
            glCompat.useDepthWriteFallback(), glCompat.useStencilTextureFallback(),
            DistantHorizonsCompat.instance().isReady() && DistantHorizonsCompat.instance().isEnabled(),
            config.preset().worldCurvatureSize,
            chunkGenerator2.size
        );
    }

    public PrepareResult prepare(Matrix4f viewMat, Matrix4f projMat, int ticks, float tickDelta, Vector3d cam) {
        assert RenderSystem.isOnRenderThread();
        getProfiler().swap("render_setup");
        Config config = ConfigManager.instance();

        if (res.failedToLoadCritical()) {
            if (RenderDoc.isFrameCapturing()) glCompat.debugMessage("prepare failed: critical resource not loaded");
            return PrepareResult.FALLBACK;
        }
        if (!config.irisSupport && IrisCompat.instance().isShadersEnabled()) {
            if (RenderDoc.isFrameCapturing()) glCompat.debugMessage("prepare failed: iris support disabled");
            return PrepareResult.FALLBACK;
        }

        // Rendering clouds when underwater was making them very visible in unloaded chunks
        if (client.gameRenderer.getCamera().getSubmersionType() != CameraSubmersionType.NONE) {
            return PrepareResult.NO_RENDER;
        }

        if (!Debug.generatorPause) {
            boolean updated = chunkGenerator2.update(cam.x, cam.z);
            if (updated) {
                glFlush();
                GL43.glMemoryBarrier(GL43.GL_BUFFER_UPDATE_BARRIER_BIT | GL46.GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT);
                glFlush();
                if (cloudBufferWriteOnceId != -1) {
                    glDeleteBuffers(cloudBufferWriteOnceId);
                }
                cloudBufferWriteOnceId = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, cloudBufferWriteOnceId);
                glCompat.objectLabelDev(glCompat.GL_BUFFER, cloudBufferWriteOnceId, "cloud_buffer_write_once");
                long size = (long) cloudBuffer.capacity() * ChunkGenerator2.BYTES_PER_COORD;
                glCompat.bufferStorage(GL43.GL_ARRAY_BUFFER, size, 0);

//                glBindBuffer(GL_COPY_READ_BUFFER, cloudBufferId);
                GL46.glCopyNamedBufferSubData(cloudBufferId, cloudBufferWriteOnceId, 0, 0, size);
//                glCopyBufferSubData(GL_COPY_READ_BUFFER, GL_ARRAY_BUFFER, 0, 0, size);

//                RenderDoc.triggerCapture();
                glBindTexture(GL_TEXTURE_2D_ARRAY, heightTextureArray);
                for (int region = 0; region < chunkGenerator2.index.length; region++) {
                    int length = ChunkGenerator2.GEN_CHUNK_SIZE_2 * ChunkGenerator2.BYTES_PER_COORD;
                    int offset = length * region;
                    var slice = chunkGenerator2.buffer.slice(offset, length);
                    GlStateManager._pixelStore(GL_UNPACK_ALIGNMENT, 4);
                    GlStateManager._pixelStore(GL_UNPACK_ROW_LENGTH, 0);
                    GlStateManager._pixelStore(GL_UNPACK_IMAGE_HEIGHT, 0);
                    GlStateManager._pixelStore(GL_UNPACK_SKIP_PIXELS, 0);
                    GlStateManager._pixelStore(GL_UNPACK_SKIP_ROWS, 0);
                    GlStateManager._pixelStore(GL_UNPACK_SKIP_IMAGES, 0);
                    glTexSubImage3D(
                        GL_TEXTURE_2D_ARRAY,
                        0,
                        0,
                        0,
                        region,
                        ChunkGenerator2.GEN_CHUNK_SIZE,
                        ChunkGenerator2.GEN_CHUNK_SIZE,
                        1,
                        GL_RED,
                        GL_UNSIGNED_BYTE,
                        slice);
                }

                glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, regionOffsetsSsbo);
                int[] data = ArrayUtils.addAll(chunkGenerator2.regionOffsets, chunkGenerator2.regionMap.values());
                glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, data, GL_STATIC_DRAW);
            }
        }


        DimensionEffects effects = world.getDimensionEffects();
        cloudsHeight = effects.getCloudsHeight();

        res.generator().bind();
        ShaderParameters currentShaderParameters = createShaderParameters(config);
        if (!Objects.equals(currentShaderParameters, shaderParameters)) {
            shaderParameters = currentShaderParameters;
            res.reloadShaders(client.getResourceManager(), shaderParameters);
        }
        res.generator().reallocateIfStale(config, useCubeClouds());

        float cloudiness = CloudinessProvider.getCloudiness(client.world, tickDelta);
        res.generator().update(cam, ticks, tickDelta, ConfigManager.instance(), cloudiness);
        if (res.generator().canGenerate() && !res.generator().generating() && !Debug.generatorPause) {
            getProfiler().swap("generate_clouds");
            res.generator().generate();
            getProfiler().swap("render_setup");
        }

//        if (res.generator().canSwap()) {
//            getProfiler().swap("swap");
//            res.generator().swap();
//            ByteBuffer cullingBuffer = res.generator().buffer().cullingBuffer;
//            cullingBuffer.clear();
//            for (ChunkedGenerator.ChunkIndex chunk : res.generator().chunks()) {
//                Box bounds = chunk.bounds(cloudsHeight, config.sizeXZ, config.sizeY);
//                cullingBuffer.putFloat((float) bounds.minX);
//                cullingBuffer.putFloat((float) bounds.minZ);
//                cullingBuffer.putFloat((float) bounds.maxX);
//                cullingBuffer.putFloat((float) bounds.maxZ);
//                cullingBuffer.putInt(chunk.start());
//                cullingBuffer.putInt(chunk.count());
//            }
//            getProfiler().swap("render_setup");
//        }

        float cloudsCullingHeightMin = cloudsHeight - config.sizeY;
        float cloudsCullingHeightMax = cloudsHeight + config.yRange + config.sizeY;
        float cloudsCullingHeight = MathHelper.clamp((float) cam.y, cloudsCullingHeightMin, cloudsCullingHeightMax);
        Vector3d frustumOrigin = new Vector3d(cam).sub(res.generator().originX(), 0, res.generator().originZ());
        frustumCuller.update(viewMat, frustumOrigin, projMat, cloudsCullingHeightMin, cloudsCullingHeightMax);


        // This is fixes issue #14, not entirely sure why, but it forces the matrix to be homogenous
        tempMatrix.set(viewMat);
        tempMatrix.m30(0);
        tempMatrix.m31(0);
        tempMatrix.m32(0);
        // If this isn't 0 then https://github.com/Qendolin/better-clouds/issues/165 occurs, but idk why.
        // If it is 0 then the matrix is not invertible
        tempMatrix.m33(0);
        tempMatrix.m23(0);
        tempMatrix.m13(0);
        tempMatrix.m03(0);

        rotationProjectionMatrix.set(projMat);
        rotationProjectionMatrix.mul(tempMatrix);

//        tempMatrix.translate((float) res.generator().renderOriginX(cam.x), (float) (cloudsHeight - cam.y), (float) res.generator().renderOriginZ(cam.z));
        tempMatrix.translate((float) -cam.x, (float) (cloudsHeight - cam.y), (float) -cam.z);
        tempMatrix.m33(1);

        pMatrix.set(projMat);
        pInverseMatrix.set(projMat);
        pInverseMatrix.invert();

        mvMatrix.set(tempMatrix);
        mvpMatrix.set(projMat);
        mvpMatrix.mul(mvMatrix);

        return PrepareResult.RENDER;
    }

    // Don't forget to push / pop matrix stack outside
    // Note: render must not return early, this will cause corruption because prepare binds stuff
    public void render(int ticks, float tickDelta, Vector3d cam, Vector3d frustumPos, Frustum frustum) {
        // In 1.21.3 render is called some time after prepare, so this may be false by now
        if (res.failedToLoadCritical()) return;

        getProfiler().swap("render_setup");
        if (Debug.isProfilingEnabled()) {
            if (res.timer() == null)
                res.reloadTimer();
            if (res.timer() != null)
                res.timer().start();
        }

        // Unbind vanilla shader, this is for compatability
        RenderHelper.unbindShader();

        Config config = ConfigManager.instance();
        if (isFramebufferStale()) {
            res.reloadFramebuffer(scaledFramebufferWidth(), scaledFramebufferHeight());
        }

        RenderHelper.Fog fog = FogProvider.getFog(client, config, tickDelta);

        // Render clouds to our framebuffer
        getProfiler().swap("draw_coverage");
        GlStateManager._viewport(0, 0, res.fboWidth(), res.fboHeight());
        GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, res.oitFbo());
        glClearColor(0, 0, 0, 0);
        glClearDepth(1);
        drawCoverage(ticks + tickDelta, cam, frustumPos, frustum, fog);

        // Draw to game framebuffer
        VanillaRenderTarget rt = new VanillaRenderTarget(client, config);
        rt.begin();

        // Render debug stuff
        getProfiler().swap("draw_debug");
        Debug.render(res, cam);
        frustumCuller.debugDraw();

        // Resolve and shade clouds
        getProfiler().swap("draw_shading");
        drawShading(tickDelta, fog);

        // Restore state
        getProfiler().swap("render_cleanup");
        rt.end();
        res.generator().unbind();
        GlStateManager._disableBlend();
        GlStateManager._enableDepthTest();
        GlStateManager._depthMask(true);
        GlStateManager._depthFunc(GL_LEQUAL);
        GlStateManager._activeTexture(GL_TEXTURE0);
        GlStateManager._colorMask(true, true, true, true);

        if (!glCompat.useStencilTextureFallback()) {
            glDisable(GL_STENCIL_TEST);
            glStencilFunc(GL_ALWAYS, 0x0, 0xff);
            glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
        }

        if (Debug.isProfilingEnabled() && res.timer() != null) {
            res.timer().stop();

            if (res.timer().frames() >= Debug.profileInterval) {
                PerfTimer.Stats gpu = PerfTimer.Stats.of(res.timer().gpu());
                PerfTimer.Stats cpu = PerfTimer.Stats.of(res.timer().cpu());
                BetterCloudsStatic.getLogger().info("GPU Times (msec):\n" + gpu);
                BetterCloudsStatic.getLogger().info("CPU Times (msec):\n" + cpu);
                ChatUtil.debugChatMessage("profiling.gpuTimes", gpu.formatted());
                ChatUtil.debugChatMessage("profiling.cpuTimes", cpu.formatted());
                res.timer().reset();
            }
        }
    }

    private boolean isFramebufferStale() {
        return res.fboWidth() != scaledFramebufferWidth() || res.fboHeight() != scaledFramebufferHeight();
    }

    private void drawCoverage(float ticks, Vector3d cam, Vector3d frustumPos, Frustum frustum, RenderHelper.Fog fog) {
        GlStateManager._enableDepthTest();
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._depthMask(true);
        glEnable(GL_DEPTH_CLAMP);

        if (glCompat.useStencilTextureFallback()) {
            GlStateManager._depthFunc(GL_ALWAYS);
            GlStateManager._enableBlend();
            glBlendEquation(GL_FUNC_ADD);
            // FIXME: buf0 needs depth sorting
            glCompat.blendFunci(0, GL_ONE, GL_ZERO);
            glCompat.blendFunci(1, GL_ONE, GL_ONE);
            glDisable(GL_STENCIL_TEST);
        } else {
            GlStateManager._depthFunc(GL_LEQUAL);
            GlStateManager._disableBlend();
            glEnable(GL_STENCIL_TEST);
            glStencilMask(0xff);
            glClearStencil(0);
            glStencilOp(GL_KEEP, GL_INCR, GL_INCR);
            glStencilFunc(GL_ALWAYS, 0xff, 0xff);
        }

        if (useCubeClouds()) GlStateManager._enableCull();
        else GlStateManager._disableCull();
//        GlStateManager._disableCull();
        glClear(GL_STENCIL_BUFFER_BIT | GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        Config generatorConfig = getGeneratorConfig();
        Config config = ConfigManager.instance();

        res.coverageShader().bind();
        res.coverageShader().uMVPMatrix.setMat4(mvpMatrix);
//        res.coverageShader().uOriginOffset.setVec3((float) -res.generator().renderOriginX(cam.x), (float) cam.y - cloudsHeight, (float) -res.generator().renderOriginZ(cam.z));
        res.coverageShader().uBoundingBox.setVec4((float) cam.x, (float) cam.z, generatorConfig.blockDistance() - generatorConfig.chunkSize / 2f, generatorConfig.yRange + config.sizeY);
        res.coverageShader().uTime.setFloat(ticks / 20);
        res.coverageShader().uMiscellaneous.setVec3(config.scaleFalloffMin, config.windEffectFactor, config.windSpeedFactor);
        if (fog == null) { // Fog off
            res.coverageShader().uFogRange.setVec2(config.blockDistance() - 8, config.blockDistance());
        } else {
            res.coverageShader().uFogRange.setVec2(fog.start(), fog.end());
        }

        GlStateManager._activeTexture(GL_TEXTURE0);
        RenderHelper.bindTexture(client.getFramebuffer().getDepthAttachment());

        // Distant Horizons compat
        if (DistantHorizonsCompat.instance().isReady() && DistantHorizonsCompat.instance().isEnabled()) {
            res.coverageShader().uMVMatrix.setMat4(mvMatrix);
            res.coverageShader().uMcPMatrix.setMat4(pMatrix);

            Optional<Integer> depthId = DistantHorizonsCompat.instance().getDepthTextureId();
            GlStateManager._activeTexture(GL_TEXTURE6);
            if (depthId.isPresent()) {
                Matrix4f dhProjectionMatrix = DistantHorizonsCompat.instance().getProjectionMatrix();
                RenderHelper.bindTexture(depthId.get());
                if (DistantHorizonsCompat.instance().isTextureCreateFlagSet()) {
                    // This fixes a bug in DH, see: https://discord.com/channels/881614130614767666/1211290858134052894
                    glBindTexture(GL_TEXTURE_2D, depthId.get());
                    DistantHorizonsCompat.instance().resetTextureCreateFlag();
                }
                res.coverageShader().uDhPMatrix.setMat4(dhProjectionMatrix);
            } else {
                RenderHelper.bindTexture(0);
                res.coverageShader().uDhPMatrix.setMat4(DistantHorizonsCompat.NOOP_MATRIX);
            }
        }

        GlStateManager._activeTexture(GL_TEXTURE5);
        RenderHelper.bindTexture(client.getTextureManager().getTexture(Resources.NOISE_TEXTURE));

        res.generator().bind();
        if (glCompat.useBaseInstanceFallback()) {
            res.generator().buffer().bindDrawBuffer();
        }

        setFrustumTo(tempFrustum, frustum);
        Frustum frustumAtOrigin = tempFrustum;
        frustumAtOrigin.setPosition(frustumPos.x - res.generator().originX(), frustumPos.y, frustumPos.z - res.generator().originZ());

        if (!res.generator().canRender()) {
            GlStateManager._enableCull();
            return;
        }


        boolean frustumCulling = config.useFrustumCulling;
        if (IrisCompat.instance().isFrustumCullingDisabled() || config.preset().worldCurvatureSize != 0) {
            frustumCulling = false;
        }

        if (frustumCulling) {
            drawCloudsWithFrustumCulling(frustumAtOrigin, config, cam);
        } else {
            drawCloudsWithoutFrustumCulling();
        }

        glDisable(GL_DEPTH_CLAMP);
        GlStateManager._enableCull();
    }

    static class StackEntry {
        int lvl;
        int x;
        int z;

        void set(int lvl, int x, int z) {
            this.lvl = lvl;
            this.x = x;
            this.z = z;
        }
    }

    StackEntry[] cullingStack = new StackEntry[ChunkGenerator2.MAX_SUB_LVL * 4];

    {
        for (int i = 0; i < cullingStack.length; i++) {
            cullingStack[i] = new StackEntry();
        }
    }

    IntBuffer mdiBuffer;
    int mdiBufferId;

    private static class DrawCommand {
        public int start, count;
        public final float[] bounds = new float[4];
    }

    private static class DrawCommands {
        private final DrawCommand[] commands;
        private int size = 0;

        public DrawCommands(int capacity) {
            commands = new DrawCommand[capacity];
            for (int i = 0; i < commands.length; i++) {
                commands[i] = new DrawCommand();
            }
        }

        public void reset(int size) {
            this.size = size;
        }

        public int size() {
            return size;
        }

        public void add(int start, int count, float[] bounds) {
            var cmd = commands[size++];
            cmd.start = start;
            cmd.count = count;
            cmd.bounds[0] = bounds[0];
            cmd.bounds[1] = bounds[1];
            cmd.bounds[2] = bounds[2];
            cmd.bounds[3] = bounds[3];
        }

        public DrawCommand get(int i) {
            return commands[i];
        }
    }

    DrawCommands commands;

    // FIXME: spyglass

    // TODO: culling boxes should be based on their world size, not some arbitrary, fixed number
    // For spacing 2 and 256 distance the biggest level is still too small
    private boolean generateCulledDrawCommands(DrawCommands draws, int region, int lvl, int x, int z) {
        final float[] bounds = new float[4];
        chunkGenerator2.bounds(region, lvl, x, z, bounds);
        float maxDist = ConfigManager.instance().blockDistance() / 2f; // half is far

        int visible = frustumCuller.test2(bounds[0], bounds[1], bounds[2], bounds[3]);
        int inRange = frustumCuller.testDist2(bounds[0], bounds[1], bounds[2], bounds[3], maxDist);

        if (visible == 0 || inRange == 0) {
            if(Debug.frustumCulling)
                Debug.addFrustumCulledBox(new Box(bounds[0], cloudsHeight, bounds[1], bounds[2], cloudsHeight + 64, bounds[3]), 0, 0, false);
            return false;
        } else if ((visible == 4 && inRange == 4) || lvl == ChunkGenerator2.MAX_SUB_LVL) {
            int start = chunkGenerator2.startOf(region, lvl, x, z);
            int count = chunkGenerator2.countOf(lvl);
            draws.add(start, count, bounds);
            return true;
        }

        int marker = draws.size();

        boolean sub11 = generateCulledDrawCommands(draws, region, lvl + 1, x * 2 + 1, z * 2 + 1);
        boolean sub01 = generateCulledDrawCommands(draws, region, lvl + 1, x * 2, z * 2 + 1);
        boolean sub10 = generateCulledDrawCommands(draws, region, lvl + 1, x * 2 + 1, z * 2);
        boolean sub00 = generateCulledDrawCommands(draws, region, lvl + 1, x * 2, z * 2);

        // if it turns out that all subsections were actually not culled
        if (sub11 && sub01 && sub10 && sub00) {
            draws.reset(marker);
            int start = chunkGenerator2.startOf(region, lvl, x, z);
            int count = chunkGenerator2.countOf(lvl);
            draws.add(start, count, bounds);
            return true;
        }

        if(Debug.frustumCulling)
            Debug.addFrustumCulledBox(new Box(bounds[0], cloudsHeight, bounds[1], bounds[2], cloudsHeight + 64, bounds[3]), 0, 0, false);
        return false;
    }

    private void drawFarClouds(Config config, Vector3d cam) {
        GlStateManager._disableCull();
        res.coverageFarShader().bind();
        res.coverageFarShader().uMVPMatrix.setMat4(mvpMatrix);
        res.coverageFarShader().uSpacing.setFloat(config.spacing);
        res.coverageFarShader().uCircle.setVec3((float) cam.x, (float) cam.z, config.blockDistance() * 0.5f);

        GlStateManager._depthMask(false);

        int coverage = MathHelper.ceil((config.sizeXZ*config.sizeXZ)/(config.spacing*config.spacing)) * 2;
        glStencilOp(GL_KEEP, GL_REPLACE, GL_REPLACE);
        glStencilFunc(GL_ALWAYS, Math.min(coverage, 64), 0xff);
//        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 18);

        glStencilOp(GL_KEEP, GL_INCR, GL_INCR);
        glStencilFunc(GL_ALWAYS, 0xff, 0xff);

        GlStateManager._depthMask(true);
    }

    private void drawCloudsWithFrustumCulling(Frustum frustumAtOrigin, Config config, Vector3d cam) {
//        glBindBuffer(GL_ARRAY_BUFFER, cloudBufferId);
//        glBindBuffer(GL_ARRAY_BUFFER, cloudBufferWriteOnceId);


        // bind to vao, usually happens elsewhere, but rn I need tis
//        glVertexAttribPointer(0, 4, GL_FLOAT, false, Float.BYTES * 4, 0);
//        glVertexAttribPointer(0, 1, GL_UNSIGNED_BYTE, true, 1, 0);

//        glEnableVertexAttribArray(0);
//        glEnableVertexAttribArray(1);
//        glEnableVertexAttribArray(2);
//        glEnableVertexAttribArray(3);
//        glVertexAttribPointer(0, 4, GL_FLOAT, false, 4 * Float.BYTES * 4, 0);
//        glVertexAttribPointer(1, 4, GL_FLOAT, false, 4 * Float.BYTES * 4, Float.BYTES*4);
//        glVertexAttribPointer(2, 4, GL_FLOAT, false, 4 * Float.BYTES * 4, 2*Float.BYTES*4);
//        glVertexAttribPointer(3, 4, GL_FLOAT, false, 4 * Float.BYTES * 4, 3*Float.BYTES*4);
//        glCompat.vertexAttribDivisor(0, 1);
//        glCompat.vertexAttribDivisor(1, 1);
//        glCompat.vertexAttribDivisor(2, 1);
//        glCompat.vertexAttribDivisor(3, 1);

//        glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, res.generator().instanceVertexCount(), chunkGenerator2.clouds());

        glActiveTexture(GL_TEXTURE6);
        glBindTexture(GL_TEXTURE_2D_ARRAY, heightTextureArray);

        glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, regionOffsetsSsbo);

        if (commands == null) {
            commands = new DrawCommands((int) Math.pow(4, ChunkGenerator2.MAX_SUB_LVL) * 2);
        }

        res.coverageShader().uCameraPos.setVec3((float) cam.x, (float) cam.y - cloudsHeight, (float) cam.z);
        res.coverageShader().uSpacing.setFloat(config.spacing);
        res.coverageShader().uCircle.setVec3((float) cam.x, (float) cam.z, config.blockDistance() * 0.5f);

        // Render flat first,
        // otherwise there are some issues with ordering
        drawFarClouds(config, cam);
        res.coverageShader().bind();

        // FIXME: some chunks are drawn at a higher sub level than needed
        // When the larger chunk is split, but all the children are drawn anyways, because they are at the lowest sub level
        // In that case the subdiv was correct, but should be joined again.

        // Possible optimization:
        // Culling this many faces is quite slow, so any regions that are not the center regions can
        // not draw faces that always point away

        for (int region = 0; region < chunkGenerator2.index.length; region++) {
            commands.reset(0);
            generateCulledDrawCommands(commands, region, 0, 0, 0);

            for (int i = 0; i < commands.size(); i++) {
                DrawCommand cmd = commands.get(i);
                if (Debug.frustumCulling)
                    Debug.addFrustumCulledBox(new Box(cmd.bounds[0], cloudsHeight, cmd.bounds[1], cmd.bounds[2], cloudsHeight + 64, cmd.bounds[3]), 0, 0, true);
//                GL43.glDrawArraysInstancedBaseInstance(GL_TRIANGLE_STRIP, 0, Mesh.FANCY_MESH_VERTEX_COUNT, cmd.count, cmd.start);
                GL43.glDrawArraysInstancedBaseInstance(GL_TRIANGLE_FAN, 0, 8, cmd.count, cmd.start);
            }
        }

        if (1 == 1) return;

        // Issue: Too many draw calls
        // Fixes:
        // - reduce max sub level
        // - use MDI (doesn't matter apparently)

        if (mdiBuffer == null) {
            mdiBufferId = glGenBuffers();
            glBindBuffer(GL43.GL_DRAW_INDIRECT_BUFFER, mdiBufferId);
            int size = chunkGenerator2.index.length * 8 * ChunkGenerator2.MAX_SUB_LVL * 4 * Integer.BYTES;
            glCompat.bufferStorage(GL43.GL_DRAW_INDIRECT_BUFFER, size, GL_MAP_WRITE_BIT | glCompat.GL_MAP_PERSISTENT_BIT | glCompat.GL_MAP_COHERENT_BIT);
            mdiBuffer = glMapBufferRange(GL43.GL_DRAW_INDIRECT_BUFFER, 0, size, GL_MAP_WRITE_BIT | glCompat.GL_MAP_PERSISTENT_BIT | glCompat.GL_MAP_COHERENT_BIT)
                .asIntBuffer();
        }
        mdiBuffer.clear();
        glBindBuffer(GL43.GL_DRAW_INDIRECT_BUFFER, mdiBufferId);

        final float[] bounds = new float[4];
        for (int i = 0; i < chunkGenerator2.index.length; i++) {
            int stackTop = 0;
            cullingStack[stackTop++].set(0, 0, 0);

            //*
            while (stackTop > 0) {
                StackEntry entry = cullingStack[--stackTop];
                int lvl = entry.lvl;
                int x = entry.x;
                int z = entry.z;
                int submax = 1 << lvl;
//                int sub = entry.x + submax * entry.z;
//                float[] bounds = chunkGenerator2.bounds(i, entry.lvl, sub);
                chunkGenerator2.bounds(i, lvl, x, z, bounds);
                int visible = frustumCuller.test2(bounds[0], bounds[1], bounds[2], bounds[3]);
//                if(visible > 0) visible = 4;
                if (visible == 0) {
                    if (Debug.frustumCulling)
                        Debug.addFrustumCulledBox(new Box(bounds[0], cloudsHeight, bounds[1], bounds[2], cloudsHeight + 64, bounds[3]), 0, 0, false);
                } else if (visible == 4 || lvl == ChunkGenerator2.MAX_SUB_LVL) {
//                    int start = chunkGenerator2.startOf(i, entry.lvl, sub);
                    int start = chunkGenerator2.startOf(i, lvl, x, z);
//                    GL43.glDrawArraysInstancedBaseInstance(GL_TRIANGLE_STRIP, 0, res.generator().instanceVertexCount(), chunkGenerator2.countOf(entry.lvl), start);

                    mdiBuffer.put(Mesh.FANCY_MESH_VERTEX_COUNT);
//                    mdiBuffer.put(15 * 4 - 1);
                    mdiBuffer.put(chunkGenerator2.countOf(lvl));
//                    mdiBuffer.put(chunkGenerator2.countOf(lvl)/4);
                    mdiBuffer.put(0);
                    mdiBuffer.put(start);
//                    mdiBuffer.put(start/4);
                    if (Debug.frustumCulling)
                        Debug.addFrustumCulledBox(new Box(bounds[0], cloudsHeight, bounds[1], bounds[2], cloudsHeight + 64, bounds[3]), 0, 0, true);
                } else {
                    cullingStack[stackTop++].set(lvl + 1, x * 2 + 1, z * 2 + 1);
                    cullingStack[stackTop++].set(lvl + 1, x * 2, z * 2 + 1);
                    cullingStack[stackTop++].set(lvl + 1, x * 2 + 1, z * 2);
                    cullingStack[stackTop++].set(lvl + 1, x * 2, z * 2);
                }
            }
            /*/
            mdiBuffer.put(Mesh.FANCY_MESH_VERTEX_COUNT);
            mdiBuffer.put(ChunkGenerator2.GEN_CHUNK_SIZE_2);
            mdiBuffer.put(0);
            mdiBuffer.put(i * ChunkGenerator2.GEN_CHUNK_SIZE_2);
            //*/

//            chunkGenerator2.bounds(i, bounds);
//            res.coverageShader().uMiscellaneous.setVec3(bounds[0], 0, bounds[1]);
//            res.coverageShader().uDeleteMe.setInt(i * ChunkGenerator2.GEN_CHUNK_SIZE_2);
//            mdiBuffer.flip();
//            GL43.glMultiDrawArraysIndirect(GL_TRIANGLE_STRIP, 0, mdiBuffer.limit() / 4, 0);
//            mdiBuffer.clear();

        }

        mdiBuffer.flip();
        GL43.glMultiDrawArraysIndirect(GL_TRIANGLE_STRIP, 0, mdiBuffer.limit() / 4, 0);
//        GL43.glMultiDrawArraysIndirect(GL_TRIANGLES, 0, mdiBuffer.limit() / 4, 0);

        if (1 == 1) return;


////        int groups = MathHelper.ceilDiv(res.generator().cloudCount(), 64);
//        int groups = MathHelper.ceilDiv(res.generator().buffer().tmp_drawCommandCount, 64);
//        res.cullingShader().bind();
//        res.cullingShader().uFrustumPlaneTop.setVec3((float) frustumCuller.top().x, (float) frustumCuller.top().y, (float) frustumCuller.top().z);
//        res.cullingShader().uFrustumPlaneRight.setVec3((float) frustumCuller.right().x, (float) frustumCuller.right().y, (float) frustumCuller.right().z);
//        res.cullingShader().uFrustumPlaneBottom.setVec3((float) frustumCuller.bottom().x, (float) frustumCuller.bottom().y, (float) frustumCuller.bottom().z);
//        res.cullingShader().uFrustumPlaneLeft.setVec3((float) frustumCuller.left().x, (float) frustumCuller.left().y, (float) frustumCuller.left().z);
//        res.cullingShader().uOrigin.setVec3((float) frustumCuller.origin().x, (float) frustumCuller.origin().y, (float) frustumCuller.origin().z);
//        res.cullingShader().uCloudCount.setInt(res.generator().cloudCount());
//        GL43.glBindBufferBase(GL43.GL_ATOMIC_COUNTER_BUFFER, 0, res.generator().buffer().atomicCounterId);
//        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 1, res.generator().buffer().drawBufferId());
//        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 2, res.generator().buffer().compactBufferId);
//        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 3, res.generator().buffer().drawIndirectBufferId);
//        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 4, res.generator().buffer().cullingBufferId);
//
//        GL43.glDispatchCompute(groups, 1, 1);
//
//        GL43.glMemoryBarrier(GL43.GL_COMMAND_BARRIER_BIT | GL43.GL_SHADER_STORAGE_BARRIER_BIT);
//        glBindBuffer(GL43.GL_DRAW_INDIRECT_BUFFER, res.generator().buffer().drawIndirectBufferId);
//        res.coverageShader().bind();
////        GL43.glDrawArraysIndirect(GL_TRIANGLE_STRIP, 0);
////        GL43.glMultiDrawArraysIndirect(GL_TRIANGLE_STRIP, 0, groups, 0);
//        GL43.glMultiDrawArraysIndirect(GL_TRIANGLE_STRIP, 0, res.generator().buffer().tmp_drawCommandCount, 0);
//        glBindBuffer(GL43.GL_ATOMIC_COUNTER_BUFFER, res.generator().buffer().atomicCounterId);
//        GL43.glBufferSubData(GL43.GL_ATOMIC_COUNTER_BUFFER, 0, new int[] {0}); // reset counter
//        GL43.glBufferSubData(GL43.GL_DRAW_INDIRECT_BUFFER, Integer.BYTES, new int[] {0}); // reset instance count
//        if(1==1) return;

        // This algorithm loops over chunks, which are in a line-by-line order.a
        // When a visible chunk is found it's marked as a run start. The run continues until
        // the next non-visible chunk is found. At the end of a run the entire run is rendered as once.
        // This is possible due to the memory layout of the instance buffers.
//        int runStart = -1;
//        int runCount = 0;
//        for (ChunkedGenerator.ChunkIndex chunk : res.generator().chunks()) {
//            Box bounds = chunk.bounds(cloudsHeight, config.sizeXZ, config.sizeY);
////            if (!frustumAtOrigin.isVisible(bounds)) {
//            if (!frustumCuller.test(bounds)) {
//                Debug.addFrustumCulledBox(bounds, res.generator().originX(), res.generator().originZ(), false);
//                if (runCount != 0) {
//                    if (glCompat.useBaseInstanceFallback()) {
//                        res.generator().buffer().setVAPointerToInstance(runStart);
//                    }
//                    glCompat.drawArraysInstancedBaseInstanceFallback(GL_TRIANGLE_STRIP, 0, res.generator().instanceVertexCount(), runCount, runStart);
//                }
//                runStart = -1;
//                runCount = 0;
//            } else {
//                Debug.addFrustumCulledBox(bounds, res.generator().originX(), res.generator().originZ(), true);
//                if (runStart == -1) runStart = chunk.start();
//                runCount += chunk.count();
//            }
//        }
//        if (runCount != 0) {
//            if (glCompat.useBaseInstanceFallback()) {
//                res.generator().buffer().setVAPointerToInstance(runStart);
//            }
//            glCompat.drawArraysInstancedBaseInstanceFallback(GL_TRIANGLE_STRIP, 0, res.generator().instanceVertexCount(), runCount, runStart);
//        }
    }

    private void drawCloudsWithoutFrustumCulling() {
        List<ChunkedGenerator.ChunkIndex> chunks = res.generator().chunks();
        if (chunks.isEmpty()) return;
        ChunkedGenerator.ChunkIndex first = chunks.get(0);
        ChunkedGenerator.ChunkIndex last = chunks.get(chunks.size() - 1);
        int start = first.start();
        int count = last.start() + last.count();
        if (glCompat.useBaseInstanceFallback()) {
            res.generator().buffer().setVAPointerToInstance(start);
        }
        glCompat.drawArraysInstancedBaseInstanceFallback(GL_TRIANGLE_STRIP, 0, res.generator().instanceVertexCount(), count, start);
    }

    private void drawShading(float tickDelta, RenderHelper.Fog fog) {
        Config config = ConfigManager.instance();
        GlStateManager._depthFunc(GL_LEQUAL);

        if (!glCompat.useDepthWriteFallback()) {
            GlStateManager._depthMask(true);
            GlStateManager._enableDepthTest();
        } else {
            GlStateManager._disableDepthTest();
        }

        GlStateManager._enableBlend();
        glBlendEquation(GL_FUNC_ADD);
        // sync up state manager state, can be desynced by use of blendFunci
        GlStateManager._blendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager._colorMask(false, false, false, false);
        glColorMaski(0, true, true, true, true);
        if (!glCompat.useStencilTextureFallback()) {
            glDisable(GL_STENCIL_TEST);
        }

        GlStateManager._activeTexture(GL_TEXTURE1);
        if (glCompat.useDepthWriteFallback()) {
            RenderHelper.bindTexture(0);
        } else {
            RenderHelper.bindTexture(res.oitCoverageDepthTexture());
        }
        GlStateManager._activeTexture(GL_TEXTURE2);
        RenderHelper.bindTexture(res.oitDataTexture());
        GlStateManager._activeTexture(GL_TEXTURE3);
        RenderHelper.bindTexture(res.oitCoverageTexture());
        GlStateManager._activeTexture(GL_TEXTURE4);
        RenderHelper.bindTexture(client.getTextureManager().getTexture(Resources.LIGHTING_TEXTURE));

        Vector3f effectTint = EffectTintProvider.getEffectTint(client, fog, tickDelta);
        long skyTime = world.getLunarTime() % 24000;
        float skyAngleRad = world.getSkyAngleRadians(tickDelta);
        float sunPathAngleRad = (float) Math.toRadians(config.preset().sunPathAngle);
        float dayNightFactor = MathUtil.interpolateDayNightFactor(skyTime, config.preset().sunriseStartTime, config.preset().sunriseEndTime, config.preset().sunsetStartTime, config.preset().sunsetEndTime);
        float brightness = (1 - dayNightFactor) * config.preset().nightBrightness + dayNightFactor * config.preset().dayBrightness;
        float sunAxisY = MathHelper.sin(sunPathAngleRad);
        float sunAxisZ = MathHelper.cos(sunPathAngleRad);
        Vector3f sunDir = tempVector.set(1, 0, 0).rotateAxis(skyAngleRad + MathHelper.HALF_PI, 0, sunAxisY, sunAxisZ);
        float dayTime = world.getTimeOfDay() % 24000;
        float mappedTime = MathUtil.mapTimeOfDay(dayTime, config.preset().sunriseStartTime, config.preset().sunriseEndTime, config.preset().sunsetStartTime, config.preset().sunsetEndTime);

        res.shadingShader().bind();
        res.shadingShader().uVPMatrix.setMat4(rotationProjectionMatrix);
        res.shadingShader().uSunDirection.setVec4(sunDir.x, sunDir.y, sunDir.z, mappedTime / 24000f);
        res.shadingShader().uSunAxis.setVec3(0, sunAxisY, sunAxisZ);
        res.shadingShader().uOpacity.setVec3(config.preset().opacity, config.preset().opacityFactor, config.preset().opacityExponent);
        res.shadingShader().uColorGrading.setVec4(brightness, 1f / config.preset().gamma(), 0.0f, config.preset().saturation);
        res.shadingShader().uTint.setVec3(config.preset().tintRed * effectTint.x, config.preset().tintGreen * effectTint.y, config.preset().tintBlue * effectTint.z);
        res.shadingShader().uNoiseFactor.setFloat(config.colorVariationFactor);

        glBindVertexArray(res.cubeVao());
        glDrawArrays(GL_TRIANGLES, 0, Mesh.CUBE_MESH_VERTEX_COUNT);

        if (glCompat.useDepthWriteFallback()) {
            GlStateManager._colorMask(false, false, false, false);
            GlStateManager._activeTexture(GL_TEXTURE6);
            RenderHelper.bindTexture(res.oitCoverageDepthTexture());
            glTexParameteri(GL_TEXTURE_2D, glCompat.GL_DEPTH_STENCIL_TEXTURE_MODE, GL_DEPTH_COMPONENT);
            res.depthShader().bind();
            glDrawArrays(GL_TRIANGLES, 0, Mesh.QUAD_MESH_VERTEX_COUNT);
            glTexParameteri(GL_TEXTURE_2D, glCompat.GL_DEPTH_STENCIL_TEXTURE_MODE, GL_STENCIL_INDEX);
        }
    }

    private Config getGeneratorConfig() {
        Config config = res.generator().config();
        if (config != null) return config;
        return ConfigManager.instance();
    }

    private static void setFrustumTo(Frustum dst, Frustum src) {
        dst.frustumIntersection = src.frustumIntersection;
        dst.positionProjectionMatrix.set(src.positionProjectionMatrix);
        dst.x = src.x;
        dst.y = src.y;
        dst.z = src.z;
        dst.recession = src.recession;
    }

    public void close() {
        res.close();
    }

    public enum PrepareResult {
        RENDER, NO_RENDER, FALLBACK
    }
}
