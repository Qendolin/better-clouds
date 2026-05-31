#version 330 core

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 WorldPosition;
in vec3 LocalPosition;

layout (std140) uniform CloudData {
    float ticks;
    float partialTicks;

    float uSizeXZ;
    float uSizeY;

    vec3 tint;
};

void main() {
    vec3 pos = WorldPosition + LocalPosition * vec3(uSizeXZ, uSizeY, uSizeXZ);
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1);
}
