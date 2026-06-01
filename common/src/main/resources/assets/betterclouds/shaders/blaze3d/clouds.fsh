#version 330 core

#moj_import <minecraft:fog.glsl>

out vec4 fragColor;

layout (std140) uniform CloudFragData {
    float opacity, opacityFactor, opacityExponent;
    vec3 tint;
};

void main() {
    fragColor = vec4(tint.rgb, opacity);
}
