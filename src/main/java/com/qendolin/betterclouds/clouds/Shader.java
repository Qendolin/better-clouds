package com.qendolin.betterclouds.clouds;

import com.mojang.blaze3d.platform.GlStateManager;
import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.mixin.ShaderProgramAccessor;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidHierarchicalFileException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.lwjgl.opengl.GL32.*;

public class Shader implements AutoCloseable {

    private final Map<String, String> defs;

    protected int programId;

    public Shader(ResourceManager resMan, Identifier vshId, Identifier fshId, Map<String, String> defs) throws IOException {
        this.defs = defs;
        int vsh = compileShader(GL_VERTEX_SHADER, vshId, resMan);
        int fsh = compileShader(GL_FRAGMENT_SHADER, fshId, resMan);

        Main.glCompat.objectLabel(Main.glCompat.GL_SHADER, vsh, vshId.getPath());
        Main.glCompat.objectLabel(Main.glCompat.GL_SHADER, fsh, fshId.getPath());

        programId = GlStateManager.glCreateProgram();
        glAttachShader(programId, vsh);
        glAttachShader(programId, fsh);

        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            String log = glGetProgramInfoLog(programId);
            throw new IllegalStateException("Failed to link program: " + log);
        }

        GlStateManager.glDeleteShader(vsh);
        GlStateManager.glDeleteShader(fsh);
    }

    protected int compileShader(int type, Identifier resource, ResourceManager resMan) throws IOException {
        String shaderSrc;
        try {
            InputStream stream = resMan.getResourceOrThrow(resource).getInputStream();
            shaderSrc = IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            InvalidHierarchicalFileException fileEx = InvalidHierarchicalFileException.wrap(ex);
            fileEx.addInvalidFile(resource.toString());
            throw fileEx;
        }
        for (Map.Entry<String, String> entry : defs.entrySet()) {
            shaderSrc = shaderSrc.replace(entry.getKey(), entry.getValue());
        }
        int id = GlStateManager.glCreateShader(type);
        GlStateManager.glShaderSource(id, Collections.singletonList(shaderSrc));
        GlStateManager.glCompileShader(id);
        if (GlStateManager.glGetShaderi(id, GL_COMPILE_STATUS) == 0) {
            String log = StringUtils.trim(GlStateManager.glGetShaderInfoLog(id, 32768));
            InvalidHierarchicalFileException parseEx = new InvalidHierarchicalFileException("Couldn't compile shader program (" + resource + ") : " + log);
            parseEx.addInvalidFile(resource.toString());
            throw parseEx;
        }
        return id;
    }

    @Override
    public void close() {
        if(programId != 0) glDeleteProgram(programId);
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

    public static void unbind() {
        int previousProgramId = ShaderProgramAccessor.getActiveProgramGlRef();
        if(previousProgramId > 0)
            glUseProgram(previousProgramId);
    }

    protected Uniform getUniform(String name, boolean cached) {
        int location = glGetUniformLocation(programId, name);
        if(location < 0) {
            return new Uniform.Noop(name, location);
        }
        if(cached) {
            return new Uniform.Cached(name, location);
        }
        return new Uniform.Simple(name, location);
    }
}
