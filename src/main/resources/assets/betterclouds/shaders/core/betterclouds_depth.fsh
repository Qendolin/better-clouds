#version 430

uniform sampler2D u_depth_texture;

layout (depth_any) out float gl_FragDepth;

void main() {
    gl_FragDepth = texelFetch(u_depth_texture, ivec2(gl_FragCoord.xy), 0).r;
}