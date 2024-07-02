package com.qendolin.betterclouds.clouds;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.clouds.shaders.CoverageShader;
import com.qendolin.betterclouds.clouds.shaders.DepthShader;
import com.qendolin.betterclouds.clouds.shaders.ShaderParameters;
import com.qendolin.betterclouds.clouds.shaders.ShadingShader;
import com.qendolin.betterclouds.compat.Telemetry;
import com.qendolin.betterclouds.mixin.BufferRendererAccessor;
import com.qendolin.betterclouds.mixin.ShaderProgramAccessor;
import com.qendolin.betterclouds.mixin.VertexBufferAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.Closeable;
import java.io.IOException;

import static com.qendolin.betterclouds.Main.LOGGER;
import static com.qendolin.betterclouds.Main.glCompat;
import static org.lwjgl.opengl.GL32.*;

public class Resources implements Closeable {
    // Texture Unit 5
    public static final Identifier NOISE_TEXTURE = Identifier.of(Main.MODID, "textures/environment/cloud_noise_rgb.png");
    // Texture Unit 4
    public static final Identifier LIGHTING_TEXTURE = Identifier.of(Main.MODID, "textures/environment/cloud_light_gradient.png");

    private static final int UNASSIGNED = 0;

    // Shaders
    private DepthShader depthShader = null;
    private CoverageShader coverageShader = null;
    private ShadingShader shadingShader = null;

    // Generator
    private ChunkedGenerator generator = null;

    // Meshes
    private int cubeVbo;
    private int cubeVao;

    // FBO
    private int oitFbo;
    // Texture Unit 1
    private int oitCoverageDepthTexture;
    // Texture Unit 2
    private int oitDataTexture;
    // Texture Unit 3
    private int oitCoverageTexture;
    private int fboWidth;
    private int fboHeight;

    private GlTimer timer;

    public ChunkedGenerator generator() {
        return generator;
    }


    public DepthShader depthShader() {
        return depthShader;
    }

    public CoverageShader coverageShader() {
        return coverageShader;
    }

    public ShadingShader shadingShader() {
        return shadingShader;
    }

    public int cubeVao() {
        return cubeVao;
    }

    public int oitFbo() {
        return oitFbo;
    }

    public int oitCoverageDepthTexture() {
        return oitCoverageDepthTexture;
    }

    public int oitDataTexture() {
        return oitDataTexture;
    }

    public int oitCoverageTexture() {
        return oitCoverageTexture;
    }

    public GlTimer timer() {
        return timer;
    }

    public int fboWidth() {
        return fboWidth;
    }

    public int fboHeight() {
        return fboHeight;
    }

    public boolean failedToLoadCritical() {
        if (depthShader == null || coverageShader == null || shadingShader == null) return true;
        if (depthShader.isIncomplete() || coverageShader.isIncomplete() || shadingShader.isIncomplete()) return true;
        if (generator == null) return true;
        if (oitFbo == UNASSIGNED) return true;
        if (oitDataTexture == UNASSIGNED || oitCoverageTexture == UNASSIGNED)
            return true;
        if (cubeVao == UNASSIGNED || cubeVbo == UNASSIGNED) return true;

        return false;
    }

    public void reloadTimer() {
        deleteTimer();
        if (!Main.isProfilingEnabled()) return;

        timer = new GlTimer();
    }

    public void deleteTimer() {
        if (timer != null) timer.close();
        timer = null;
    }

    public void reloadMeshPrimitives() {
        deleteMeshPrimitives();

        cubeVao = glGenVertexArrays();
        glBindVertexArray(cubeVao);
        glCompat.objectLabelDev(glCompat.GL_VERTEX_ARRAY, cubeVao, "cube");

        cubeVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, cubeVbo);
        glCompat.objectLabelDev(glCompat.GL_BUFFER, cubeVbo, "cube");

        glBufferData(GL_ARRAY_BUFFER, Mesh.CUBE_MESH, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        unbindVao();
        unbindVbo();
    }

    public void deleteMeshPrimitives() {
        if (cubeVbo != 0) glDeleteBuffers(cubeVbo);
        if (cubeVao != 0) glDeleteVertexArrays(cubeVao);
        cubeVbo = UNASSIGNED;
        cubeVao = UNASSIGNED;
    }

    public static void unbindVao() {
        VertexBufferAccessor buffer = (VertexBufferAccessor) BufferRendererAccessor.getCurrentVertexBuffer();
        if (buffer == null) return;
        int previousVaoId = buffer.getVertexArrayId();
        if (previousVaoId > 0)
            glBindVertexArray(previousVaoId);
    }

    public static void unbindVbo() {
        VertexBufferAccessor buffer = (VertexBufferAccessor) BufferRendererAccessor.getCurrentVertexBuffer();
        if (buffer == null) return;
        int previousVboId = buffer.getVertexBufferId();
        if (previousVboId > 0)
            glBindBuffer(GL_ARRAY_BUFFER, previousVboId);
    }

    public void reloadTextures(MinecraftClient client) {
        int noiseTexture = client.getTextureManager().getTexture(NOISE_TEXTURE).getGlId();
        RenderSystem.activeTexture(GL_TEXTURE0);
        RenderSystem.bindTexture(noiseTexture);
        glCompat.objectLabelDev(GL_TEXTURE, noiseTexture, "noise");
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GL_REPEAT);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GL_REPEAT);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_LINEAR);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_LINEAR);
        int lightingTexture = client.getTextureManager().getTexture(LIGHTING_TEXTURE).getGlId();
        RenderSystem.bindTexture(lightingTexture);
        glCompat.objectLabelDev(GL_TEXTURE, lightingTexture, "lighting");
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GL_REPEAT);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_LINEAR);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_LINEAR);

        RenderSystem.bindTexture(0);
    }

    public void reloadGenerator(boolean fancy) {
        deleteGenerator();

        generator = new ChunkedGenerator();
        generator.allocate(Main.getConfig(), fancy);
        generator.clear();
        generator.unbind();
    }

    public void deleteGenerator() {
        if (generator != null) generator.close();
        generator = null;
    }

    public void reloadFramebuffer(int width, int height) {
        if (width == 0 || height == 0) {
            LOGGER.warn("Cannot create framebuffer with size 0 ({}x{})! Skipping framebuffer creation to avoid an error.", width, height);
            return;
        }
        deleteFramebuffer();

        oitFbo = glGenFramebuffers();
        GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, oitFbo);
        glCompat.objectLabelDev(GL_FRAMEBUFFER, oitFbo, "coverage");

        fboWidth = width;
        fboHeight = height;

        oitDataTexture = glGenTextures();
        RenderSystem.activeTexture(GL_TEXTURE0);
        RenderSystem.bindTexture(oitDataTexture);
        glCompat.objectLabelDev(GL_TEXTURE, oitDataTexture, "coverage_color");
        glCompat.texStorage2DFallback(GL_TEXTURE_2D, 1, GL_RGB8, fboWidth, fboHeight, GL_RGB, GL_BYTE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, oitDataTexture, 0);
        glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0});

        if (glCompat.useStencilTextureFallback()) {
            oitCoverageTexture = glGenTextures();
            RenderSystem.bindTexture(oitCoverageTexture);
            glCompat.objectLabelDev(GL_TEXTURE, oitCoverageTexture, "coverage_color_fallback");
            glCompat.texStorage2DFallback(GL_TEXTURE_2D, 1, GL_R8, fboWidth, fboHeight, GL_RED, GL_UNSIGNED_BYTE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, oitCoverageTexture, 0);
            glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1});

            oitCoverageDepthTexture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, oitCoverageDepthTexture);
            glCompat.objectLabelDev(GL_TEXTURE, oitCoverageDepthTexture, "coverage_depth");
            glCompat.texStorage2DFallback(GL_TEXTURE_2D, 1, GL_DEPTH_COMPONENT24, fboWidth, fboHeight, GL_DEPTH_COMPONENT, GL_FLOAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, oitCoverageDepthTexture, 0);
        } else {
            oitCoverageTexture = glGenTextures();
            RenderSystem.bindTexture(oitCoverageTexture);
            glCompat.objectLabelDev(GL_TEXTURE, oitCoverageTexture, "coverage_stencil");
            glCompat.texStorage2D(GL_TEXTURE_2D, 1, GL_DEPTH24_STENCIL8, fboWidth, fboHeight);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, glCompat.GL_DEPTH_STENCIL_TEXTURE_MODE, GL_STENCIL_INDEX);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_TEXTURE_2D, oitCoverageTexture, 0);

            if (glCompat.useDepthWriteFallback()) {
                oitCoverageDepthTexture = oitCoverageTexture;
            } else {
                oitCoverageDepthTexture = glGenTextures();
                glCompat.textureView(oitCoverageDepthTexture, GL_TEXTURE_2D, oitCoverageTexture, GL_DEPTH24_STENCIL8, 0, 1, 0, 1);
                glBindTexture(GL_TEXTURE_2D, oitCoverageDepthTexture);
                glCompat.objectLabelDev(GL_TEXTURE, oitCoverageDepthTexture, "coverage_depth");
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, glCompat.GL_DEPTH_STENCIL_TEXTURE_MODE, GL_DEPTH_COMPONENT);
            }
        }

        RenderSystem.bindTexture(0);

        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Better Clouds framebuffer incomplete, your GPU is likely incompatible, status: " + status);
        }
    }

    public void deleteFramebuffer() {
        if (oitFbo != 0) glDeleteFramebuffers(oitFbo);
        if (oitDataTexture != 0) RenderSystem.deleteTexture(oitDataTexture);
        if (oitCoverageTexture != 0) RenderSystem.deleteTexture(oitCoverageTexture);
        if (oitCoverageDepthTexture != 0) RenderSystem.deleteTexture(oitCoverageDepthTexture);
        oitFbo = UNASSIGNED;
        oitDataTexture = UNASSIGNED;
        oitCoverageTexture = UNASSIGNED;
        oitCoverageDepthTexture = UNASSIGNED;
    }

    public void reloadShaders(ResourceManager manager, ShaderParameters shaderParameters) {
        try {
            reloadShadersInternal(manager, shaderParameters);
        } catch (Exception e) {
            Main.sendGpuIncompatibleChatMessage();
            Main.LOGGER.error(e);
            Telemetry.INSTANCE.sendShaderCompileError(e.toString());
            deleteShaders();
        }
        unbindShader();
    }

    protected void reloadShadersInternal(ResourceManager manager, ShaderParameters shaderParameters) throws IOException {
        deleteShaders();

        depthShader = DepthShader.create(manager);
        depthShader.bind();
        depthShader.uDepthTexture.setInt(6);
        glCompat.objectLabelDev(glCompat.GL_PROGRAM, depthShader.glId(), "depth");

        int edgeFade = (int) (shaderParameters.configFadeEdge() * shaderParameters.blockViewDistance());
        coverageShader = CoverageShader.create(manager,
            shaderParameters.configSizeXZ(),
            shaderParameters.configSizeY(),
            edgeFade,
            shaderParameters.useStencilTextureFallback(),
            shaderParameters.useDistantHorizonsCompat(),
            shaderParameters.worldCurvatureSize());
        coverageShader.bind();
        coverageShader.uDepthTexture.setInt(0);
        coverageShader.uNoiseTexture.setInt(5);
        coverageShader.uDhDepthTexture.setInt(6);
        glCompat.objectLabelDev(glCompat.GL_PROGRAM, coverageShader.glId(), "coverage");

        shadingShader = ShadingShader.create(manager,
            shaderParameters.useDepthWriteFallback(),
            shaderParameters.useStencilTextureFallback(),
            shaderParameters.configCelestialBodyHalo());
        shadingShader.bind();
        shadingShader.uDepthTexture.setInt(1);
        shadingShader.uDataTexture.setInt(2);
        shadingShader.uCoverageTexture.setInt(3);
        shadingShader.uLightTexture.setInt(4);
        glCompat.objectLabelDev(glCompat.GL_PROGRAM, shadingShader.glId(), "shading");
    }

    public void deleteShaders() {
        if (depthShader != null) depthShader.close();
        if (coverageShader != null) coverageShader.close();
        if (shadingShader != null) shadingShader.close();
        depthShader = null;
        coverageShader = null;
        shadingShader = null;
    }

    public static void unbindShader() {
        int previousProgramId = ShaderProgramAccessor.getActiveProgramGlRef();
        if (previousProgramId > 0)
            glUseProgram(previousProgramId);
    }

    @Override
    public void close() {
        deleteFramebuffer();
        deleteMeshPrimitives();
        deleteGenerator();
        deleteShaders();
        deleteTimer();
    }
}
