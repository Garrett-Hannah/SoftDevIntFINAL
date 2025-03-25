import Engine.Graphics.Mesh.VBO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;

public class VBOTest {

    private static VBO vbo;
    private static long window;

    final static float[] starVertices = new float[] {
            // Outer points of the star
            0.0f,  1.0f, 0.0f,   // Top point
            0.2f,  0.3f, 0.0f,   // Right-upper point
            1.0f,  0.0f, 0.0f,   // Right point
            0.2f, -0.3f, 0.0f,   // Right-lower point
            0.0f, -1.0f, 0.0f,   // Bottom point
            -0.2f, -0.3f, 0.0f,  // Left-lower point
            -1.0f,  0.0f, 0.0f,  // Left point
            -0.2f,  0.3f, 0.0f,  // Left-upper point
            0.0f,  1.0f, 0.0f    // Top point (close the loop)
    };

    @BeforeAll
    static void setupProgram()
    {
        window = isolateProgramHelper.setUpGenericWindow();
    }

    @Test
    void createVBO()
    {
        // Define the star's vertices (2D star with 5 points)
        vbo = new VBO(starVertices);

        System.out.println(vbo.getVboID() + " <- vertex buffer.");

        Assertions.assertEquals(vbo.getVboID(), 1, " Vertex Buffer Not Created!");
    }

    @Test
    void testBufferSize()
    {
        int bufferSize = vbo.getBufferSize();


        Assertions.assertEquals(starVertices.length * Float.BYTES, bufferSize, "Buffer Size mismatch!");

        System.out.println("Buffer Size is expected size.");
    }



    @AfterAll
    static void closeOpenGl()
    {
        isolateProgramHelper.closeGenericWindow(window);
    }

}
