package com.qendolin.betterclouds.clouds;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import com.qendolin.betterclouds.Config;
import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.clouds.shaders.CoverageShader;
import com.qendolin.betterclouds.clouds.shaders.DepthShader;
import com.qendolin.betterclouds.clouds.shaders.ShadingShader;
import com.qendolin.betterclouds.compat.Telemetry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.Closeable;
import java.io.IOException;

import static com.qendolin.betterclouds.Main.glCompat;
import static org.lwjgl.opengl.GL32.*;

public class Resources implements Closeable {
    // Texture Unit 5
    public static final Identifier NOISE_TEXTURE = new Identifier(Main.MODID, "textures/environment/cloud_noise_rgb.png");
    // Texture Unit 4
    public static final Identifier LIGHTING_TEXTURE = new Identifier(Main.MODID, "textures/environment/cloud_light_gradient.png");

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
    private int oitCoverageDepthView;
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

    public int oitCoverageDepthView() {
        return oitCoverageDepthView;
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
        if (oitDataTexture == UNASSIGNED || oitCoverageTexture == UNASSIGNED || oitCoverageDepthView == UNASSIGNED)
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
        glCompat.objectLabel(glCompat.GL_VERTEX_ARRAY, cubeVao, "cube");

        cubeVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, cubeVbo);
        glCompat.objectLabel(glCompat.GL_BUFFER, cubeVbo, "cube");

        glBufferData(GL_ARRAY_BUFFER, Mesh.CUBE_MESH, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
    }

    public void deleteMeshPrimitives() {
        if (cubeVbo != 0) glDeleteBuffers(cubeVbo);
        if (cubeVao != 0) glDeleteVertexArrays(cubeVao);
        cubeVbo = UNASSIGNED;
        cubeVao = UNASSIGNED;
    }

    public void reloadTextures(MinecraftClient client) {
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

    public void reloadGenerator(boolean fancy) {
        deleteGenerator();
        generator = new ChunkedGenerator();
        generator.allocate(Main.getConfig(), fancy);
        generator.clear();
    }

    public void deleteGenerator() {
        if (generator != null) generator.close();
        generator = null;
    }

    public void reloadFramebuffer(int width, int height) {
        deleteFramebuffer();

        oitFbo = glGenFramebuffers();
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, oitFbo);
        glCompat.objectLabel(GL_FRAMEBUFFER, oitFbo, "coverage");

        fboWidth = width;
        fboHeight = height;

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
        glCompat.objectLabel(GL_TEXTURE, oitCoverageDepthView, "coverage_depth");
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, glCompat.GL_DEPTH_STENCIL_TEXTURE_MODE, GL_DEPTH_COMPONENT);


        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Clouds framebuffer incomplete, status: " + status);
        }
    }

    public void deleteFramebuffer() {
        if (oitFbo != 0) glDeleteFramebuffers(oitFbo);
        if (oitDataTexture != 0) RenderSystem.deleteTexture(oitDataTexture);
        if (oitCoverageTexture != 0) RenderSystem.deleteTexture(oitCoverageTexture);
        if (oitCoverageDepthView != 0) RenderSystem.deleteTexture(oitCoverageDepthView);
        oitFbo = UNASSIGNED;
        oitDataTexture = UNASSIGNED;
        oitCoverageTexture = UNASSIGNED;
        oitCoverageDepthView = UNASSIGNED;
    }

    public void reloadShaders(ResourceManager manager) {
        try {
            reloadShadersInternal(manager, false);
        } catch (Exception ignored) {
            try {
                reloadShadersInternal(manager, true);
            } catch (Exception e) {
                Main.sendGpuIncompatibleChatMessage();
                Main.LOGGER.error(e);
                if (Telemetry.INSTANCE != null) {
                    Telemetry.INSTANCE.sendShaderCompileError(e.toString());
                }
                deleteShaders();
            }
        }
    }

    protected void reloadShadersInternal(ResourceManager manager, boolean safeMode) throws IOException {
        deleteShaders();

        Config config = Main.getConfig();

        depthShader = DepthShader.create(manager, (glCompat.openGl42 || glCompat.arbConservativeDepth) && !safeMode);
        depthShader.bind();
        depthShader.uDepthTexture.setInt(0);
        glCompat.objectLabel(glCompat.GL_PROGRAM, depthShader.glId(), "depth");

        coverageShader = CoverageShader.create(manager, config.sizeXZ, config.sizeY, (int) (config.fadeEdge * config.blockDistance()));
        coverageShader.bind();
        coverageShader.uNoiseTexture.setInt(5);
        glCompat.objectLabel(glCompat.GL_PROGRAM, coverageShader.glId(), "coverage");

        shadingShader = ShadingShader.create(manager, config.writeDepth);
        shadingShader.bind();
        shadingShader.uDepthTexture.setInt(1);
        shadingShader.uDataTexture.setInt(2);
        shadingShader.uCoverageTexture.setInt(3);
        shadingShader.uLightTexture.setInt(4);
        glCompat.objectLabel(glCompat.GL_PROGRAM, shadingShader.glId(), "shading");
    }

    public void deleteShaders() {
        if (depthShader != null) depthShader.close();
        if (coverageShader != null) coverageShader.close();
        if (shadingShader != null) shadingShader.close();
        depthShader = null;
        coverageShader = null;
        shadingShader = null;
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
