package com.qendolin.betterclouds.clouds;

import com.qendolin.betterclouds.Main;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL44;

import java.io.IOException;
import java.util.Map;

public class MainShader extends Shader {
    public static final String DEF_SIZE_X_KEY = "_SCALE_X_";
    public static final String DEF_SIZE_Y_KEY = "_SCALE_Y_";
    public static final String DEF_FADE_EDGE_KEY = "_VISIBILITY_EDGE_";

    public static final Identifier VERTEX_SHADER_FAST_ID = new Identifier(Main.MODID, "shaders/core/clouds_fast.vsh");
    public static final Identifier VERTEX_SHADER_FANCY_ID = new Identifier(Main.MODID, "shaders/core/clouds_fancy.vsh");
    public static final Identifier FRAGMENT_SHADER_FAST_ID = new Identifier(Main.MODID, "shaders/core/clouds_fast.fsh");
    public static final Identifier FRAGMENT_SHADER_FAST_IRIS_ID = new Identifier(Main.MODID, "shaders/core/clouds_fast-iris.fsh");
    public static final Identifier FRAGMENT_SHADER_FANCY_ID = new Identifier(Main.MODID, "shaders/core/clouds_fancy.fsh");
    public static final Identifier FRAGMENT_SHADER_FANCY_IRIS_ID = new Identifier(Main.MODID, "shaders/core/clouds_fancy-iris.fsh");

    public final Uniform uSkyData;
    public final Uniform uModelViewProjMat;
    public final Uniform uSkyColor;
    public final Uniform uSkyColorOverride;
    public final Uniform uCloudsOrigin;
    public final Uniform uCloudsDistance;
    public final Uniform uCloudsBox;
    public final Uniform uColor;
    public final Uniform uTime;
    public final Uniform uCloudNoise;
    public final Uniform uGradient;
    public final Uniform uGradientPos;
    public final Uniform uDepth;

    public MainShader(ResourceManager resMan, boolean iris, boolean fancy, Map<String, String> defs) throws IOException {
        super(resMan, vertexShaderId(fancy), fragmentShaderId(fancy, iris), defs);

        if(Main.IS_DEV) GL44.glObjectLabel(GL44.GL_PROGRAM, programId, "cloud_main_shader");

        uSkyData = getUniform("u_skyData", true);
        uModelViewProjMat = getUniform("u_modelViewProjMat", false);
        uSkyColor = getUniform("u_skyColor", true);
        uSkyColorOverride = getUniform("u_skyColorOverride", true);
        uCloudsOrigin = getUniform("u_cloudsOrigin", false);
        uCloudsDistance = getUniform("u_cloudsDistance", true);
        uCloudsBox = getUniform("u_cloudsBox", true);
        uColor = getUniform("u_color", true);
        uTime = getUniform("u_time", false);
        uCloudNoise = getUniform("u_cloudNoise", false);
        uGradient = getUniform("u_gradient", false);
        uGradientPos = getUniform("u_gradientPos", true);
        uDepth = getUniform("u_depth", false);
    }

    private static Identifier vertexShaderId(boolean fancy) {
        return fancy ? VERTEX_SHADER_FANCY_ID : VERTEX_SHADER_FAST_ID;
    }

    private static Identifier fragmentShaderId(boolean fancy, boolean iris) {
        if(fancy) {
            return iris ? FRAGMENT_SHADER_FANCY_IRIS_ID : FRAGMENT_SHADER_FANCY_ID;
        } else {
            return iris ? FRAGMENT_SHADER_FAST_IRIS_ID : FRAGMENT_SHADER_FAST_ID;
        }
    }
}
