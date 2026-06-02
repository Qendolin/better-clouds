#version 320 core
#define pi 3.1415926536

#moj_import <minecraft:fog.glsl>

out vec4 fragColor;
in float fogFade;
in vec3 lightSampleDir;

uniform sampler2D LightTexture;

layout (std140) uniform CloudFragData {
// note to self: don't mix floats and vecs, otherwise padding issues may occur
    float opacity, opacityFactor, opacityExponent, brightness;
    float tintRed, tintGreen, tintBlue;
    float sunX, sunY, sunZ, mappedTime;
};

void main() {
    vec3 sunDir = vec3(sunX, sunY, sunZ);
    vec3 fragDir = normalize(lightSampleDir);
    float lightUvX = dot(sunDir, fragDir) * 0.9;

    # if SUN_HALO_ENABLED
    vec3 xzProj = fragDir - sunDir * dot(fragDir, sunDir);
    float projAngle = acos(dot(normalize(xzProj), sunDir));
    float superellipseFalloff = dot(sunDir, fragDir);
    float sphere = dot(sunDir, fragDir);

    // i still have no idea how this formula works but it seems to work fine
    float superellipse = (
    (1.0 + (1.0 / 3.0) * (pow(sin(2.0 * projAngle + pi / 2.0), 2.0))) * (SUN_HALO_SIZE - abs(superellipseFalloff) * SUN_HALO_SIZE) - 1.0
    ) * sign(-superellipseFalloff);

    lightUvX = mix(sphere, superellipse, smoothstep(0.75, 1.0, abs(sphere)));
    #endif

    // i give up trying to figure out how all this works, lets just do a direct port of the shader code
    // (1, 0) to (0.5, 1)
    if (lightUvX > 0.5) lightUvX = (-2.0 * lightUvX + 2.0) * 0.375;
    // (0.5, 0) to (-0.5, 1)
    else if (lightUvX > -0.5) lightUvX = 0.375 + (-1.0 * lightUvX + 0.5) * 0.25;
    // (-0.5, 0) to (-1, 1)
    else lightUvX = 0.625 + (-2.0 * lightUvX - 1.0) * 0.375;

    vec2 lightUv = vec2(lightUvX, mappedTime);

    // prevent sampling the horizontally interpolated vertical edges
    lightUv.x -= (lightUv.x - 0.5) / textureSize(LightTexture, 0).x;

    vec3 color = texture(LightTexture, lightUv).rgb * vec3(tintRed, tintGreen, tintBlue) * brightness;
    float alpha = opacityFactor * opacity * pow(fogFade, opacityExponent);
    fragColor = vec4(color, alpha);
}
