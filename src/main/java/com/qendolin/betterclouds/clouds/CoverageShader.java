package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.Main;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Map;

public class CoverageShader extends Shader {
    public static final String DEF_SIZE_XZ_KEY = "_SIZE_XZ_";
    public static final String DEF_SIZE_Y_KEY = "_SIZE_Y_";
    public static final String DEF_FADE_EDGE_KEY = "_VISIBILITY_EDGE_";

    public static final Identifier VERTEX_SHADER_ID = new Identifier(Main.MODID, "shaders/core/betterclouds_coverage.vsh");
    public static final Identifier FRAGMENT_SHADER_ID = new Identifier(Main.MODID, "shaders/core/betterclouds_coverage.fsh");

    public final Uniform uModelViewProjMat;
    public final Uniform uCloudsOrigin;
    public final Uniform uCloudsDistance;
    public final Uniform uNoiseTexture;
    public final Uniform uSkyData;
    public final Uniform uCloudsBox;
    public final Uniform uTime;
    public final Uniform uMiscOptions;


    public CoverageShader(ResourceManager resMan, Map<String, String> defs) throws IOException {
        super(resMan, VERTEX_SHADER_ID, FRAGMENT_SHADER_ID, defs);

        uModelViewProjMat = getUniform("u_modelViewProjMat", false);
        uCloudsOrigin = getUniform("u_cloudsOrigin", false);
        uCloudsDistance = getUniform("u_cloudsDistance", true);
        uSkyData = getUniform("u_skyData", true);
        uCloudsBox = getUniform("u_cloudsBox", true);
        uTime = getUniform("u_time", false);
        uMiscOptions = getUniform("u_miscOptions", true);
        uNoiseTexture = getUniform("u_noiseTexture", false);
    }
}
