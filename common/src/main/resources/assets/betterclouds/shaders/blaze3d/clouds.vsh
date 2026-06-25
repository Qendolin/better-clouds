#version 330 core

#moj_import < minecraft:dynamictransforms.glsl >
#moj_import < minecraft:projection.glsl >

in vec3 WorldPosition;
in vec3 LocalPosition;

uniform sampler2D NoiseTexture;

layout (std140) uniform CloudVertexData {
    float uSizeXZ, uSizeY;
    float uTime;
    float uOriginOffsetX, uOriginOffsetY, uOriginOffsetZ;
    float uCameraX, uCameraZ;
    float uBlockDistance, uCloudHeightRange, uScaleFalloffMin;
    float uWindEffectFactor, uWindSpeedFactor;
    float uFogStart, uFogEnd;
};

out float fogFade;
out float tintInterp;
out vec3 lightSampleDir;

float calcFogFade(float distance, float fogStart, float fogEnd) {
    #if NEAR_CLOUD_FADE
    float nearFade = clamp(distance / NEAR_FADE_DIST, 0, 1);
    #else
    float nearFade = 1;
    #endif
    float farFade = clamp(1.0 - (distance - fogStart) / (fogEnd - fogStart), 0, 1);
    return nearFade * nearFade * farFade * farFade;
}

void main() {
    // very rough functionality port from betterclouds_coverage.vsh
    vec3 originOffset = vec3(uOriginOffsetX, uOriginOffsetY, uOriginOffsetZ);
    vec3 localWorldPosition = WorldPosition - originOffset;

    // clouds smaller when farther away
    float scaleFalloff = mix(1.0, uScaleFalloffMin, pow(length(localWorldPosition.xz), 2.0) / pow(uBlockDistance, 2.0));
    vec3 cloudPos = WorldPosition;
    cloudPos.y *= scaleFalloff;

    // use noise texture to vary cloud size over position, mix big and small for better realism
    float waveScale = texture(NoiseTexture, (localWorldPosition.xz + vec2(uCameraX, uCameraZ)) / 4000.0 + vec2(uWindSpeedFactor * uTime / 800.0)).r;
    float smallWaves = texture(NoiseTexture, (localWorldPosition.zx + vec2(uCameraZ, uCameraX)) / 1000.0 + vec2(uWindSpeedFactor * uTime / 200.0)).r * 1.8 - 0.9;
    waveScale = mix(mix(waveScale, 1.0, max(smallWaves, 0.0)), 0.0, max(-smallWaves, 0.0));

    // make cloud tops wobble less
    float fDynScale = 1.0 - smoothstep(0.0, uCloudHeightRange / 4.0, WorldPosition.y + 0.5);
    float dynScale = mix(1.0, waveScale, fDynScale * uWindEffectFactor);
    vec3 scale = vec3(uSizeXZ, uSizeY, uSizeXZ) * dynScale * scaleFalloff;

    vec3 pos = scale * LocalPosition + cloudPos;
    vec3 localWorldVertexPos = pos - originOffset;
    fogFade = calcFogFade(length(localWorldVertexPos.xyz), uFogStart, uFogEnd);
    lightSampleDir = localWorldVertexPos;
    tintInterp = clamp((scale.y * LocalPosition.y + WorldPosition.y) / 2, 0, 1);

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1);
}
