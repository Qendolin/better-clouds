#version 330

// Geometry attributes
#define SIZE vec3(_SIZE_XZ_, _SIZE_Y_, _SIZE_XZ_)
#define NEAR_VISIBILITY_START 10. + _SIZE_XZ_
#define NEAR_VISIBILITY_END 20. + _SIZE_XZ_
#define FAR_VISIBILITY_EDGE _VISIBILITY_EDGE_

layout(location = 0) in vec3 in_pos;
layout(location = 1) in vec3 in_vert;
layout(location = 2) in vec3 in_normal;

uniform mat4 u_modelViewProjMat;
uniform vec3 u_cloudsOrigin;
uniform float u_cloudsDistance;
uniform sampler2D u_noiseTexture;
uniform vec4 u_skyData;
uniform vec4 u_cloudsBox;
uniform vec4 u_miscOptions;
uniform float u_time;

flat out float pass_opacity;
out vec3 pass_color;

const float pi = 3.14159265359;

void main() {
    vec3 localWorldPosition = in_pos - u_cloudsOrigin;
    float scaleFalloff = mix(1.0, u_miscOptions.x, pow(length(localWorldPosition.xz), 2.) / pow(u_cloudsDistance, 2.));
    vec3 realPos = in_pos;
    realPos.y *= scaleFalloff;

    pass_opacity =
        smoothstep(NEAR_VISIBILITY_START, NEAR_VISIBILITY_END, length(localWorldPosition))
        * smoothstep(u_cloudsDistance, u_cloudsDistance-FAR_VISIBILITY_EDGE,
            length(vec3(localWorldPosition.x, 0., localWorldPosition.z)));

    vec3 worldDirection = normalize(localWorldPosition);
    vec3 sunDirection = vec3(u_skyData.xy, 0);

    float waveScale = texture(u_noiseTexture, (localWorldPosition.xz + u_cloudsBox.xy) / 4000.0 + vec2(u_time / 800f)).r;
    float smallWaves = texture(u_noiseTexture, (localWorldPosition.zx + u_cloudsBox.yx) / 1000.0 + vec2(u_time / 200f)).r * 1.8 - 0.9;
    waveScale = mix(mix(waveScale, 1.0, max(smallWaves, 0.0)), 0.0, max(-smallWaves, 0.0));
    float fDynScale = 1 - smoothstep(0., u_cloudsBox.w / 4f, in_pos.y);
    float dynScale = mix(1.0, waveScale, fDynScale * u_miscOptions.y);
    vec3 scale = SIZE * dynScale * scaleFalloff;

    pass_color.r = 1.;
    pass_color.g = (scale * in_vert + in_pos).y / u_cloudsBox.w;
    pass_color.b = texture(u_noiseTexture, localWorldPosition.xz / 1024.).g;

    gl_Position = u_modelViewProjMat * vec4(scale * in_vert + realPos, 1.0);
}
