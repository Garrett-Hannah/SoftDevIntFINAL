package System.FileManagers;

import Engine.Graphics.Mesh.Mesh;
import org.lwjgl.assimp.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.lwjgl.assimp.Assimp.*;

public class MeshFileManager {
    private static MeshFileManager instance;
    private ArrayList<Mesh> meshList;

    // Private constructor to prevent instantiation
    private MeshFileManager() {
        meshList = new ArrayList<>();
    }

    // Public method to provide access to the single instance
    public static MeshFileManager getInstance() {
        if (instance == null) {
            synchronized (MeshFileManager.class) {
                if (instance == null) {
                    instance = new MeshFileManager();
                }
            }
        }
        return instance;
    }

    public static AIScene loadModel(File path) {
        AIScene scene = aiImportFile(path.getAbsolutePath(), aiProcess_Triangulate | aiProcess_FlipUVs);
        if (scene == null) {
            throw new RuntimeException("Error loading model: " + aiGetErrorString());
        }
        return scene;
    }

    public File getModelFromResources(String filename) {
        return Paths.get("src", "main", "resources", "Meshes", filename).toFile();
    }
}
