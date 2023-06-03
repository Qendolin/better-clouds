package com.qendolin.betterclouds.clouds.shaders;

import com.google.common.collect.ImmutableMap;
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

    public final Uniform uNoiseTexture;
    public final Uniform uMVPMatrix;
    public final Uniform uOriginOffset;
    public final Uniform uBoundingBox;
    public final Uniform uTime;
    public final Uniform uMiscellaneous;


    public CoverageShader(ResourceManager resMan, Map<String, String> defs) throws IOException {
        super(resMan, VERTEX_SHADER_ID, FRAGMENT_SHADER_ID, defs);

        uNoiseTexture = getUniform("u_noise_texture", false);
        uMVPMatrix = getUniform("u_mvp_matrix", false);
        uOriginOffset = getUniform("u_origin_offset", false);
        uTime = getUniform("u_time", false);
        uBoundingBox = getUniform("u_bounding_box", false);
        uMiscellaneous = getUniform("u_miscellaneous", true);
    }

    public static CoverageShader create(ResourceManager manager, float sizeXZ, float sizeY, int edgeFade) throws IOException {
        Map<String, String> defs = ImmutableMap.ofEntries(
            Map.entry(CoverageShader.DEF_SIZE_XZ_KEY, Float.toString(sizeXZ)),
            Map.entry(CoverageShader.DEF_SIZE_Y_KEY, Float.toString(sizeY)),
            Map.entry(CoverageShader.DEF_FADE_EDGE_KEY, Integer.toString(edgeFade))
        );
        return new CoverageShader(manager, defs);
    }
}
