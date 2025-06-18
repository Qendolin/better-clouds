package com.qendolin.betterclouds.clouds;

import com.mojang.blaze3d.platform.GlStateManager;
import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.clouds.shaders.SkyShader;
import com.qendolin.betterclouds.compat.AstrocraftCompat;
import mod.lwhrvw.astrocraft.Astrocraft;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.io.IOException;

import static com.qendolin.betterclouds.compat.GLCompat.glCompat;
import static org.lwjgl.opengl.GL32.*;

public class SkyRenderer {

    public static final float[] DISK_VERTICES = {
        -1.0f, -1.0f, 0.0f, // Bottom-left
        -1.0f,  1.0f, 0.0f, // Top-left
        1.0f,  1.0f, 0.0f, // Top-right
        1.0f, -1.0f, 0.0f  // Bottom-right
    };

    int fbo;
    int texture;

    int vao;
    int vbo;
    SkyShader shader;

    public void init(ResourceManager resourceManager, int fboWidth, int fboHeight) {
        fbo = glGenFramebuffers();

        // Code is for 1.21.4

        GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fbo);
        glCompat.objectLabelDev(GL_FRAMEBUFFER, fbo, "sky");

        texture = glGenTextures();
        GlStateManager._activeTexture(GL_TEXTURE0);
        GlStateManager._bindTexture(texture);
        glCompat.objectLabelDev(GL_TEXTURE, texture, "sky_color");
        glCompat.texStorage2DFallback(GL_TEXTURE_2D, 1, GL_RGBA4, fboWidth, fboHeight, GL_RGBA, GL_BYTE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);
        glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0});

        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Frame buffer not complete");
        }

        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        glCompat.objectLabelDev(glCompat.GL_VERTEX_ARRAY, vao, "disk");

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glCompat.objectLabelDev(glCompat.GL_BUFFER, vbo, "disk");

        glBufferData(GL_ARRAY_BUFFER, DISK_VERTICES, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        Resources.unbindVao();
        Resources.unbindVbo();

        try {
            shader = SkyShader.create(resourceManager);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void render() {
        Vector2f obsDirection = AstrocraftCompat.getSunDirection();
        Matrix4f matrix = genMatrix(obsDirection.x, obsDirection.y);

        // TODO: Render sun and moon to fbo with sky shader

    }

    private static Matrix4f genMatrix(double rightAscension, double declination) {
        Matrix4f mat = new Matrix4f(mod.lwhrvw.astrocraft.SkyRenderer.getEquatorialMatrix());
        mat.rotate(RotationAxis.NEGATIVE_X.rotationDegrees((float)(180.0 + rightAscension)));
        mat.rotate(RotationAxis.NEGATIVE_Z.rotationDegrees((float)declination));
        return mat;
    }
}
