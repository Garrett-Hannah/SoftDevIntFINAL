package Engine.Graphics.Mesh;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45;

import java.nio.FloatBuffer;
import java.util.ArrayList;

public class VAO {

    private int vaoID;

    public VAO(float[] data)
    {
        vaoID = GL45.glGenVertexArrays();



        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(data.length);
        vertexBuffer.put(data).flip();

        GL45.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES,  vertexBuffer);
    }



    public void bind()
    {

    }

    public void unbind()
    {

    }

}
