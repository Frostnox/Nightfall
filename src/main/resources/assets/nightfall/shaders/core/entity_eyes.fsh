#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float linear_fog_fade(float vertexDistance, float fogStart, float fogEnd) {
    if (vertexDistance <= fogStart) {
        return 1.0;
    } else if (vertexDistance >= fogEnd) {
        return 0.0;
    }

    return smoothstep(fogEnd, fogStart, vertexDistance);
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if(color.a < 0.1) discard;
    fragColor = color * vertexColor * ColorModulator * linear_fog_fade(vertexDistance, FogStart, FogEnd);
}
