#version 150

uniform sampler2D Sampler0; //Texture atlas
uniform sampler2D Sampler6; //Main depth

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform vec2 ScreenSize;
uniform float LineWidth; //Depth value

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

vec4 linear_fog(vec4 inColor, float vertexDistance, float fogStart, float fogEnd, vec4 fogColor) {
    if(vertexDistance <= fogStart) return inColor;
    float fogValue = vertexDistance < fogEnd ? smoothstep(fogStart, fogEnd, vertexDistance) : 1.0;
    return vec4(mix(inColor.rgb, fogColor.rgb, fogValue * fogColor.a), inColor.a);
}

void main() {
    vec4 transColor = texture(Sampler0, texCoord0);
    //Separate water from other translucent blocks by alpha value
    if(transColor.a > 0.9 && transColor.a < 1.0) {
        //Simple fresnel effect to obscure vision in water when the block behind it is far away
        float depth = 1.0 - (1.0 - texture(Sampler6, gl_FragCoord.xy / ScreenSize).r) * LineWidth;
        if(depth > transColor.a) transColor.a = depth;
    }
    fragColor = linear_fog(transColor * vertexColor * ColorModulator, vertexDistance, FogStart, FogEnd, FogColor);
}
