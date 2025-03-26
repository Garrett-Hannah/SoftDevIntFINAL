#version 330 core

out vec4 FragColor; // Output color

in vec3 outNorm;

void main() {
    vec3 fragColor = outNorm;
    FragColor = vec4(outNorm, 1.0); // Red color
}
