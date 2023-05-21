package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.Main;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL44;

import java.io.IOException;
import java.util.Map;

public class PostShader extends Shader {

    public static final Identifier VERTEX_SHADER_ID = new Identifier(Main.MODID, "shaders/core/clouds_post.vsh");
    public static final Identifier FRAGMENT_SHADER_ID = new Identifier(Main.MODID, "shaders/core/clouds_post.fsh");

    public final Uniform uAccum;
    public final Uniform uReveal;
    public final Uniform uGamma;

    public PostShader(ResourceManager resMan, Map<String, String> defs) throws IOException {
        super(resMan, VERTEX_SHADER_ID, FRAGMENT_SHADER_ID, defs);

        if(Main.IS_DEV) GL44.glObjectLabel(GL44.GL_PROGRAM, programId, "cloud_post_shader");

        uAccum = getUniform("u_accum", false);
        uReveal = getUniform("u_reveal", false);
        uGamma = getUniform("u_gamma", true);
    }
}
