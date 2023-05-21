package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.Main;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Map;

public class TestShader0 extends Shader {
    public static final Identifier VERTEX_SHADER_ID = new Identifier(Main.MODID, "shaders/core/clouds_test_0.vsh");
    public static final Identifier FRAGMENT_SHADER_ID = new Identifier(Main.MODID, "shaders/core/clouds_test_0.fsh");

    public final Uniform uDepth;
    public final Uniform uClipPlanes;

    public TestShader0(ResourceManager resMan, Map<String, String> defs) throws IOException {
        super(resMan, VERTEX_SHADER_ID, FRAGMENT_SHADER_ID, defs);

        uDepth = getUniform("u_depth", false);
        uClipPlanes = getUniform("u_clipPlanes", true);
    }
}
