#version 330 core

#moj_import <minecraft:fog.glsl>

out vec4 fragColor;

layout (std140) uniform CloudVertexData {
    float ticks;
    float partialTicks;

    float uSizeXZ;
    float uSizeY;
};

void main() {
    fragColor = vec4(tint.rgb, 0.2);
}
