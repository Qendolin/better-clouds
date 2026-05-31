#version 330 core

in vec3 WorldPosition;
in vec3 LocalPosition;

layout (std140) uniform CloudInfo {
    float ticks;
    float partialTicks;

    float uSizeXZ;
    float uSizeY;

    vec3 tint;
};

void main() {
    gl_Position = vec4(WorldPosition + LocalPosition * vec3(uSizeXZ, uSizeY, uSizeXZ), 1);
}
