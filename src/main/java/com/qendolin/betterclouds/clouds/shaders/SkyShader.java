package com.qendolin.betterclouds.clouds.shaders;

import com.qendolin.betterclouds.BetterCloudsStatic;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Map;

public class SkyShader extends Shader {
    public static final Identifier VERTEX_SHADER_ID = Identifier.of(BetterCloudsStatic.MODID, "shaders/core/betterclouds_sky.vsh");
    public static final Identifier FRAGMENT_SHADER_ID = Identifier.of(BetterCloudsStatic.MODID, "shaders/core/betterclouds_sky.fsh");;

    public SkyShader(ResourceManager resMan, Map<String, String> defs) throws IOException {
        super(resMan, VERTEX_SHADER_ID, FRAGMENT_SHADER_ID, defs);
    }

    public static SkyShader create(ResourceManager manager) throws IOException {
        return new SkyShader(manager, Map.of());
    }
}
