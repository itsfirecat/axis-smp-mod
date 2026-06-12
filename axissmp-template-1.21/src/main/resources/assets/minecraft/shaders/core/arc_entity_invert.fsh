#version 150

uniform sampler2D Sampler0;

in vec2 texCoord0;
out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) discard;
    fragColor = vec4(1.0 - color.r, 1.0 - color.g, 1.0 - color.b, 1.0);
}