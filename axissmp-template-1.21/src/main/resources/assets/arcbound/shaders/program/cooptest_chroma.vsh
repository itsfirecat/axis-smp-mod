#version 150

in vec4 Position;
in vec2 UV;

uniform mat4 ProjMat;
uniform vec2 OutSize;

out vec2 texCoord;
out vec2 oneTexel;

void main() {
    gl_Position = ProjMat * vec4(Position.xy, 0.0, 1.0);
    texCoord = UV;
    oneTexel = 1.0 / OutSize;
}