#version 330 core

#moj_import <minecraft:fog.glsl>

out vec4 fragColor;
in float fogFade;

layout (std140) uniform CloudFragData {
    float opacity;
    float opacityFactor;
    float opacityExponent;
    float tintRed;
    float tintGreen;
    float tintBlue;
};

void main() {
    fragColor = vec4(tintRed, tintGreen, tintBlue, opacity * fogFade);
}
