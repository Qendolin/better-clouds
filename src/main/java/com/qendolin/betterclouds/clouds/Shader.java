package com.qendolin.betterclouds.clouds;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.qendolin.betterclouds.Main;
import com.qendolin.betterclouds.mixin.ShaderAccessor;
import net.minecraft.client.gl.ShaderParseException;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.lwjgl.opengl.GL32.*;

public class Shader implements AutoCloseable {
    public static final String DEF_SIZE_X_KEY = "_SCALE_X_";
    public static final String DEF_SIZE_Y_KEY = "_SCALE_Y_";
    public static final String DEF_FADE_EDGE_KEY = "_VISIBILITY_EDGE_";

    public static final Identifier VERTEX_SHADER_FAST_ID = new Identifier(Main.MODID, "shaders/core/clouds_fast.vsh");
    public static final Identifier VERTEX_SHADER_FANCY_ID = new Identifier(Main.MODID, "shaders/core/clouds_fancy.vsh");
    public static final Identifier FRAGMENT_SHADER_FAST_ID = new Identifier(Main.MODID, "shaders/core/clouds_fast.fsh");
    public static final Identifier FRAGMENT_SHADER_FAST_IRIS_ID = new Identifier(Main.MODID, "shaders/core/clouds_fast-iris.fsh");
    public static final Identifier FRAGMENT_SHADER_FANCY_ID = new Identifier(Main.MODID, "shaders/core/clouds_fancy.fsh");
    public static final Identifier FRAGMENT_SHADER_FANCY_IRIS_ID = new Identifier(Main.MODID, "shaders/core/clouds_fancy-iris.fsh");

    public final Uniform uSunDirection;
    public final Uniform uModelViewProjMat;
    public final Uniform uSkyColor;
    public final Uniform uSkyColorOverride;
    public final Uniform uCloudsPosition;
    public final Uniform uCloudsDistance;

    private final Map<String, Float> defs;
    private final boolean fancy;

    private int programId;

    public Shader(ResourceManager resMan, boolean fancy, boolean iris, Map<String, Float> defs) throws IOException {
        this.defs = defs;
        this.fancy = fancy;

        Identifier vshId = fancy ? VERTEX_SHADER_FANCY_ID : VERTEX_SHADER_FAST_ID;
        int vsh = compileShader(GL_VERTEX_SHADER, vshId, resMan);
        Identifier fshId;
        if(fancy) {
            fshId = iris ? FRAGMENT_SHADER_FANCY_IRIS_ID : FRAGMENT_SHADER_FANCY_ID;
        } else {
            fshId = iris ? FRAGMENT_SHADER_FAST_IRIS_ID : FRAGMENT_SHADER_FAST_ID;
        }
        int fsh = compileShader(GL_FRAGMENT_SHADER, fshId, resMan);

        programId = GlStateManager.glCreateProgram();
        glAttachShader(programId, vsh);
        glAttachShader(programId, fsh);

        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new IllegalStateException("Failed to link program");
        }

        GlStateManager.glDeleteShader(vsh);
        GlStateManager.glDeleteShader(fsh);

        uSunDirection = getUniform("u_sunDirection", true);
        uModelViewProjMat = getUniform("u_modelViewProjMat", false);
        uSkyColor = getUniform("u_skyColor", true);
        uSkyColorOverride = getUniform("u_skyColorOverride", true);
        uCloudsPosition = getUniform("u_cloudsPosition", false);
        uCloudsDistance = getUniform("u_cloudsDistance", true);
    }

    private int compileShader(int type, Identifier resource, ResourceManager resMan) throws IOException {
        String shaderSrc;
        try {
            var srcStream = resMan.getResource(resource).getInputStream();
            shaderSrc = TextureUtil.readResourceAsString(srcStream);
        } catch (IOException ex) {
            ShaderParseException parseEx = ShaderParseException.wrap(ex);
            parseEx.addFaultyFile(resource.toString());
            throw parseEx;
        }
        for (Map.Entry<String, Float> entry : defs.entrySet()) {
            shaderSrc = shaderSrc.replace(entry.getKey(), entry.getValue().toString());
        }
        int id = GlStateManager.glCreateShader(type);
        GlStateManager.glShaderSource(id, Collections.singletonList(shaderSrc));
        GlStateManager.glCompileShader(id);
        if (GlStateManager.glGetShaderi(id, GL_COMPILE_STATUS) == 0) {
            String log = StringUtils.trim(GlStateManager.glGetShaderInfoLog(id, 32768));
            ShaderParseException parseEx = new ShaderParseException("Couldn't compile shader program (" + resource + ") : " + log);
            parseEx.addFaultyFile(resource.toString());
            throw parseEx;
        }
        return id;
    }

    @Override
    public void close() {
        glDeleteProgram(programId);
        programId = 0;
    }

    public boolean isComplete() {
        return programId > 0;
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        int previousProgramId = ShaderAccessor.getActiveShader();
        if(previousProgramId > 0)
            glUseProgram(previousProgramId);
    }

    private Uniform getUniform(String name, boolean cached) {
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
