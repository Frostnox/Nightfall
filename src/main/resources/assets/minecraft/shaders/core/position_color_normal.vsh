#version 150

in vec3 Position;
in vec4 Color;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out float vertexDistance;
out vec4 vertexColor;
out vec4 normal;

float cylindrical_distance(mat4 modelViewMat, vec3 pos) {
    float distXZ = length((modelViewMat * vec4(pos.x, 0.0, pos.z, 1.0)).xyz);
    float distY = length((modelViewMat * vec4(0.0, pos.y, 0.0, 1.0)).xyz);
    return max(distXZ, distY);
}

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vertexDistance = cylindrical_distance(ModelViewMat, Position);
    vertexColor = Color;
    normal = ProjMat * ModelViewMat * vec4(Normal, 0.0);
}
