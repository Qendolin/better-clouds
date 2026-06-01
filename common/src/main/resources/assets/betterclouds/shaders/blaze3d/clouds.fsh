#version 320 core

#moj_import <minecraft:fog.glsl>

out vec4 fragColor;
in float fogFade;
in vec3 lightSampleDir;

uniform sampler2D LightTexture;

layout (std140) uniform CloudFragData {
    float opacity, opacityFactor, opacityExponent, brightness;
    float tintRed, tintGreen, tintBlue;
    float sunX, sunY, sunZ, mappedTime;
};

void main() {
    vec3 sunDir = vec3(sunX, sunY, sunZ);
    vec3 fragDir = normalize(lightSampleDir);
    float lightUvX = dot(sunDir, fragDir) * 0.9;

    if (lightUvX > 0.5) lightUvX = (-2.0 * lightUvX + 2.0) * 0.375;
    else if (lightUvX > -0.5) lightUvX = 0.375 + (-1.0 * lightUvX + 0.5) * 0.25;
    else lightUvX = 0.625 + (-2.0 * lightUvX - 1.0) * 0.375;

    vec2 lightUv = vec2(lightUvX, mappedTime);
    lightUv.x -= (lightUv.x - 0.5) / textureSize(LightTexture, 0).x;
    vec3 color = texture(LightTexture, lightUv).rgb * vec3(tintRed, tintGreen, tintBlue) * brightness;
    float alpha = opacityFactor * opacity * pow(fogFade, opacityExponent);
    fragColor = vec4(color, alpha);
}
