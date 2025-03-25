import org.junit.Assert;
import org.junit.Test;
import java.io.File;
import Mesh.MeshManager;
import org.lwjgl.assimp.AIScene;

public class TestMeshManager {

    MeshManager MESHMANAGER = MeshManager.getInstance();




    @Test
    public void getResourceTest()
    {
        File objFile = MESHMANAGER.getModelFromResources("chess.obj");

        Assert.assertTrue(objFile.exists());
    }

    File getChessObjDemoFile()
    {
        return MESHMANAGER.getModelFromResources("chess.obj");
    }

    @Test
    public void openResourceTest()
    {
        File objFile = getChessObjDemoFile();

        AIScene scene = MeshManager.loadModel(objFile);

        Assert.assertTrue(scene != null);
    }

    @Test
    public void getVertexDataTest()
    {
        File objFile = MeshManager.getInstance().getModelFromResources("chess.obj");

        AIScene model = MeshManager.loadModel(objFile);

        Assert.assertNotEquals(model.mNumMeshes(), 0);
        System.out.println(model.mNumMeshes() + " meshes in scene.");
    }


}
