package com.qendolin.betterclouds.clouds.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.clouds.Resources;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidHierarchicalFileException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL43;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;

public class CullingShader implements Closeable {

    public static final Identifier COMPUTE_SHADER_ID = Identifier.of(Main.MODID, "shaders/core/betterclouds_culling.comp");

    public final Uniform uFrustumPlaneTop;
    public final Uniform uFrustumPlaneRight;
    public final Uniform uFrustumPlaneBottom;
    public final Uniform uFrustumPlaneLeft;
    public final Uniform uOrigin;
    public final Uniform uCloudCount;

    protected int programId;

    public CullingShader(ResourceManager resMan) throws IOException {
        int comp = compileShader(GL43.GL_COMPUTE_SHADER, COMPUTE_SHADER_ID, resMan);

        Main.glCompat.objectLabelDev(Main.glCompat.GL_SHADER, comp, COMPUTE_SHADER_ID.getPath());

        programId = GlStateManager.glCreateProgram();
        glAttachShader(programId, comp);

        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            String log = glGetProgramInfoLog(programId);
            throw new IllegalStateException("Failed to link program: " + log);
        }

        GlStateManager.glDeleteShader(comp);

        uFrustumPlaneTop = getUniform("u_frustum_planes[0]", true);
        uFrustumPlaneRight = getUniform("u_frustum_planes[1]", true);
        uFrustumPlaneBottom = getUniform("u_frustum_planes[2]", true);
        uFrustumPlaneLeft = getUniform("u_frustum_planes[3]", true);
        uOrigin = getUniform("u_origin", false);
        uCloudCount = getUniform("u_cloud_count", true);
    }

    protected int compileShader(int type, Identifier resource, ResourceManager resMan) throws IOException {
        String shaderSrc;
        try {
            InputStream stream = resMan.getResourceOrThrow(resource).getInputStream();
            shaderSrc = IOUtils.toString(stream, StandardCharsets.UTF_8);
            shaderSrc = shaderSrc.strip();
        } catch (IOException ex) {
            InvalidHierarchicalFileException fileEx = InvalidHierarchicalFileException.wrap(ex);
            fileEx.addInvalidFile(resource.toString());
            throw fileEx;
        }
        int id = GlStateManager.glCreateShader(type);
        glShaderSource(id, shaderSrc);
        glCompileShader(id);
        if (GlStateManager.glGetShaderi(id, GL_COMPILE_STATUS) == 0) {
            String log = StringUtils.trim(GlStateManager.glGetShaderInfoLog(id, 32768));
            InvalidHierarchicalFileException parseEx = new InvalidHierarchicalFileException("Couldn't compile shader program (" + resource + "): \n" + log + "\n\nShader Source: \n" + shaderSrc);
            parseEx.addInvalidFile(resource.toString());
            throw parseEx;
        }
        return id;
    }

    @Override
    public void close() {
        if (programId != 0) glDeleteProgram(programId);
        programId = 0;
    }

    public boolean isIncomplete() {
        return programId <= 0;
    }

    public void bind() {
        glUseProgram(programId);
    }

    public int glId() {
        return programId;
    }

    public void unbind() {
        Resources.unbindShader();
    }

    protected Uniform getUniform(String name, boolean cached) {
        int location = glGetUniformLocation(programId, name);
        if (location < 0) {
            return new Uniform.Noop(name, location);
        }
        if (cached) {
            return new Uniform.Cached(name, location);
        }
        return new Uniform.Simple(name, location);
    }
}
