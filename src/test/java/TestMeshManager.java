import java.io.File;

import Engine.Graphics.Mesh.Mesh;
import System.FileManagers.MeshFileManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.glfw.GLFW;

public class TestMeshManager {

    MeshFileManager MESHMANAGER = MeshFileManager.getInstance();

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

        AIScene scene = MeshFileManager.loadModel(objFile);

        Assertions.assertTrue(scene != null);
    }

    @Test
    public void getVertexDataTest() {
        File objFile = MeshFileManager.getInstance().getModelFromResources("chess.obj");
        AIScene model = MeshFileManager.loadModel(objFile);

        Assertions.assertEquals(model.mNumMeshes(), 1);
        System.out.println(model.mNumMeshes() + " meshes in scene.");
    }

    @Test
    public void getMeshInfo()
    {
        File objFile = MeshFileManager.getInstance().getModelFromResources("chess.obj");
        AIScene scene = MeshFileManager.loadModel(objFile);




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
