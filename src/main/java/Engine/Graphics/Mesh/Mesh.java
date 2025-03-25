package Engine.Graphics.Mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Mesh {

    private int vaoId;
    private int vboId;
    private int eboId;
    private int vertexCount;

    public Mesh(float[] vertices, int[] indices) {
        vertexCount = indices.length;

        // Create Vertex Array Object (VAO)
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // Create Vertex Buffer Object (VBO)
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        FloatBuffer vertexBuffer = memAllocFloat(vertices.length);
        vertexBuffer.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        memFree(vertexBuffer);

        // Define vertex attribute pointers
        // Position attribute
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Create Element Buffer Object (EBO)
        eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        IntBuffer indexBuffer = memAllocInt(indices.length);
        indexBuffer.put(indices).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        memFree(indexBuffer);

        // Unbind VAO
        glBindVertexArray(0);
    }

    public void render() {
        // Bind VAO
        glBindVertexArray(vaoId);
        // Draw the mesh
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
        // Unbind VAO
        glBindVertexArray(0);
    }

    public void cleanup() {
        // Disable vertex attribute pointers
        glDisableVertexAttribArray(0);

        // Delete VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vboId);

        // Delete EBO
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glDeleteBuffers(eboId);

        // Delete VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }
}
