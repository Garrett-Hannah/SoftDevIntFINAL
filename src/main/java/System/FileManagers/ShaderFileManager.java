package System.FileManagers;

import java.io.File;
import java.nio.file.Paths;

public class ShaderFileManager {
    private static ShaderFileManager instance;

    // Private constructor to prevent instantiation
    private ShaderFileManager() {
    }

    // Public method to provide access to the single instance
    public static ShaderFileManager getInstance() {
        if (instance == null) {
            synchronized (MeshFileManager.class) {
                if (instance == null) {
                    instance = new ShaderFileManager();
                }
            }
        }
        return instance;
    }

    public File getVertexShader(String filename) {
        return Paths.get("src", "main", "resources", "shaders", filename).toFile();
    }

    public File getFragShader(String filename) {
        return Paths.get("src", "main", "resources", "shaders", filename).toFile();
    }
}
