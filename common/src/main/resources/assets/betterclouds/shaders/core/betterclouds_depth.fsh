#version 330 core

uniform sampler2D u_depth_texture;

layout (location=0) out vec4 out_color;

void main() {
    out_color = vec4(0.0); // write color, just for safety
    gl_FragDepth = texelFetch(u_depth_texture, ivec2(gl_FragCoord.xy), 0).r;
}