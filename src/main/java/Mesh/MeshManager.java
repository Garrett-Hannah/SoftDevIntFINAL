//package Mesh;
//
//import org.lwjgl.assimp.*;
//
//import java.io.File;
//import java.util.ArrayList;
//
//public class MeshManager {
//
//    private ArrayList<Mesh> meshList;
//
//    MeshManager()
//    {
//
//    }
//
//    public static AIScene loadModel(File path)
//    {
//        AIScene scene = aiImportFile(path.getAbsolutePath(), aiProcess_Triangulate | aiProcess_FlipUVs);
//        if(scene == null)
//        {
//            throw new RuntimeException("Error loading model: " + aiGetErrorString());
//        }
//
//        return scene;
//    }
//}
