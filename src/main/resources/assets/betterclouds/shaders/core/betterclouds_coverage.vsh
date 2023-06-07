#version 330

#extension GL_ARB_separate_shader_objects : enable

// Geometry attributes
#define SIZE vec3(_SIZE_XZ_, _SIZE_Y_, _SIZE_XZ_)
#define NEAR_VISIBILITY_START 10.0 + _SIZE_XZ_
#define NEAR_VISIBILITY_END 20.0 + _SIZE_XZ_
#define FAR_VISIBILITY_EDGE _VISIBILITY_EDGE_

layout(location = 0) in vec3 in_pos;
layout(location = 1) in vec3 in_vert;
layout(location = 2) in vec3 in_normal;

uniform sampler2D u_noise_texture;
uniform mat4 u_mvp_matrix;
// x, y, z offset to the local origin
uniform vec3 u_origin_offset;
// x, z offset to the world origin
// width, height of the bounding box
uniform vec4 u_bounding_box;
// scale falloff minimum, dynamic scale factor
uniform vec2 u_miscellaneous;
uniform float u_time;

flat out float pass_opacity;
out vec3 pass_color;

void main() {
    vec3 localWorldPosition = in_pos - u_origin_offset;
    float scaleFalloff = mix(1.0, u_miscellaneous.x, pow(length(localWorldPosition.xz), 2.0) / pow(u_bounding_box.z, 2.0));
    vec3 vertexPos = in_pos;
    vertexPos.y *= scaleFalloff;

    pass_opacity =
        smoothstep(NEAR_VISIBILITY_START, NEAR_VISIBILITY_END, length(localWorldPosition))
        * smoothstep(u_bounding_box.z, u_bounding_box.z-FAR_VISIBILITY_EDGE,
            length(vec3(localWorldPosition.x, 0.0, localWorldPosition.z)));

    vec3 worldDirection = normalize(localWorldPosition);

    float waveScale = texture(u_noise_texture, (localWorldPosition.xz + u_bounding_box.xy) / 4000.0 + vec2(u_time / 800.0)).r;
    float smallWaves = texture(u_noise_texture, (localWorldPosition.zx + u_bounding_box.yx) / 1000.0 + vec2(u_time / 200.0)).r * 1.8 - 0.9;
    waveScale = mix(mix(waveScale, 1.0, max(smallWaves, 0.0)), 0.0, max(-smallWaves, 0.0));
    float fDynScale = 1.0 - smoothstep(0.0, u_bounding_box.w / 4.0, in_pos.y+0.5);
    float dynScale = mix(1.0, waveScale, fDynScale * u_miscellaneous.y);
    vec3 scale = SIZE * dynScale * scaleFalloff;

    pass_color.r = 1.;
    pass_color.g = (scale.y * 0.625 * (in_vert.y+0.375) + in_pos.y) / (u_bounding_box.w);
    pass_color.b = texture(u_noise_texture, localWorldPosition.xz / 1024.0).g;

    gl_Position = u_mvp_matrix * vec4(scale * in_vert + vertexPos, 1.0);
}
