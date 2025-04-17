#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D TranslucentSampler;
uniform sampler2D TranslucentDepthSampler;

uniform mat4 InvMat;
uniform float DepthValue;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform int FogShape;

in vec2 texCoord;

out vec4 fragColor;

vec3 blend(vec3 dst, vec4 src) {
	return (dst * (1.0 - src.a)) + src.rgb;
}

vec4 linear_fog(vec4 inColor, float vertexDistance, float fogStart, float fogEnd, vec4 fogColor) {
	if(vertexDistance <= fogStart) return inColor;
	float fogValue = vertexDistance < fogEnd ? smoothstep(fogStart, fogEnd, vertexDistance) : 1.0;
	return vec4(mix(inColor.rgb, fogColor.rgb, fogValue * fogColor.a), inColor.a);
}

void main() {
	vec4 transColor = texture(TranslucentSampler, texCoord);
	vec4 color = texture(DiffuseSampler, texCoord);
	if(transColor.a > 0.0) {
		//Separate water from other translucent blocks by alpha value
		if(transColor.a > 0.9 && transColor.a < 1.0) {
			//Simple fresnel effect to obscure vision in water when the block behind it is far away
			float depth = 1.0 - (1.0 - texture(DiffuseDepthSampler, texCoord).r) * DepthValue;
			if(depth > transColor.a) transColor.a = depth;
		}
		//Convert to world position
		vec4 pos = InvMat * vec4(texCoord * 2.0 - 1.0, texture(TranslucentDepthSampler, texCoord).r * 2.0 - 1.0, 1.0);
		vec3 worldPos = pos.xyz / pos.w;
		float pixelDist;
		if(FogShape == 1) pixelDist = max(abs(worldPos.y), length(worldPos.xz)); //Cylinder
		else pixelDist = length(worldPos); //Sphere
		//Blend colors and apply fog
		fragColor = linear_fog(vec4(blend(color.rgb, transColor), 1.0), pixelDist, FogStart, FogEnd, FogColor);
	}
	else fragColor = vec4(color.rgb, 1.0);
}
