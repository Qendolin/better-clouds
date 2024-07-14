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
    public static final String DEF_POSITIONAL_COLORING = "_POSITIONAL_COLORING_";
    public static final String DEF_DISTANT_HORIZONS = "_DISTANT_HORIZONS_";
    public static final String DEF_WORLD_CURVATURE = "_WORLD_CURVATURE_";

    public static final Identifier VERTEX_SHADER_ID = Identifier.of(Main.MODID, "shaders/core/betterclouds_coverage.vsh");
    public static final Identifier FRAGMENT_SHADER_ID = Identifier.of(Main.MODID, "shaders/core/betterclouds_coverage.fsh");

    public final Uniform uDepthTexture;
    public final Uniform uDhDepthTexture;
    public final Uniform uNoiseTexture;
    public final Uniform uMVPMatrix;
    public final Uniform uMVMatrix;
    public final Uniform uMcPMatrix;
    public final Uniform uDhPMatrix;
    public final Uniform uOriginOffset;
    public final Uniform uBoundingBox;
    public final Uniform uTime;
    public final Uniform uMiscellaneous;
    public final Uniform uFogRange;
    public final Uniform uDepthRange;


    public CoverageShader(ResourceManager resMan, Map<String, String> defs) throws IOException {
        super(resMan, VERTEX_SHADER_ID, FRAGMENT_SHADER_ID, defs);

        uDepthTexture = getUniform("u_depth_texture", false);
        uDhDepthTexture = getUniform("u_dh_depth_texture", false);
        uNoiseTexture = getUniform("u_noise_texture", false);
        uMVPMatrix = getUniform("u_mvp_matrix", false);
        uMVMatrix = getUniform("u_mv_matrix", false);
        uMcPMatrix = getUniform("u_mc_p_matrix", false);
        uDhPMatrix = getUniform("u_dh_p_matrix", false);
        uOriginOffset = getUniform("u_origin_offset", false);
        uTime = getUniform("u_time", false);
        uBoundingBox = getUniform("u_bounding_box", false);
        uMiscellaneous = getUniform("u_miscellaneous", true);
        uFogRange = getUniform("u_fog_range", true);
        uDepthRange = getUniform("u_depth_range", true);
    }

    public static CoverageShader create(ResourceManager manager, float sizeXZ, float sizeY, int edgeFade, boolean stencilFallback, boolean dhCompat, int worldCurvatureSize) throws IOException {
        Map<String, String> defs = ImmutableMap.ofEntries(
            Map.entry(CoverageShader.DEF_SIZE_XZ_KEY, Float.toString(sizeXZ)),
            Map.entry(CoverageShader.DEF_SIZE_Y_KEY, Float.toString(sizeY)),
            Map.entry(CoverageShader.DEF_FADE_EDGE_KEY, Integer.toString(edgeFade)),
            Map.entry(CoverageShader.DEF_POSITIONAL_COLORING, stencilFallback ? "0" : "1"),
            Map.entry(CoverageShader.DEF_DISTANT_HORIZONS, dhCompat ? "1" : "0"),
            Map.entry(CoverageShader.DEF_WORLD_CURVATURE, Integer.toString(worldCurvatureSize))
        );
        return new CoverageShader(manager, defs);
    }
}
