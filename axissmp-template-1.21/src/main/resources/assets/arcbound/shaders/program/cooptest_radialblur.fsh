#version 150

uniform sampler2D DiffuseSampler;
uniform float BlurStrength;
uniform float Samples;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 dir    = texCoord - vec2(0.5);
    vec2 step   = dir * (BlurStrength / Samples);
    vec4 color  = vec4(0.0);
    float total = 0.0;

    for (float i = 0.0; i < Samples; i++) {
        float weight = (Samples - i) / Samples; // closer samples weigh more
        color  += texture(DiffuseSampler, texCoord - step * i) * weight;
        total  += weight;
    }

    fragColor = color / total;
}