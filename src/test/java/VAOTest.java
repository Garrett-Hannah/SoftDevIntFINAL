import Engine.Graphics.Mesh.VAO;
import Engine.Graphics.Mesh.VBO;
import Engine.Graphics.Shaders.ShaderProgram;
import System.FileManagers.MeshFileManager;
import System.FileManagers.ShaderFileManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lwjgl.opengl.GL30;

import java.io.File;

public class VAOTest {

    static VAO vao;
    static long window;
    static ShaderProgram shaderProgram;

    @BeforeAll
    public static void setupGL()
    {
        window = isolateProgramHelper.setUpGenericWindow();

        MeshFileManager.getInstance().getModelFromResources("chess.obj");



        //pull the vertices VBO from the other test.
        VBO vbo = new VBO(VBOTest.starVertices);

        vao = new VAO(vbo.getVboID());
    }

    @Test
    public void getShaderFiles()
    {
        File frag = ShaderFileManager.getInstance().getFragShader("basic.frag");
        File vert = ShaderFileManager.getInstance().getVertexShader("basic.vert");


        Assertions.assertTrue(frag.exists());
        Assertions.assertTrue(vert.exists());

        shaderProgram = new ShaderProgram(vert, frag);


        Assertions.assertNotEquals(shaderProgram.getProgramID(), 0, "ID is not equal to zero (success)");
    }

    @Test
    public void testRender()
    {
        shaderProgram.use();
        float[] modelMatrix = new float[16];
        shaderProgram.setUniform("modelMatrix", modelMatrix);

        vao.bind();
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, vao.getVertexCount());
        vao.unbind();

        shaderProgram.stop();
    }



    @AfterAll
    public static void close()
    {
        isolateProgramHelper.closeGenericWindow(window);

    }


}
