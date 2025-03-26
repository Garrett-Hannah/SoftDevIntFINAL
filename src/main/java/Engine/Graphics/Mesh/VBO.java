package Engine.Graphics.Mesh;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;

public class VBO
{
    private final int vboID;
    private final int vertexCount;

    public VBO(float[] data)
    {
        vboID = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboID);



        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data).flip();

        vertexCount = data.length / 3;

        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, buffer, GL30.GL_STATIC_DRAW);
    }

    // Method to bind the VBO to OpenGL context
    public void bind() {
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboID);
    }

    // Method to unbind the VBO
    public void unbind() {
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
    }

    // Method to delete the VBO (cleanup)
    public void delete() {
        GL30.glDeleteBuffers(vboID);
    }

    // Getter for VBO ID (useful for debugging)
    public int getVboID() {
        return vboID;
    }

    public int getBufferSize()
    {
        return GL30.glGetBufferParameteri(GL30.GL_ARRAY_BUFFER, GL30.GL_BUFFER_SIZE);
    }

}