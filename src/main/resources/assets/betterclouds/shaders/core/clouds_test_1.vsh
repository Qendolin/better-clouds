#version 330

// Geometry attributes
// TODO: rename SCALE to SIZE
#define SCALE vec3(_SCALE_X_, _SCALE_Y_, _SCALE_X_)
#define NEAR_VISIBILITY_START 10. + SCALE.x/2.
#define NEAR_VISIBILITY_END 20. + SCALE.x/2.
#define FAR_VISIBILITY_EDGE _VISIBILITY_EDGE_

layout(location = 0) in vec3 in_pos;
layout(location = 1) in vec3 in_vert;
layout(location = 2) in vec3 in_normal;

uniform mat4 u_modelViewProjMat;
uniform vec3 u_cloudsOrigin;
uniform float u_cloudsDistance;
uniform sampler2D u_lightTexture;
uniform sampler2D u_noise1Texture;
uniform sampler2D u_noise2Texture;
uniform vec4 u_skyData;
uniform vec4 u_cloudsBox;
uniform vec4 u_miscOptions;
uniform float u_time;

flat out float pass_opacity;
out vec3 pass_color;

const float pi = 3.14159265359;


void main() {
    // TODO: rename worldPosition because it's clearly not in world space
    vec3 worldPosition = in_pos - u_cloudsOrigin;
    float scaleFalloff = mix(1.0, u_miscOptions.x, pow(length(worldPosition.xz), 2.) / pow(u_cloudsDistance, 2.));
    vec3 realPos = in_pos;
    realPos.y *= scaleFalloff;

    // FIXME: for large cloud sizes, this is is too short
    pass_opacity =
        smoothstep(NEAR_VISIBILITY_START, NEAR_VISIBILITY_END, length(worldPosition))
        * smoothstep(u_cloudsDistance, u_cloudsDistance-FAR_VISIBILITY_EDGE,
            length(vec3(worldPosition.x, 0., worldPosition.z)));

    vec3 worldDirection = normalize(worldPosition);
    vec3 sunDirection = vec3(u_skyData.xy, 0);

    float lightUVx = dot(normalize(worldDirection), sunDirection);
    // (1, 0) to (0.5, 1)
    if(lightUVx > 0.5) lightUVx = (-2 * lightUVx + 2) * 0.375;
    // (0.5, 0) to (-0.5, 1)
    else if(lightUVx > -0.5) lightUVx = 0.375 + (-1 * lightUVx + 0.5) * 0.25;
    // (-0.5, 0) to (-1, 1)
    else lightUVx = 0.625 + (-2 * lightUVx - 1) * 0.375;

    vec2 lightUV = vec2(lightUVx, u_skyData.z);
    lightUV -= (lightUV - 0.5) / textureSize(u_lightTexture, 0);
    pass_color = texture(u_lightTexture, lightUV).rgb;

    // TODO: Side coloring
    // I might not be able to achieve that

    float viewAngle = -dot(in_normal, normalize(worldPosition));

    float dotX = abs(dot(in_normal, vec3(1, 0, 0)));
    float dotY = abs(dot(in_normal, vec3(0, 1, 0)));
    float dotZ = abs(dot(in_normal, vec3(0, 0, 1)));

    float brightness = dotX*0.5 + dotY + dotZ*0.5;
//    float brightness = dotX + dotY * 0.8 + dotZ;
    brightness = mix(1.0, brightness, viewAngle);
    pass_color.r = pow(viewAngle, 5.);
//    pass_color.r = brightness;
    pass_color.r = (in_pos.y-4.) * 2. / u_cloudsBox.w;

    pass_color.r = max(pass_color.r, 1./255.);

    float waveScale = texture(u_noise1Texture, (worldPosition.xz + u_cloudsBox.xy) / 4000.0 + vec2(u_time / 800f)).x;
    float smallWaves = texture(u_noise1Texture, (worldPosition.zx + u_cloudsBox.yx) / 1000.0 + vec2(u_time / 200f)).x * 1.8 - 0.9;
    waveScale = mix(mix(waveScale, 1.0, max(smallWaves, 0.0)), 0.0, max(-smallWaves, 0.0));
    //    waveScale = (waveScale - 0.25) * 2.;
    float fDynScale = 1 - smoothstep(0., u_cloudsBox.w / 4f, in_pos.y);
    float dynScale = mix(1.0, waveScale, fDynScale * u_miscOptions.y);
//    dynScale *= 1. - length(worldPosition.xz) / (2. * u_cloudsDistance);
    vec3 scale = SCALE * dynScale * scaleFalloff;

    pass_color.g = (scale * in_vert + in_pos).y / u_cloudsBox.w;

//    vec4 projCloudPos = u_modelViewProjMat * vec4(realPos - SCALE * in_vert, 1.0);
//    pass_color.b = projCloudPos.z / projCloudPos.w * 0.5 + 0.5;

    pass_color.b = texture(u_noise2Texture, worldPosition.xz / 1024.).r;

    gl_Position = u_modelViewProjMat * vec4(scale * in_vert + realPos, 1.0);
}
