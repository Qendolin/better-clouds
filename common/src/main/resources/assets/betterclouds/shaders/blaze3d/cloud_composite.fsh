#version 330 core
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D CloudAccumulation;
layout(location = 0) out vec4 fragColor;

void main() {
    // Same size and viewport as the destination; preserve the premultiplied RGB.
    vec4 clouds = texelFetch(CloudAccumulation, ivec2(gl_FragCoord.xy), 0);
    if (clouds.a == 0.0) discard;
    fragColor = clouds;
}
