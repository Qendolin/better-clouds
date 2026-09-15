#version 330 core

uniform sampler2D CloudAccumulation;
out vec4 fragColor;

void main() {
    // Same size and viewport as the destination; preserve the premultiplied RGB.
    vec4 clouds = texelFetch(CloudAccumulation, ivec2(gl_FragCoord.xy), 0);
    if (clouds.a == 0.0) discard;
    fragColor = clouds;
}
