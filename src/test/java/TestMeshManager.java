import java.io.File;

import Mesh.MeshManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.glfw.GLFW;

public class TestMeshManager {

    MeshManager MESHMANAGER = MeshManager.getInstance();

    private static long window;

    @BeforeAll
    public static void setupOpenGL() {
        window = isolateProgramHelper.setUpGenericWindow();
    }




    @Test
    public void getResourceTest() {
        File objFile = MESHMANAGER.getModelFromResources("chess.obj");
        Assertions.assertTrue(objFile.exists());
    }

    @Test
    public void openResourceTest() {
        File objFile = getChessObjDemoFile();

        AIScene scene = MeshManager.loadModel(objFile);

        Assertions.assertTrue(scene != null);
    }

    @Test
    public void getVertexDataTest() {
        File objFile = MeshManager.getInstance().getModelFromResources("chess.obj");
        AIScene model = MeshManager.loadModel(objFile);

        Assertions.assertEquals(model.mNumMeshes(), 1);
        System.out.println(model.mNumMeshes() + " meshes in scene.");
    }


    @AfterAll
    public static void cleanup() {
        // Destroy OpenGL context
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    // Helper method for getting the "chess.obj" file
    File getChessObjDemoFile() {
        return MESHMANAGER.getModelFromResources("chess.obj");
    }
}
