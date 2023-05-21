package com.qendolin.betterclouds.clouds;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.qendolin.betterclouds.Config;
import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.compat.IrisCompat;
import com.qendolin.betterclouds.compat.SodiumExtraCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.*;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL44;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.qendolin.betterclouds.Main.bcObjectLabel;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer4 implements AutoCloseable {

    private static final Identifier NOISE_TEXTURE = new Identifier(Main.MODID, "textures/environment/cloud_noise.png");
    private static final Identifier NOISE2_TEXTURE = new Identifier(Main.MODID, "textures/environment/cloud_noise_3.png");
    private static final Identifier LIGHTING_TEXTURE = new Identifier(Main.MODID, "textures/environment/cloud_gradient.png");

    private final MinecraftClient client;
    private ClientWorld world = null;

    private CloudRenderMode lastCloudRenderMode = null;
    private TestShader0 depthShader = null;
    private TestShader1 coverageShader = null;
    private TestShader2 blitShader = null;
    private ChunkedGenerator cloudGenerator = null;
    private final ViewboxTransform viewboxTransform = new ViewboxTransform();
    private final long startTime = Util.getEpochTimeMs();
    private float lastDistance = -1;
    private int quadVbo;
    private int quadVao;
    private int oitFbo;
    private int oitDataTexture;
    private int oitCoverageTexture;
    private int oitCoverageDepthView;
    private int fboWidth;
    private int fboHeight;
    private float cloudsHeight;
    private int defaultFbo;
    private float raininess;
    private final Matrix4f mvpMatrix = new Matrix4f();
    private final Matrix4f inverseMatrix = new Matrix4f();
    private final Matrix4f tempMatrix = new Matrix4f();

    private GlTimer timer;

    public Renderer4(MinecraftClient client) {
        this.client = client;
    }

    public void setWorld(ClientWorld world) {
        this.world = world;
    }

    public void reload(ResourceManager manager) {
        // TODO: Move this somewhere else
        if(Main.IS_DEV) GL44.glEnable(GL44.GL_DEBUG_OUTPUT_SYNCHRONOUS);

        Main.LOGGER.info("Reloading cloud renderer...");

        Main.LOGGER.info("1) Reloading shaders");
        reloadShaders(manager);
        Main.LOGGER.info("2) Reloading generator");
        reloadGenerator();
        Main.LOGGER.info("3) Reloading textures");
        reloadTextures();
        Main.LOGGER.info("4) Reloading quad mesh");
        reloadQuad();
        Main.LOGGER.info("5) Reloading framebuffer");
        reloadFramebuffer();
        Main.LOGGER.info("6) Reloading timers");
        reloadTimer();

        Main.LOGGER.info("Cloud renderer initialized");
    }

    private void deleteTimer() {
        if(timer != null) timer.close();
    }

    private void reloadTimer() {
        if(!Main.IS_DEV) return;
        deleteTimer();

        timer = new GlTimer();
    }

    private void deleteQuad() {
        if(quadVbo != 0) glDeleteBuffers(quadVbo);
        if(quadVao != 0) glDeleteVertexArrays(quadVao);
    }

    private void reloadQuad() {
        deleteQuad();

        quadVao = glGenVertexArrays();
        glBindVertexArray(quadVao);
        bcObjectLabel(GL44.GL_VERTEX_ARRAY, quadVao, "quad");

        quadVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, quadVbo);
        bcObjectLabel(GL44.GL_BUFFER, quadVbo, "quad");

        glBufferData(GL_ARRAY_BUFFER, new float[]{1,-1, 1,1, -1,-1, -1,1}, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
    }

    private void reloadTextures() {
        // FIXME: The texture doesn't load sometimes
        int noiseTexture = client.getTextureManager().getTexture(NOISE_TEXTURE).getGlId();
        RenderSystem.bindTexture(noiseTexture);
        bcObjectLabel(GL_TEXTURE, noiseTexture, "noise");
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GL_REPEAT);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GL_REPEAT);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_LINEAR);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_LINEAR);
        noiseTexture = client.getTextureManager().getTexture(NOISE2_TEXTURE).getGlId();
        RenderSystem.bindTexture(noiseTexture);
        bcObjectLabel(GL_TEXTURE, noiseTexture, "noise2");
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GL_REPEAT);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GL_REPEAT);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_LINEAR);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_LINEAR);
        int lightingTexture = client.getTextureManager().getTexture(LIGHTING_TEXTURE).getGlId();
        RenderSystem.bindTexture(lightingTexture);
        bcObjectLabel(GL_TEXTURE, lightingTexture, "lighting");
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GL_REPEAT);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_LINEAR);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_LINEAR);
    }

    private void deleteGenerator() {
        if(cloudGenerator != null) cloudGenerator.close();
    }

    private void reloadGenerator() {
        deleteGenerator();
        cloudGenerator = new ChunkedGenerator();
        cloudGenerator.allocate(Main.CONFIG, isFancyMode());
        cloudGenerator.clear();
    }

    private void deleteFramebuffer() {
        if(oitFbo != 0) glDeleteFramebuffers(oitFbo);
        if(oitDataTexture != 0) glDeleteTextures(oitDataTexture);
        if(oitCoverageTexture != 0) glDeleteTextures(oitCoverageTexture);
    }

    private void reloadFramebuffer() {
        deleteFramebuffer();

        oitFbo = glGenFramebuffers();
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, oitFbo);
        bcObjectLabel(GL_FRAMEBUFFER, oitFbo, "coverage");

        fboWidth = client.getFramebuffer().textureWidth;
        fboHeight = client.getFramebuffer().textureHeight;

        oitDataTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, oitDataTexture);
        if(Main.IS_DEV) GL44.glObjectLabel(GL_TEXTURE, oitDataTexture, "coverage_color");
        GL44.glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGB8, fboWidth, fboHeight);
//        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, fboWidth, fboHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, oitDataTexture, 0);

        glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0});

        oitCoverageTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, oitCoverageTexture);
        bcObjectLabel(GL_TEXTURE, oitCoverageTexture, "coverage_stencil");
//        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH24_STENCIL8, fboWidth, fboHeight, 0, GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, 0);
        GL44.glTexStorage2D(GL_TEXTURE_2D, 1, GL_DEPTH24_STENCIL8, fboWidth, fboHeight);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL44.GL_DEPTH_STENCIL_TEXTURE_MODE, GL_STENCIL_INDEX);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_TEXTURE_2D, oitCoverageTexture, 0);

        oitCoverageDepthView = glGenTextures();
        GL44.glTextureView(oitCoverageDepthView, GL_TEXTURE_2D, oitCoverageTexture, GL_DEPTH24_STENCIL8, 0, 1, 0, 1);
        glBindTexture(GL_TEXTURE_2D, oitCoverageDepthView);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL44.GL_DEPTH_STENCIL_TEXTURE_MODE, GL_DEPTH_COMPONENT);

        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Clouds framebuffer incomplete, status: " + status);
        }
    }

    private void deleteShaders() {
        if(depthShader != null) depthShader.close();
        if(coverageShader != null) coverageShader.close();
        if(blitShader != null) blitShader.close();
    }

    private void reloadShaders(ResourceManager manager) {
        deleteShaders();

        Config options = Main.CONFIG;
        Map<String, String> defs = ImmutableMap.ofEntries(
            Map.entry(MainShader.DEF_SIZE_X_KEY, Float.toString(options.sizeXZ)),
            Map.entry(MainShader.DEF_SIZE_Y_KEY, Float.toString(options.sizeY)),
            Map.entry(MainShader.DEF_FADE_EDGE_KEY, Integer.toString((int) (options.fadeEdge * options.blockDistance())))
        );

        try {
            depthShader = new TestShader0(manager, Map.of());
            depthShader.bind();
            depthShader.uDepth.setInt(0);
            bcObjectLabel(GL44.GL_PROGRAM, depthShader.glId(), "depth");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            coverageShader = new TestShader1(manager, defs);
            coverageShader.bind();
            coverageShader.uLightTexture.setInt(2);
            coverageShader.uNoise2Texture.setInt(3);
            coverageShader.uNoise1Texture.setInt(5);
            bcObjectLabel(GL44.GL_PROGRAM, coverageShader.glId(), "coverage");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            blitShader = new TestShader2(manager, defs);
            blitShader.bind();
            blitShader.uColor.setInt(0);
            blitShader.uAccum.setInt(1);
            blitShader.uDepth.setInt(4);
            blitShader.uLightTexture.setInt(2);
            blitShader.uNoise2Texture.setInt(3);
            bcObjectLabel(GL44.GL_PROGRAM, blitShader.glId(), "blit");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean setup(MatrixStack matrices, Matrix4f projMat, float tickDelta, int ticks, Vec3d cam, Frustum frustum) {
        assert RenderSystem.isOnRenderThread();
        client.getProfiler().swap("render_setup");

        if(depthShader == null || coverageShader == null || blitShader == null) return false;
        if(!depthShader.isComplete() || !coverageShader.isComplete() || !blitShader.isComplete()) return false;
        if(!Main.CONFIG.enableExperimentalIrisSupport && IrisCompat.IS_LOADED && IrisCompat.isShadersEnabled()) return false;
        if(Main.CONFIG.useVanillaClouds) return false;
        // Rendering clouds when underwater was making them very visible in unloaded chunks
        if(client.gameRenderer.getCamera().getSubmersionType() != CameraSubmersionType.NONE) return false;

        DimensionEffects effects = world.getDimensionEffects();

        if(SodiumExtraCompat.IS_LOADED && effects.getSkyType() == DimensionEffects.SkyType.NORMAL) {
            cloudsHeight = SodiumExtraCompat.getCloudsHeight();
        } else {
            cloudsHeight = effects.getCloudsHeight();
        }

        cloudGenerator.bind();
        if(Main.CONFIG.hasChanged || lastCloudRenderMode != client.options.getCloudRenderModeValue() || lastDistance != Main.CONFIG.blockDistance()) {
            lastDistance = Main.CONFIG.blockDistance();
            lastCloudRenderMode = client.options.getCloudRenderModeValue();

            reloadShaders(client.getResourceManager());
            cloudGenerator.reallocate(Main.CONFIG, isFancyMode());

            Main.CONFIG.hasChanged = false;
        }

        raininess = Math.max(0.6f*world.getRainGradient(tickDelta), world.getThunderGradient(tickDelta));
        float cloudiness = raininess * 0.3f + 0.5f;

        cloudGenerator.update(cam, tickDelta, Main.CONFIG, cloudiness);
        if(cloudGenerator.canGenerate() && !cloudGenerator.generating()) {
            client.getProfiler().swap("generate_clouds");
            cloudGenerator.generate();
            client.getProfiler().swap("render_setup");
        }

        if(cloudGenerator.canSwap()) {
            client.getProfiler().swap("swap");
            cloudGenerator.swap();
            client.getProfiler().swap("render_setup");
        }

        matrices.translate(cloudGenerator.renderOriginX(cam.x), cloudsHeight-cam.y, cloudGenerator.renderOriginZ(cam.z));

        float pitch = client.cameraEntity == null ? 0 : client.cameraEntity.getPitch();
        float yaw = client.cameraEntity == null ? 0 : client.cameraEntity.getYaw();
        viewboxTransform.update(projMat, (float) cam.y, cloudsHeight, pitch);

        if(viewboxTransform.isInvalid()) {
            return false;
        }

        inverseMatrix.identity();
        Matrix4f viewRotationMatrix = tempMatrix;
        viewRotationMatrix.identity();
//        viewRotationMatrix.mul(Vec3f.POSITIVE_X.getDegreesQuaternion(pitch));
        viewRotationMatrix.rotate(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
//        viewRotationMatrix.mul(Vec3f.POSITIVE_Y.getDegreesQuaternion(yaw + 180.0f));
        viewRotationMatrix.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(yaw + 180.0f));
        inverseMatrix.set(viewboxTransform.getProjection());
        inverseMatrix.mul(viewRotationMatrix);
        inverseMatrix.invert();

        mvpMatrix.set(viewboxTransform.getProjection());
        mvpMatrix.mul(matrices.peek().getPositionMatrix());

        // TODO: don't do this dynamicaly
        defaultFbo = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING);

        return true;
    }

    // Push / Pop matrix stack outside
    public void render(MatrixStack matrices, float tickDelta, int ticks, Vec3d cam, Frustum frustum) {
        // FIXME: Back to Game button missing background texture
        // FIXME: Uniforms like spreadY are not 'in sync' with the vbo which causes graphic bugs when changing the options
        client.getProfiler().swap("render_setup");
        if(Main.DO_PROFILE) {
            timer.start();
        }

        if(framebufferStale()) {
            reloadFramebuffer();
        }

        RenderSystem.viewport(0, 0, fboWidth, fboHeight);
        GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, oitFbo);
        RenderSystem.clearDepth(1);
        RenderSystem.clearColor(0, 0, 0, 0);
//        RenderSystem.enableTexture();

        client.getProfiler().swap("draw_depth");
        drawDepth();

        client.getProfiler().swap("draw_coverage");
        drawCoverage(tickDelta, cam, frustum);

        client.getProfiler().swap("draw_shading");
        if(IrisCompat.IS_LOADED && IrisCompat.isShadersEnabled() && Main.CONFIG.useIrisFBO) {
            IrisCompat.bindFramebuffer();
        } else {
            GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, defaultFbo);
        }
        // TODO: viewport might not be correct always
        RenderSystem.viewport(0, 0, client.getFramebuffer().textureWidth, client.getFramebuffer().textureHeight);

        drawShading(tickDelta);

        client.getProfiler().swap("render_cleanup");

        cloudGenerator.unbind();
        Shader.unbind();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(GL_LESS);
        RenderSystem.activeTexture(GL_TEXTURE0);
        glDisable(GL_STENCIL_TEST);
        glStencilFunc(GL_ALWAYS, 0x0, 0xff);

        if(Main.DO_PROFILE) {
            timer.stop();

            if(timer.frames() >= 10000) {
                List<Double> times = timer.get();
                times.sort(Double::compare);
                double median = times.get(times.size()/2);
                double p25 = times.get((int) Math.ceil(times.size()*0.25));
                double p75 = times.get((int) Math.ceil(times.size()*0.75));
                double min = times.get(0);
                double max = times.get(times.size()-1);
                double average = times.stream().mapToDouble(d -> d).average().orElse(0);
                client.inGameHud.getChatHud().addMessage(Text.literal(String.format("Stats over %d frames: %.3f / %.3f / %.3f min/avg/max %.3f / %.3f / %.3f p25/med/p75", timer.frames(), min, average, max, p25, median, p75)));
                timer.reset();
            }
        }
    }

    private boolean framebufferStale() {
        return fboWidth != client.getFramebuffer().textureWidth || fboHeight != client.getFramebuffer().textureHeight;
    }

    private void drawShading(float tickDelta) {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendEquation(GL_FUNC_ADD);
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
        RenderSystem.colorMask(true, true, true, true);
        glStencilFunc(GL_GREATER, 0x0, 0xff);
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);

        RenderSystem.activeTexture(GL_TEXTURE0);
        RenderSystem.bindTexture(oitDataTexture);
        RenderSystem.activeTexture(GL_TEXTURE1);
        RenderSystem.bindTexture(oitCoverageTexture);
        RenderSystem.activeTexture(GL_TEXTURE2);
        client.getTextureManager().getTexture(LIGHTING_TEXTURE).bindTexture();
        RenderSystem.activeTexture(GL_TEXTURE3);
        client.getTextureManager().getTexture(NOISE2_TEXTURE).bindTexture();
        RenderSystem.activeTexture(GL_TEXTURE4);
        RenderSystem.bindTexture(oitCoverageDepthView);

        float effectLuma = getEffectLuminance(tickDelta);
        float skyAngle = world.getSkyAngleRadians(tickDelta);

        blitShader.bind();
        blitShader.uEffectColor.setVec4((float) Math.pow(effectLuma, Main.CONFIG.gamma), 0.0f, 0.0f, Main.CONFIG.opacity);
        blitShader.uSkyData.setVec4((float) -Math.sin(skyAngle), (float) Math.cos(skyAngle), world.getTimeOfDay()/24000f, 1-0.75f*raininess);
        blitShader.uGamma.setVec3(Main.CONFIG.brightness, Main.CONFIG.gamma, Main.CONFIG.alphaFactor);
        blitShader.uDepthRange.setVec2((float) viewboxTransform.maxNearPlane(), (float) viewboxTransform.minFarPlane());
        blitShader.uInverseMat.setMat4(inverseMatrix);

        glBindVertexArray(quadVao);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    }

    public float getEffectLuminance(float tickDelta) {
        float luma = 1.0f;
        float rain = world.getRainGradient(tickDelta);
        if (rain > 0.0f) {
            float f = rain * 0.95f;
            luma *= (1.0 - f) + f * 0.6f;
        }
        float thunder = world.getThunderGradient(tickDelta);
        if (thunder > 0.0f) {
            float f = thunder * 0.95f;
            luma *= (1.0 - f) + f * 0.2f;
        }
        return luma;
    }

    private void drawCoverage(float tickDelta, Vec3d cam, Frustum frustum) {
        glEnable(GL_STENCIL_TEST);
        glStencilMask(0xff);
        glStencilOp(GL_KEEP, GL_INCR, GL_INCR);
        glStencilFunc(GL_ALWAYS, 0xff, 0xff);
        RenderSystem.depthFunc(GL_LESS);
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);
        glClear(GL_STENCIL_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

        Vec3d skyColor = world.getCloudsColor(tickDelta);
        float skyAngle = world.getSkyAngleRadians(tickDelta);

        coverageShader.bind();
        coverageShader.uModelViewProjMat.setMat4(mvpMatrix);
        coverageShader.uCloudsOrigin.setVec3((float) -cloudGenerator.renderOriginX(cam.x), (float) cam.y-cloudsHeight, (float) -cloudGenerator.renderOriginZ(cam.z));
        coverageShader.uCloudsDistance.setFloat(Main.CONFIG.blockDistance() - Main.CONFIG.chunkSize/2f);
        coverageShader.uSkyData.setVec4((float) -Math.sin(skyAngle), (float) Math.cos(skyAngle), world.getTimeOfDay()/24000f, 1-0.75f*raininess);
        coverageShader.uCloudsBox.setVec4((float) cam.x, (float) cam.z, (float) cam.y-cloudsHeight, Main.CONFIG.spreadY);
        coverageShader.uTime.setFloat((Util.getEpochTimeMs() - startTime)/1000f);
        coverageShader.uMiscOptions.setVec4(Main.CONFIG.fakeScaleFalloffMin, Main.CONFIG.windFactor, 0.0f, 0.0f);

        RenderSystem.activeTexture(GL_TEXTURE2);
        client.getTextureManager().getTexture(LIGHTING_TEXTURE).bindTexture();
        RenderSystem.activeTexture(GL_TEXTURE3);
        client.getTextureManager().getTexture(NOISE2_TEXTURE).bindTexture();
        RenderSystem.activeTexture(GL_TEXTURE5);
        client.getTextureManager().getTexture(NOISE_TEXTURE).bindTexture();

        cloudGenerator.bind();

        if(cloudGenerator.canRender()) {
            // TODO: improve grouping
            int runStart = -1;
            int runCount = 0;
            for (ChunkedGenerator.ChunkIndex chunk : cloudGenerator.chunks()) {
                Box bounds = chunk.bounds(cloudsHeight, Main.CONFIG.sizeXZ, Main.CONFIG.sizeY);
                if(!frustum.isVisible(bounds)) {
                    if(runCount != 0) {
                        GL44.glDrawArraysInstancedBaseInstance(GL_TRIANGLE_STRIP, 0, cloudGenerator.instanceVertexCount(), runCount, runStart);
                    }
                    runStart = -1;
                    runCount = 0;
                } else {
                    if(runStart == -1) runStart = chunk.start();
                    runCount += chunk.count();
                }
            }
            if(runCount != 0) {
                GL44.glDrawArraysInstancedBaseInstance(GL_TRIANGLE_STRIP, 0, cloudGenerator.instanceVertexCount(), runCount, runStart);
            }
        }
    }

    private void drawDepth() {
        RenderSystem.disableBlend();
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL_ALWAYS);
        RenderSystem.depthMask(true);

        depthShader.bind();
        depthShader.uClipPlanes.setVec4(
            (float) viewboxTransform.linearizeFactor(),
            (float) viewboxTransform.linearizeAddend(),
            (float) viewboxTransform.hyperbolizeFactor(),
            (float) viewboxTransform.hyperbolizeAddend()
        );

        RenderSystem.activeTexture(GL_TEXTURE0);
        RenderSystem.bindTexture(client.getFramebuffer().getDepthAttachment());

        glBindVertexArray(quadVao);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    }

    private boolean isFancyMode() {
        return client.options.getCloudRenderModeValue() == CloudRenderMode.FANCY;
    }

    public void close() {
        deleteShaders();
        deleteGenerator();
        deleteTimer();
        deleteQuad();
        deleteFramebuffer();
    }
}
