package Engine.Graphics.Shaders;

import System.FileManagers.ShaderFileManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL30.*;

public class ShaderProgram {
    private final int programID;

    public ShaderProgram(File vertexShaderPath, File fragmentShaderPath) {
        // Load and compile shaders
        int vertexShaderID = loadShader(vertexShaderPath.getAbsolutePath(), GL_VERTEX_SHADER);
        int fragmentShaderID = loadShader(fragmentShaderPath.getAbsolutePath(), GL_FRAGMENT_SHADER);

        // Link the program
        programID = glCreateProgram();
        glAttachShader(programID, vertexShaderID);
        glAttachShader(programID, fragmentShaderID);
        glLinkProgram(programID);
        checkProgramLinkStatus(programID);

        // Cleanup shaders as they are no longer needed after linking
        glDeleteShader(vertexShaderID);
        glDeleteShader(fragmentShaderID);
    }

    private int loadShader(String filePath, int shaderType) {
        String shaderSource = readShaderSource(filePath);
        int shaderID = glCreateShader(shaderType);
        glShaderSource(shaderID, shaderSource);
        glCompileShader(shaderID);
        checkShaderCompileStatus(shaderID);
        return shaderID;
    }

    private String readShaderSource(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return new String(Files.readAllBytes(path));
        } catch (Exception e) {
            throw new RuntimeException("Error reading shader file: " + filePath, e);
        }
    }

    private void checkShaderCompileStatus(int shaderID) {
        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE) {
            String errorMessage = glGetShaderInfoLog(shaderID);
            throw new RuntimeException("Shader compilation failed: " + errorMessage);
        }
    }

    private void checkProgramLinkStatus(int programID) {
        if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
            String errorMessage = glGetProgramInfoLog(programID);
            throw new RuntimeException("Program linking failed: " + errorMessage);
        }
    }

    public void use() {
        glUseProgram(programID);
    }

    public void stop() {
        glUseProgram(0);
    }

    public void setUniform(String name, float value) {
        int location = glGetUniformLocation(programID, name);
        glUniform1f(location, value);
    }

    public void setUniform(String name, int value) {
        int location = glGetUniformLocation(programID, name);
        glUniform1i(location, value);
    }

    public void setUniform(String name, float[] matrix) {
        int location = glGetUniformLocation(programID, name);
        glUniformMatrix4fv(location, false, matrix);
    }

    public void setUniform4fv(String name, Matrix4f matrix4f)
    {
        float arr[] = new float[16];
        matrix4f.get(arr);
        int location = glGetUniformLocation(programID, name);
        glUniformMatrix4fv(location, false, arr);
    }

    public int getProgramID() {
        return programID;
    }

    public void delete() {
        glDeleteProgram(programID);
    }

    public void setUniform3f(String name, Vector3f val) {
        int location = glGetUniformLocation(programID, name);
        glUniform3f(location, val.x, val.y, val.z);
    }

    public static ShaderProgram getStandardShader()
    {
        File frag = ShaderFileManager.getInstance().getFragShader("basicfrag.glsl");
        File vert = ShaderFileManager.getInstance().getVertexShader("basicvert.glsl");

        return new ShaderProgram(vert, frag);
    }
}
