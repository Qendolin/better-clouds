#version 320 core

#moj_import <minecraft:fog.glsl>

out vec4 fragColor;
in float fogFade;

layout (std140) uniform CloudFragData {
    float opacity;
    float opacityFactor;
    float opacityExponent;
    float brightness;
    float tintRed;
    float tintGreen;
    float tintBlue;
};

void main() {
    vec3 color = vec3(tintRed, tintGreen, tintBlue) * brightness;
    fragColor = vec4(color, opacity * fogFade);
}
