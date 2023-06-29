#version 330 core

#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec2 in_vert;

void main() {
    gl_Position = vec4(in_vert, 0.0, 1.0);
}
