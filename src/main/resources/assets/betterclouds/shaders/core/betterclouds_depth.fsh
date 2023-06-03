#version _VERSION_

#extension ARB_conservative_depth : enable

#define DEPTH_LAYOUT_QUALIFIER _DEPTH_LAYOUT_QUALIFIER_

uniform sampler2D u_depth_texture;

#if DEPTH_LAYOUT_QUALIFIER
layout (depth_any) out float gl_FragDepth;
#endif

void main() {
    gl_FragDepth = texelFetch(u_depth_texture, ivec2(gl_FragCoord.xy), 0).r;
}