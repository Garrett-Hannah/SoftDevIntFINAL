#version 330 core

layout(location = 0) in vec3 inPosition; // Vertex position

uniform mat4 modelMatrix; // Transformation matrix

void main() {
    gl_Position = modelMatrix * vec4(inPosition, 1.0);
}
