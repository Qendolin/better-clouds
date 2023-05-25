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
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.qendolin.betterclouds.Main.glCompat;
import static org.lwjgl.opengl.GL32.*;

public class Renderer implements AutoCloseable {

    // Texture Unit 5
    private static final Identifier NOISE_TEXTURE = new Identifier(Main.MODID, "textures/environment/cloud_noise_rgb.png");
    // Texture Unit 4
    private static final Identifier LIGHTING_TEXTURE = new Identifier(Main.MODID, "textures/environment/cloud_gradient.png");

    private final MinecraftClient client;
    private ClientWorld world = null;

    private DepthShader depthShader = null;
    private CoverageShader coverageShader = null;
    private BlitShader blitShader = null;
    private ChunkedGenerator generator = null;
    private IViewboxTransform viewboxTransform = null;
    private final long startTime = Util.getEpochTimeMs();
    private int quadVbo;
    private int quadVao;
    private int oitFbo;
    // Texture Unit 1
    private int oitCoverageDepthView;
    // Texture Unit 2
    private int oitDataTexture;
    // Texture Unit 3
    private int oitCoverageTexture;
    private int fboWidth;
    private int fboHeight;
    private float cloudsHeight;
    private int defaultFbo;
    private float raininess;
    private final Matrix4f mvpMatrix = new Matrix4f();
    private final Matrix4f inverseMatrix = new Matrix4f();
    private final Matrix4f tempMatrix = new Matrix4f();
    private final PrimitiveChangeDetector shaderInvalidator = new PrimitiveChangeDetector(false);
    private final PrimitiveChangeDetector viewboxTransformInvalidator = new PrimitiveChangeDetector(true);

    private GlTimer timer;

    public Renderer(MinecraftClient client) {
        this.client = client;
    }

    public void setWorld(ClientWorld world) {
        this.world = world;
    }

    public void reload(ResourceManager manager) {
        Main.LOGGER.info("Reloading cloud renderer...");

        Main.LOGGER.debug("[1/6] Reloading shaders");
        reloadShaders(manager);
        Main.LOGGER.debug("[2/6] Reloading generator");
        reloadGenerator();
        Main.LOGGER.debug("[3/6] Reloading textures");
        reloadTextures();
        Main.LOGGER.debug("[4/6] Reloading quad mesh");
        reloadQuad();
        Main.LOGGER.debug("[5/6] Reloading framebuffer");
        reloadFramebuffer();
        Main.LOGGER.debug("[6/6] Reloading timers");
        reloadTimer();

        Main.LOGGER.info("Cloud renderer initialized");
    }

    private void deleteTimer() {
        if(timer != null) timer.close();
    }

    private void reloadTimer() {
        deleteTimer();

        if(!Main.isProfilingEnabled()) return;
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
        glCompat.objectLabel(glCompat.GL_VERTEX_ARRAY, quadVao, "quad");

        quadVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, quadVbo);
        glCompat.objectLabel(glCompat.GL_BUFFER, quadVbo, "quad");

        glBufferData(GL_ARRAY_BUFFER, new float[]{1,-1, 1,1, -1,-1, -1,1}, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
    }

    private void reloadTextures() {
        int noiseTexture = client.getTextureManager().getTexture(NOISE_TEXTURE).getGlId();
        RenderSystem.activeTexture(GL_TEXTURE0);
        RenderSystem.bindTexture(noiseTexture);
        glCompat.objectLabel(GL_TEXTURE, noiseTexture, "noise");
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GL_REPEAT);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GL_REPEAT);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_LINEAR);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_LINEAR);
        int lightingTexture = client.getTextureManager().getTexture(LIGHTING_TEXTURE).getGlId();
        RenderSystem.bindTexture(lightingTexture);
        glCompat.objectLabel(GL_TEXTURE, lightingTexture, "lighting");
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GL_REPEAT);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_LINEAR);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_LINEAR);
    }

    private void deleteGenerator() {
        if(generator != null) generator.close();
    }

    private void reloadGenerator() {
        deleteGenerator();
        generator = new ChunkedGenerator();
        generator.allocate(Main.getConfig(), isFancyMode());
        generator.clear();
    }

    private void deleteFramebuffer() {
        if(oitFbo != 0) glDeleteFramebuffers(oitFbo);
        if(oitDataTexture != 0) RenderSystem.deleteTexture(oitDataTexture);
        if(oitCoverageTexture != 0) RenderSystem.deleteTexture(oitCoverageTexture);
        if(oitCoverageDepthView != 0) RenderSystem.deleteTexture(oitCoverageDepthView);
    }

    private void reloadFramebuffer() {
        deleteFramebuffer();

        oitFbo = glGenFramebuffers();
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, oitFbo);
        glCompat.objectLabel(GL_FRAMEBUFFER, oitFbo, "coverage");

        fboWidth = client.getFramebuffer().textureWidth;
        fboHeight = client.getFramebuffer().textureHeight;

        oitDataTexture = glGenTextures();
        RenderSystem.bindTexture(oitDataTexture);
        glCompat.objectLabel(GL_TEXTURE, oitDataTexture, "coverage_color");
        glCompat.texStorage2D(GL_TEXTURE_2D, 1, GL_RGB8, fboWidth, fboHeight);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, oitDataTexture, 0);

        glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0});

        oitCoverageTexture = glGenTextures();
        RenderSystem.bindTexture(oitCoverageTexture);
        glCompat.objectLabel(GL_TEXTURE, oitCoverageTexture, "coverage_stencil");
        glCompat.texStorage2D(GL_TEXTURE_2D, 1, GL_DEPTH24_STENCIL8, fboWidth, fboHeight);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, glCompat.GL_DEPTH_STENCIL_TEXTURE_MODE, GL_STENCIL_INDEX);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_TEXTURE_2D, oitCoverageTexture, 0);

        oitCoverageDepthView = glGenTextures();
        glCompat.textureView(oitCoverageDepthView, GL_TEXTURE_2D, oitCoverageTexture, GL_DEPTH24_STENCIL8, 0, 1, 0, 1);
        glBindTexture(GL_TEXTURE_2D, oitCoverageDepthView);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, glCompat.GL_DEPTH_STENCIL_TEXTURE_MODE, GL_DEPTH_COMPONENT);


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

        Config config = Main.getConfig();

        try {
            depthShader = new DepthShader(manager, Map.ofEntries(
                Map.entry(DepthShader.DEF_REMAP_DEPTH_KEY, config.highQualityDepth ? "1" : "0")
            ));
            depthShader.bind();
            depthShader.uDepth.setInt(0);
            glCompat.objectLabel(glCompat.GL_PROGRAM, depthShader.glId(), "depth");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            Map<String, String> defs = ImmutableMap.ofEntries(
                Map.entry(CoverageShader.DEF_SIZE_XZ_KEY, Float.toString(config.sizeXZ)),
                Map.entry(CoverageShader.DEF_SIZE_Y_KEY, Float.toString(config.sizeY)),
                Map.entry(CoverageShader.DEF_FADE_EDGE_KEY, Integer.toString((int) (config.fadeEdge * config.blockDistance())))
            );

            coverageShader = new CoverageShader(manager, defs);
            coverageShader.bind();
            coverageShader.uNoiseTexture.setInt(5);
            glCompat.objectLabel(glCompat.GL_PROGRAM, coverageShader.glId(), "coverage");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            blitShader = new BlitShader(manager, Map.ofEntries(
                Map.entry(BlitShader.DEF_BLIT_DEPTH_KEY, config.writeDepth ? "1" : "0"),
                Map.entry(BlitShader.DEF_REMAP_DEPTH_KEY, config.highQualityDepth ? "1" : "0")
            ));
            blitShader.bind();
            blitShader.uDepth.setInt(1);
            blitShader.uData.setInt(2);
            blitShader.uCoverage.setInt(3);
            blitShader.uLightTexture.setInt(4);
            glCompat.objectLabel(glCompat.GL_PROGRAM, blitShader.glId(), "blit");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Config getGeneratorConfig() {
        Config config = generator.config();
        if(config != null) return config;
        return Main.getConfig();
    }

    public boolean setup(MatrixStack matrices, Matrix4f projMat, float tickDelta, int ticks, Vec3d cam, Frustum frustum) {
        assert RenderSystem.isOnRenderThread();
        client.getProfiler().swap("render_setup");
        Config config = Main.getConfig();

        if(depthShader == null || coverageShader == null || blitShader == null) return false;
        if(depthShader.isIncomplete() || coverageShader.isIncomplete() || blitShader.isIncomplete()) return false;
        if(!config.irisSupport && IrisCompat.IS_LOADED && IrisCompat.isShadersEnabled()) return false;

        DimensionEffects effects = world.getDimensionEffects();
        if(SodiumExtraCompat.IS_LOADED && effects.getSkyType() == DimensionEffects.SkyType.NORMAL) {
            cloudsHeight = SodiumExtraCompat.getCloudsHeight() + config.yOffset;
        } else {
            cloudsHeight = effects.getCloudsHeight() + config.yOffset;
        }

        if(viewboxTransformInvalidator.hasChanged(config.highQualityDepth)) {
            if(config.highQualityDepth) viewboxTransform = new QualityViewboxTransform();
            else viewboxTransform = new FastViewboxTransform();
        }

        generator.bind();
        if(shaderInvalidator.hasChanged(client.options.getCloudRenderModeValue(), config.blockDistance(),
            config.fadeEdge, config.sizeXZ, config.sizeY, config.writeDepth, config.highQualityDepth)) {
            reloadShaders(client.getResourceManager());
        }
        generator.reallocateIfStale(config, isFancyMode());

        raininess = Math.max(0.6f*world.getRainGradient(tickDelta), world.getThunderGradient(tickDelta));
        float cloudiness = raininess * 0.3f + 0.5f;

        generator.update(cam, tickDelta, Main.getConfig(), cloudiness);
        if(generator.canGenerate() && !generator.generating()) {
            client.getProfiler().swap("generate_clouds");
            generator.generate();
            client.getProfiler().swap("render_setup");
        }

        if(generator.canSwap()) {
            client.getProfiler().swap("swap");
            generator.swap();
            client.getProfiler().swap("render_setup");
        }

        matrices.translate(generator.renderOriginX(cam.x), cloudsHeight-cam.y, generator.renderOriginZ(cam.z));

        float pitch = client.cameraEntity == null ? 0 : client.cameraEntity.getPitch();
        float yaw = client.cameraEntity == null ? 0 : client.cameraEntity.getYaw();
        viewboxTransform.update(projMat, (float) cam.y, pitch, cloudsHeight, getGeneratorConfig());

        inverseMatrix.identity();
        Matrix4f viewRotationMatrix = tempMatrix;
        viewRotationMatrix.identity();
        viewRotationMatrix.rotate(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        viewRotationMatrix.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(yaw + 180.0f));
        inverseMatrix.set(viewboxTransform.getProjection());
        inverseMatrix.mul(viewRotationMatrix);
        inverseMatrix.invert();

        mvpMatrix.set(viewboxTransform.getProjection());
        mvpMatrix.mul(matrices.peek().getPositionMatrix());

        // TODO: don't do this dynamically
        defaultFbo = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING);

        return true;
    }

    // Don't forget to push / pop matrix stack outside
    public void render(MatrixStack matrices, float tickDelta, int ticks, Vec3d cam, Frustum frustum) {
        if(viewboxTransform.isInvalid()) {
            return;
        }
        // Rendering clouds when underwater was making them very visible in unloaded chunks
        if(client.gameRenderer.getCamera().getSubmersionType() != CameraSubmersionType.NONE) return;

        // TODO: Optimize uniforms
        client.getProfiler().swap("render_setup");
        if(Main.isProfilingEnabled()) {
            if(timer == null) reloadTimer();
            timer.start();
        }

        Config config = Main.getConfig();

        if(framebufferStale()) {
            reloadFramebuffer();
        }

        RenderSystem.viewport(0, 0, fboWidth, fboHeight);
        GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, oitFbo);
        RenderSystem.clearDepth(1);
        RenderSystem.clearColor(0, 0, 0, 0);

        client.getProfiler().swap("draw_depth");
        drawDepth();

        client.getProfiler().swap("draw_coverage");
        drawCoverage(tickDelta, cam, frustum);

        client.getProfiler().swap("draw_shading");
        if(IrisCompat.IS_LOADED && IrisCompat.isShadersEnabled() && config.useIrisFBO) {
            IrisCompat.bindFramebuffer();
        } else {
            GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, defaultFbo);
        }
        // TODO: viewport might not be correct always
        RenderSystem.viewport(0, 0, client.getFramebuffer().textureWidth, client.getFramebuffer().textureHeight);

        drawShading(tickDelta);

        client.getProfiler().swap("render_cleanup");

        generator.unbind();
        Shader.unbind();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(GL_LEQUAL);
        RenderSystem.activeTexture(GL_TEXTURE0);
        RenderSystem.colorMask(true, true, true, true);
        glDisable(GL_STENCIL_TEST);
        glStencilFunc(GL_ALWAYS, 0x0, 0xff);

        if(Main.isProfilingEnabled()) {
            timer.stop();

            if(timer.frames() >= Main.profileInterval) {
                List<Double> times = timer.get();
                times.sort(Double::compare);
                double median = times.get(times.size()/2);
                double p25 = times.get((int) Math.ceil(times.size()*0.25));
                double p75 = times.get((int) Math.ceil(times.size()*0.75));
                double min = times.get(0);
                double max = times.get(times.size()-1);
                double average = times.stream().mapToDouble(d -> d).average().orElse(0);
                Main.debugChatMessage(String.format("§cGPU Times§r: %.3f | %.3f | %.3f §7min,avg,max§r || %.3f | %.3f | %.3f §7p25,med,p75§r", min, average, max, p25, median, p75));
                timer.reset();
            }
        }
    }

    private boolean framebufferStale() {
        return fboWidth != client.getFramebuffer().textureWidth || fboHeight != client.getFramebuffer().textureHeight;
    }

    private void drawShading(float tickDelta) {
        Config config = Main.getConfig();

        if(config.writeDepth) {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL_ALWAYS);
        } else {
            RenderSystem.disableDepthTest();
        }

        RenderSystem.enableBlend();
        RenderSystem.blendEquation(GL_FUNC_ADD);
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
        RenderSystem.colorMask(false, false, false, false);
        glColorMaski(0, true, true, true, true);
        glStencilFunc(GL_GREATER, 0x0, 0xff);
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);

        RenderSystem.activeTexture(GL_TEXTURE1);
        RenderSystem.bindTexture(oitCoverageDepthView);
        RenderSystem.activeTexture(GL_TEXTURE2);
        RenderSystem.bindTexture(oitDataTexture);
        RenderSystem.activeTexture(GL_TEXTURE3);
        RenderSystem.bindTexture(oitCoverageTexture);
        RenderSystem.activeTexture(GL_TEXTURE4);
        client.getTextureManager().getTexture(LIGHTING_TEXTURE).bindTexture();

        float effectLuma = getEffectLuminance(tickDelta);
        float skyAngle = world.getSkyAngle(tickDelta);
        float skyAngleRad = world.getSkyAngleRadians(tickDelta);
        float sunPathAngleRad = (float) Math.toRadians(config.sunPathAngle);
        float dayNightFactor = smoothstep(skyAngle, 0.17333f, 0.25965086f) * smoothstep(skyAngle, 0.82667f, 0.7403491f);
        float brightness = dayNightFactor * config.nightBrightness + (1-dayNightFactor) * config.dayBrightness;
        Vector3f sunDir = new Vector3f(0, 1, 0).rotateAxis(skyAngleRad, 0, MathHelper.sin(sunPathAngleRad/2), MathHelper.cos(sunPathAngleRad/2));

        blitShader.bind();
        blitShader.uEffectColor.setVec4((float) Math.pow(effectLuma, config.gamma), 0.0f, 0.0f, config.opacity);
        blitShader.uSunData.setVec4(sunDir.x, sunDir.y, sunDir.z,(world.getTimeOfDay()%24000)/24000f);
        blitShader.uColorGrading.setVec4(brightness, 1f/config.gamma(), config.alphaFactor, config.saturation);
        blitShader.uDepthRange.setVec2((float) viewboxTransform.maxNearPlane(), (float) viewboxTransform.minFarPlane());
        blitShader.uInverseMat.setMat4(inverseMatrix);
        blitShader.uTint.setVec3(config.tintRed, config.tintGreen, config.tintBlue);
        blitShader.uDepthCoeffs.setVec4(
            (float) viewboxTransform.inverseLinearizeFactor(),
            (float) viewboxTransform.inverseLinearizeAddend(),
            (float) viewboxTransform.inverseHyperbolizeFactor(),
            (float) viewboxTransform.inverseHyperbolizeAddend()
        );

        glBindVertexArray(quadVao);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    }

    private float smoothstep(float x, float e0, float e1) {
        x = MathHelper.clamp((x-e0) / (e1-e0), 0, 1);
        return x * x * (3 - 2*x);
    }

    private float getEffectLuminance(float tickDelta) {
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

        float skyAngle = world.getSkyAngleRadians(tickDelta);

        Config generatorConfig = getGeneratorConfig();
        Config config = Main.getConfig();

        coverageShader.bind();
        coverageShader.uModelViewProjMat.setMat4(mvpMatrix);
        coverageShader.uCloudsOrigin.setVec3((float) -generator.renderOriginX(cam.x), (float) cam.y-cloudsHeight, (float) -generator.renderOriginZ(cam.z));
        coverageShader.uCloudsDistance.setFloat(generatorConfig.blockDistance() - generatorConfig.chunkSize/2f);
        coverageShader.uSkyData.setVec4((float) -Math.sin(skyAngle), (float) Math.cos(skyAngle), world.getTimeOfDay()/24000f, 1-0.75f*raininess);
        coverageShader.uCloudsBox.setVec4((float) cam.x, (float) cam.z, (float) cam.y-cloudsHeight, generatorConfig.yRange +config.sizeY);
        coverageShader.uTime.setFloat((Util.getEpochTimeMs() - startTime)/1000f);
        coverageShader.uMiscOptions.setVec4(config.scaleFalloffMin, config.windFactor, 0.0f, 0.0f);

        RenderSystem.activeTexture(GL_TEXTURE5);
        client.getTextureManager().getTexture(NOISE_TEXTURE).bindTexture();

        generator.bind();

        Frustum frustumAtOrigin = new Frustum(frustum);
        frustumAtOrigin.setPosition(cam.x - generator.originX(), cam.y, cam.z - generator.originZ());
        if(generator.canRender()) {
            // TODO: improve grouping
            int runStart = -1;
            int runCount = 0;
            for (ChunkedGenerator.ChunkIndex chunk : generator.chunks()) {
                Box bounds = chunk.bounds(cloudsHeight, config.sizeXZ, config.sizeY);
                if(!frustumAtOrigin.isVisible(bounds)) {
                    if(runCount != 0) {
                        glCompat.drawArraysInstancedBaseInstance(GL_TRIANGLE_STRIP, 0, generator.instanceVertexCount(), runCount, runStart);
                    }
                    runStart = -1;
                    runCount = 0;
                } else {
                    if(runStart == -1) runStart = chunk.start();
                    runCount += chunk.count();
                }
            }
            if(runCount != 0) {
                glCompat.drawArraysInstancedBaseInstance(GL_TRIANGLE_STRIP, 0, generator.instanceVertexCount(), runCount, runStart);
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
        depthShader.uDepthCoeffs.setVec4(
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
