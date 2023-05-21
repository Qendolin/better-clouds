#version 440 core
//#version 150 core

#define GRADIENT_COLOR _GRADIENT_COLOR_

flat in vec4 pass_cloudColor;
// FIXME: unused
flat in vec3 pass_cloudPosition;
in vec3 pass_vert;

//out vec4 fragColor;
layout (location = 0) out vec4 accum;
layout (location = 1) out float reveal;

uniform vec4 u_cloudsBox;
uniform sampler2D u_depth;

void main() {
//    if(gl_FragCoord.z > texelFetch(u_depth, ivec2(gl_FragCoord.xy)*2, 0).r) discard;
    vec4 fragColor = pass_cloudColor;
#if GRADIENT_COLOR
    // FIXME: looks like shit with small height range
    float height = smoothstep(0, u_cloudsBox.w/2., pass_vert.y+u_cloudsBox.z+4.);
    vec3 gradientColor = mix(vec3(0.8, 0.8, 0.85), vec3(1), (1.0-pow(1.0-height, 2.0)));
    fragColor.rgb *= gradientColor;
#endif

    fragColor.a = pass_cloudColor.a;

    // TODO: real plane depths
    float depth = (2.0 * 0.05) / (1024.0 + 0.05 - gl_FragCoord.z * (1024.0 - 0.05));
    float weight = clamp(
            fragColor.a *
            pow(1.0 - depth, 3.0),
        1e-2,
        3e3
    );

    // store pixel color accumulation
    accum = vec4(fragColor.rgb * fragColor.a, fragColor.a) * weight;

    // store pixel revealage threshold
    reveal = fragColor.a;
}
