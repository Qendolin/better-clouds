package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.Main;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Map;

public class TestShader2 extends Shader {
    public static final Identifier VERTEX_SHADER_ID = new Identifier(Main.MODID, "shaders/core/clouds_test_2.vsh");
    public static final Identifier FRAGMENT_SHADER_ID = new Identifier(Main.MODID, "shaders/core/clouds_test_2.fsh");

    public final Uniform uColor;
    public final Uniform uAccum;
    public final Uniform uDepth;
    public final Uniform uDepthRange;
    public final Uniform uEffectColor;
    public final Uniform uSkyData;
    public final Uniform uGamma;
    public final Uniform uInverseMat;
    public final Uniform uLightTexture;
    public final Uniform uNoise2Texture;

    public TestShader2(ResourceManager resMan, Map<String, String> defs) throws IOException {
        super(resMan, VERTEX_SHADER_ID, FRAGMENT_SHADER_ID, defs);

        uColor = getUniform("u_color", false);
        uAccum = getUniform("u_accum", false);
        uDepth = getUniform("u_depth", false);
        uDepthRange = getUniform("u_depthRange", true);
        uEffectColor = getUniform("u_effectColor", true);
        uSkyData = getUniform("u_skyData", true);
        uGamma = getUniform("u_gamma", true);
        uLightTexture = getUniform("u_lightTexture", false);
        uNoise2Texture = getUniform("u_noise2Texture", false);
        uInverseMat = getUniform("u_inverseMat", false);
    }
}
