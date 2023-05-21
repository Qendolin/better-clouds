package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.Main;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Map;

public class TestShader1 extends Shader {
    public static final String DEF_SIZE_X_KEY = "_SCALE_X_";
    public static final String DEF_SIZE_Y_KEY = "_SCALE_Y_";
    public static final String DEF_FADE_EDGE_KEY = "_VISIBILITY_EDGE_";

    public static final Identifier VERTEX_SHADER_ID = new Identifier(Main.MODID, "shaders/core/clouds_test_1.vsh");
    public static final Identifier FRAGMENT_SHADER_ID = new Identifier(Main.MODID, "shaders/core/clouds_test_1.fsh");

    public final Uniform uModelViewProjMat;
    public final Uniform uCloudsOrigin;
    public final Uniform uCloudsDistance;
    public final Uniform uLightTexture;
    public final Uniform uNoise1Texture;
    public final Uniform uNoise2Texture;
    public final Uniform uSkyData;
    public final Uniform uCloudsBox;
    public final Uniform uTime;
    public final Uniform uMiscOptions;


    public TestShader1(ResourceManager resMan, Map<String, String> defs) throws IOException {
        super(resMan, VERTEX_SHADER_ID, FRAGMENT_SHADER_ID, defs);

        uModelViewProjMat = getUniform("u_modelViewProjMat", false);
        uCloudsOrigin = getUniform("u_cloudsOrigin", false);
        uCloudsDistance = getUniform("u_cloudsDistance", true);
        uSkyData = getUniform("u_skyData", true);
        uCloudsBox = getUniform("u_cloudsBox", true);
        uTime = getUniform("u_time", false);
        uMiscOptions = getUniform("u_miscOptions", true);
        uLightTexture = getUniform("u_lightTexture", false);
        uNoise1Texture = getUniform("u_noise1Texture", false);
        uNoise2Texture = getUniform("u_noise2Texture", false);
    }
}
