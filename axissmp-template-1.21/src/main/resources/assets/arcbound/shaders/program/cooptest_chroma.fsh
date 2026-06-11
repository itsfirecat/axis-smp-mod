#version 150

uniform sampler2D DiffuseSampler;
uniform float ChromaOffset;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main() {
    vec2 dir  = texCoord - vec2(0.5);
    float dist = length(dir);
    // Edge-based split: stronger at corners, zero at center
    vec2 offset = (dist > 0.001)
        ? normalize(dir) * ChromaOffset * dist * 2.0
        : vec2(0.0);

    float r = texture(DiffuseSampler, texCoord + offset).r;
    float g = texture(DiffuseSampler, texCoord).g;
    float b = texture(DiffuseSampler, texCoord - offset).b;

    fragColor = vec4(r, g, b, 1.0);
}