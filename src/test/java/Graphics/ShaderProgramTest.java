package Graphics;

import Engine.Graphics.Shaders.ShaderProgram;
import System.FileManagers.ShaderFileManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

public class ShaderProgramTest {

    private static ShaderProgram shaderProgram;
    private static long window;

    @BeforeAll
    static void setupGL()
    {

        window = isolateProgramHelper.setUpGenericWindow();

        File frag = ShaderFileManager.getInstance().getFragShader("basicfrag.glsl");
        File vert = ShaderFileManager.getInstance().getFragShader("basicvert.glsl");

        shaderProgram = new ShaderProgram(vert, frag);

    }

    @Test
    public void getShaderFiles()
    {
        File frag = ShaderFileManager.getInstance().getFragShader("basicfrag.glsl");
        File vert = ShaderFileManager.getInstance().getVertexShader("basicvert.glsl");


        Assertions.assertTrue(frag.exists());
        Assertions.assertTrue(vert.exists());

        shaderProgram = new ShaderProgram(vert, frag);


        Assertions.assertNotEquals(shaderProgram.getProgramID(), 0, "ID is not equal to zero (success)");
    }
}
