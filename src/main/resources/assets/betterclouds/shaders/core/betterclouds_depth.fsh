#version 130

uniform sampler2D u_depth_texture;

void main() {
    gl_FragDepth = texelFetch(u_depth_texture, ivec2(gl_FragCoord.xy), 0).r;
}