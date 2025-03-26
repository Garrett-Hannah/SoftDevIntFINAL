#version 330 core

layout(location = 0) in vec3 inPosition; // Vertex position
layout(location = 1) in vec3 inNorm;
layout(location = 2) in int[2] inInds;

uniform mat4 modelMatrix; // Transformation matrix

void main() {
    gl_Position = modelMatrix * vec4(inPosition, 1.0);
}
