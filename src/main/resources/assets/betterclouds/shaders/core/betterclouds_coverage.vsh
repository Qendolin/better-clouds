#version 330 core

#extension GL_ARB_separate_shader_objects : enable

// Geometry attributes
#define SIZE vec3(_SIZE_XZ_, _SIZE_Y_, _SIZE_XZ_)
#define NEAR_VISIBILITY_START 10.0 + _SIZE_XZ_
#define NEAR_VISIBILITY_END 20.0 + _SIZE_XZ_
#define FAR_VISIBILITY_EDGE _VISIBILITY_EDGE_

#define POSITIONAL_COLORING _POSITIONAL_COLORING_

#define DISTANT_HORIZONS _DISTANT_HORIZONS_

layout(location = 0) in vec3 in_pos;
layout(location = 1) in vec3 in_vert;
layout(location = 2) in vec3 in_normal;

uniform sampler2D u_noise_texture;
#if DISTANT_HORIZONS
uniform mat4 u_mv_matrix;
uniform mat4 u_mc_p_matrix;
uniform mat4 u_dh_p_matrix;
out float pass_dh_depth;
#else
uniform mat4 u_mvp_matrix;
#endif
// x, y, z offset to the local origin
uniform vec3 u_origin_offset;
// x, z offset to the world origin
// width, height of the bounding box
uniform vec4 u_bounding_box;
// scale falloff minimum, dynamic scale factor, dynamic scale speed
uniform vec3 u_miscellaneous;
uniform float u_time;
// start, end
uniform vec2 u_fog_range;
uniform vec2 u_depth_range;

flat out float pass_opacity;
out vec3 pass_color;


float linear_fog(float distance, float fogStart, float fogEnd) {
    if(distance <= fogStart) return 0.0;
    if(distance > fogEnd) return 1.0;

    return smoothstep(fogStart, fogEnd, distance);
}

float linearize_depth(float depth)
{
    float near = u_depth_range.x;
    float far  = u_depth_range.y;
    return (2.0 * near * far) / (far + near - depth * (far - near));
}

void main() {
    vec3 localWorldPosition = in_pos - u_origin_offset;
    float scaleFalloff = mix(1.0, u_miscellaneous.x, pow(length(localWorldPosition.xz), 2.0) / pow(u_bounding_box.z, 2.0));
    vec3 vertexPos = in_pos;
    vertexPos.y *= scaleFalloff;

    pass_opacity =
        smoothstep(NEAR_VISIBILITY_START, NEAR_VISIBILITY_END, length(localWorldPosition))
        * smoothstep(u_bounding_box.z, u_bounding_box.z-FAR_VISIBILITY_EDGE,
            length(vec3(localWorldPosition.x, 0, localWorldPosition.z)));

    if(u_fog_range.y * 4.0 < u_bounding_box.z-FAR_VISIBILITY_EDGE) {
        pass_opacity *= 1.0 - linear_fog(length(localWorldPosition.xyz), u_fog_range.x, u_fog_range.y);
    }

    vec3 worldDirection = normalize(localWorldPosition);

    float waveScale = texture(u_noise_texture, (localWorldPosition.xz + u_bounding_box.xy) / 4000.0 + vec2(u_miscellaneous.z * u_time / 800.0)).r;
    float smallWaves = texture(u_noise_texture, (localWorldPosition.zx + u_bounding_box.yx) / 1000.0 + vec2(u_miscellaneous.z * u_time / 200.0)).r * 1.8 - 0.9;
    waveScale = mix(mix(waveScale, 1.0, max(smallWaves, 0.0)), 0.0, max(-smallWaves, 0.0));
    float fDynScale = 1.0 - smoothstep(0.0, u_bounding_box.w / 4.0, in_pos.y+0.5);
    float dynScale = mix(1.0, waveScale, fDynScale * u_miscellaneous.y);
    vec3 scale = SIZE * dynScale * scaleFalloff;

#if POSITIONAL_COLORING
    pass_color.g = (scale.y * 0.625 * (in_vert.y+0.375) + in_pos.y) / (u_bounding_box.w);
#else
    pass_color.g = 1.0;
#endif
    pass_color.b = texture(u_noise_texture, localWorldPosition.xz / 1024.0).g;

#if DISTANT_HORIZONS
    vec4 localPos = u_mv_matrix * vec4(scale * in_vert + vertexPos, 1.0);
    gl_Position = u_mc_p_matrix * localPos;
    vec4 dhPos = u_dh_p_matrix * localPos;
    pass_dh_depth = (dhPos.z/dhPos.w) * 0.5 + 0.5;
#else
    gl_Position = u_mvp_matrix * vec4(scale * in_vert + vertexPos, 1.0);
#endif

    // "Fix" for #75
    float depth = linearize_depth(gl_Position.z / gl_Position.w);
    pass_opacity *= 1.0 - smoothstep(u_depth_range.y - FAR_VISIBILITY_EDGE, u_depth_range.y - FAR_VISIBILITY_EDGE / 2.0, depth);
}
