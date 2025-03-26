package Engine.Graphics.Mesh;

import org.lwjgl.opengl.GL30;

public class VAO {

    private final int vaoID;
    private final int vboID;


    public VAO(int vboID)
    {
        this.vaoID = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoID);

        this.vboID = vboID;

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboID);

        GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL30.glEnableVertexAttribArray(0);

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    public void bind() {
        GL30.glBindVertexArray(vaoID);
    }

    public void unbind() {
        GL30.glBindVertexArray(0);
    }

    public int getVertexCount() {

    }
}
