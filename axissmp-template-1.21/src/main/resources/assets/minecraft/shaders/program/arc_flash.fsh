#version 150

uniform sampler2D DiffuseSampler;
uniform float FlashR;
uniform float FlashG;
uniform float FlashB;
uniform float FlashMode;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 original = texture(DiffuseSampler, texCoord);

    if (FlashMode < 0.5) {
        fragColor = vec4(FlashR, FlashG, FlashB, 1.0);
    } else {
        fragColor = vec4(1.0 - original.rgb, 1.0);
    }
}