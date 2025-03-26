#version 330 core

layout(location = 0) in vec3 inPosition; // Vertex position
layout(location = 1) in vec3 inNorm;
layout(location = 2) in int[2] inInds;

uniform mat4 modelMatrix; // Transformation matrix
uniform mat4 viewMatrix; // camera transformation.
uniform mat4 projectionMatrix;

out vec3 outNorm;
out vec3 fragPos;

void main() {
    // Transform vertex position into world space
    vec4 worldPosition = modelMatrix * vec4(inPosition, 1.0);

    // Apply view and projection transformations
    gl_Position = projectionMatrix * viewMatrix * worldPosition;

    // Pass normal and fragment position to fragment shader
    outNorm = inNorm;
    //mat3(transpose(inverse(modelMatrix))) * inNorm; // Transform normal correctly
    fragPos = worldPosition.xyz; // Pass world space position
}