package Engine.Graphics.Mesh;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.assimp.*;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;

public class Mesh {

    private int vaoID;
    private int positionVBO;
    private int normalVBO;
    private int indexVBO;
    private int vertexCount;

    private Vector3f position = new Vector3f(0.f, 0.f, 0.f);

    public Matrix4f modelMatrix = new Matrix4f().identity();

    public enum BUFFER_TYPE {
        POS_VB(0),
        NORMAL_VB(1),
        INDEX_BUFFER(2);


        private final int value;

        // Constructor to associate value with each constant
        BUFFER_TYPE(int value) {
            this.value = value;
        }

        // Getter to retrieve the value associated with each constant
        public int getValue() {
            return value;
        }
    }


    public Mesh(float[] positions, float[] normals, int[] indices) {
        vertexCount = indices.length;

        // Convert the lists to buffers
        FloatBuffer positionBuffer = BufferUtils.createFloatBuffer(positions.length);
        for (float pos : positions) positionBuffer.put(pos);
        positionBuffer.flip();

        FloatBuffer normalBuffer = BufferUtils.createFloatBuffer(normals.length);
        for (float normal : normals) normalBuffer.put(normal);
        normalBuffer.flip();

        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.length);
        for (int index : indices) indexBuffer.put(index);
        indexBuffer.flip();

        // Create and bind the VAO
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Position VBO
        positionVBO = createVBO(positionBuffer, GL_ARRAY_BUFFER);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);  // Position attribute
        glEnableVertexAttribArray(0);

        // Normal VBO
        normalVBO = createVBO(normalBuffer, GL_ARRAY_BUFFER);
        glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 0, 0);  // Normal attribute
        glEnableVertexAttribArray(1);

        // Index VBO
        indexVBO = createVBO(indexBuffer, GL_ELEMENT_ARRAY_BUFFER);

        glBindVertexArray(0);  // Unbind VAO
    }

    private int createVBO(FloatBuffer buffer, int bufferType) {
        int vbo = glGenBuffers();
        glBindBuffer(bufferType, vbo);
        glBufferData(bufferType, buffer, GL_STATIC_DRAW);
        return vbo;
    }

    private int createVBO(IntBuffer buffer, int bufferType) {
        int vbo = glGenBuffers();
        glBindBuffer(bufferType, vbo);
        glBufferData(bufferType, buffer, GL_STATIC_DRAW);
        return vbo;
    }

    public void render() {
        // Bind VAO
        glBindVertexArray(vaoID);
        // Draw the mesh
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
        // Unbind VAO
        glBindVertexArray(0);
    }

    public void cleanup() {
        // Disable vertex attribute pointers
        glDisableVertexAttribArray(0);

        // Delete VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(positionVBO);

        // Delete EBO
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glDeleteBuffers(normalVBO);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(indexVBO);

        // Delete VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoID);
    }

    public static Mesh loadMesh(File file)
    {
        AIScene scene = Assimp.aiImportFile(file.getAbsolutePath(),
                Assimp.aiProcess_JoinIdenticalVertices |
                        Assimp.aiProcess_Triangulate
                        | Assimp.aiProcess_FlipUVs);


        AIMesh aiMesh = AIMesh.create(scene.mMeshes().get(0));

        float[] pos = getPositions(aiMesh);
        float[] norm = getNormals(aiMesh);
        int[] indices = getIndices(aiMesh);

        return new Mesh(pos, norm, indices);
    }

    private static float[] getPositions(AIMesh mesh) {
        AIVector3D.Buffer vertices = mesh.mVertices();
        float[] positions = new float[vertices.remaining() * 3];

        for (int i = 0; i < vertices.remaining(); i++) {
            AIVector3D vertex = vertices.get(i);
            positions[i * 3] = vertex.x();
            positions[i * 3 + 1] = vertex.y();
            positions[i * 3 + 2] = vertex.z();
        }

        return positions;
    }

    private static float[] getNormals(AIMesh mesh) {
        AIVector3D.Buffer normalsBuffer = mesh.mNormals();
        if (normalsBuffer == null) {
            return new float[0]; // No normals provided
        }

        float[] normals = new float[normalsBuffer.remaining() * 3];
        for (int i = 0; i < normalsBuffer.remaining(); i++) {
            AIVector3D normal = normalsBuffer.get(i);
            normals[i * 3] = normal.x();
            normals[i * 3 + 1] = normal.y();
            normals[i * 3 + 2] = normal.z();
        }

        return normals;
    }

    private static int[] getIndices(AIMesh mesh) {
        AIFace.Buffer faces = mesh.mFaces();
        int numIndices = faces.remaining() * 3; // Assumes triangulated faces
        int[] indices = new int[numIndices];

        int index = 0;
        for (int i = 0; i < faces.remaining(); i++) {
            AIFace face = faces.get(i);
            if (face.mNumIndices() != 3) {
                throw new IllegalStateException("Non-triangle face detected!");
            }
            indices[index++] = face.mIndices().get(0);
            indices[index++] = face.mIndices().get(1);
            indices[index++] = face.mIndices().get(2);
        }

        return indices;
    }

    public Vector3f getPosition()
    {
        return position;
    }



}
