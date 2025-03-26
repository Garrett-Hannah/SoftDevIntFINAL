package Engine.Graphics.Mesh;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45;

import java.nio.FloatBuffer;
import java.util.ArrayList;

public class VBO
{
    private final int vboID;

    VBO(ArrayList<Float> verts)
    {
        vboID = GL45.glGenBuffers();
        GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, vboID);

        GL45.glBufferData(GL45.GL_ARRAY_BUFFER, (long) verts.size() * Float.BYTES, GL45.GL_STATIC_DRAW);
    }

    void bind()
    {
        GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, vboID);
    }

    void unbind()
    {
        GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, 0);
    }

    void delete()
    {
        GL45.glDeleteBuffers(vboID);
    }


    public static ArrayList<Float> genGrid(Integer subdivide)
    {
        if(subdivide <= 2) throw new  IllegalArgumentException("Error: Subdivision is not fine enough.");

        ArrayList<Float> grid = new ArrayList<>();

        float incrVals = 1.0f / ((float) subdivide);

        for(float i = 0.0f; i < 1.0f; i+= incrVals)
        {
            for(float j = 0.0f; j < 1.0f; j += incrVals)
            {
                grid.add(i);
                grid.add(j);
                grid.add(0.0f);
            }
        }

        return grid;
    }

}