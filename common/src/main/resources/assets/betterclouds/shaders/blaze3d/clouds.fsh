#version 330 core

out vec4 fragColor;

layout (std140) uniform CloudInfo {
    float ticks;
    float partialTicks;

    float uSizeXZ;
    float uSizeY;

    vec3 tint;
};

void main() {
    fragColor = vec4(tint.rgb, 0.5);
}
