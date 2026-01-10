#version 150

uniform sampler2D DiffuseSampler;
uniform vec3 Color;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);
    fragColor = vec4(color.rgb * Color, color.a);
}