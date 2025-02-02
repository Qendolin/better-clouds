#version 330 core

#extension GL_ARB_separate_shader_objects : enable

layout (location=0) out vec3 out_color;
layout (location=1) out float out_one;

uniform sampler2D u_depth_texture;

void main() {
    out_color = vec3(0.0);
    out_one = 0.0;

    float depth = texelFetch(u_depth_texture, ivec2(gl_FragCoord.xy), 0).r;
    if(min(gl_FragCoord.z, 1.0) > depth) discard;

    out_color.r = 1.0;
    out_color.gb = vec2(1.0);
    out_one = 1.0 / 255.0;
}