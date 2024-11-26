#version 330 core

#extension GL_ARB_separate_shader_objects : enable

// Geometry attributes
#define SIZE vec3(_SIZE_XZ_, _SIZE_Y_, _SIZE_XZ_)
#define NEAR_VISIBILITY_START 10.0 + _SIZE_XZ_
#define NEAR_VISIBILITY_END 20.0 + _SIZE_XZ_

#define POSITIONAL_COLORING _POSITIONAL_COLORING_
#define WORLD_CURVATURE _WORLD_CURVATURE_

#define DISTANT_HORIZONS _DISTANT_HORIZONS_

layout(location = 0) in vec3 in_pos; // instanced per cloud cube
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

flat out float pass_opacity;
out vec3 pass_color;

float linearFogFade(float distance, float fog_start, float fog_end) {
    if (distance <= fog_start) {
        return 1.0;
    } else if (distance >= fog_end) {
        return 0.0;
    }

    return smoothstep(fog_end, fog_start, distance);
}

void main() {
    vec3 localWorldPosition = in_pos - u_origin_offset; // in world space but anchored to the camera
    float scaleFalloff = mix(1.0, u_miscellaneous.x, pow(length(localWorldPosition.xz), 2.0) / pow(u_bounding_box.z, 2.0));
    vec3 cloudPos = in_pos; // in world space but anchored to the chunk grid
    cloudPos.y *= scaleFalloff;

    pass_opacity = smoothstep(NEAR_VISIBILITY_START, NEAR_VISIBILITY_END, length(localWorldPosition));

    vec3 worldDirection = normalize(localWorldPosition);

    float waveScale = texture(u_noise_texture, (localWorldPosition.xz + u_bounding_box.xy) / 4000.0 + vec2(u_miscellaneous.z * u_time / 800.0)).r;
    float smallWaves = texture(u_noise_texture, (localWorldPosition.zx + u_bounding_box.yx) / 1000.0 + vec2(u_miscellaneous.z * u_time / 200.0)).r * 1.8 - 0.9;
    waveScale = mix(mix(waveScale, 1.0, max(smallWaves, 0.0)), 0.0, max(-smallWaves, 0.0));
    float fDynScale = 1.0 - smoothstep(0.0, u_bounding_box.w / 4.0, in_pos.y+0.5);
    float dynScale = mix(1.0, waveScale, fDynScale * u_miscellaneous.y);
    vec3 scale = SIZE * dynScale * scaleFalloff;

    vec3 vertexPos = scale * in_vert + cloudPos;
    vec3 localWorldVertexPos = vertexPos - u_origin_offset;

    // Due to the limited max depth this can sometimes result in issues but they're barely visible
    pass_color.r = linearFogFade(length(localWorldVertexPos.xz), u_fog_range.x, u_fog_range.y);
    pass_color.r *= linearFogFade(abs(localWorldVertexPos.y), u_fog_range.y-16, u_fog_range.y);

#if POSITIONAL_COLORING
    pass_color.g = (scale.y * 0.625 * (in_vert.y+0.375) + in_pos.y) / (u_bounding_box.w);
#else
    pass_color.g = 1.0;
#endif
    pass_color.b = texture(u_noise_texture, localWorldPosition.xz / 1024.0).g;


#if WORLD_CURVATURE != 0
    vertexPos.y -= dot(localWorldVertexPos, localWorldVertexPos) / WORLD_CURVATURE;
#endif

#if DISTANT_HORIZONS
    vec4 localPos = u_mv_matrix * vec4(vertexPos, 1.0);
    gl_Position = u_mc_p_matrix * localPos;
    vec4 dhPos = u_dh_p_matrix * localPos;
    pass_dh_depth = (dhPos.z/dhPos.w) * 0.5 + 0.5;
#else
    gl_Position = u_mvp_matrix * vec4(vertexPos, 1.0);
#endif
}
