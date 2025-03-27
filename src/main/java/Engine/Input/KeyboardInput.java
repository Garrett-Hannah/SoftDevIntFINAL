package Engine.Input;

import Engine.Graphics.Camera;
import Engine.Graphics.Mesh.Mesh;
import Engine.Graphics.Shaders.ShaderProgram;
import Engine.Graphics.glContextWindow;
import System.FileManagers.MeshFileManager;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.io.File;

import static org.lwjgl.glfw.GLFW.*;

public class KeyboardInput {
    private static boolean[] keys = new boolean[GLFW_KEY_LAST];

    private static GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (key >= 0 && key < keys.length) {
                keys[key] = (action != GLFW_RELEASE);
            }
        }
    };

    KeyboardInput(glContextWindow win) {
        glfwSetKeyCallback(win.getWindowID(), keyCallback); // Register key callback
    }

    public static void main(String[] args) {
        glContextWindow win = new glContextWindow(new Vector2i(1600, 1600));
        KeyboardInput keyboardInput = new KeyboardInput(win); // Ensure callback is set

        File horsie = MeshFileManager.getInstance().getModelFromResources("horsie.obj");
        Mesh mesh = Mesh.loadMesh(horsie);

        Camera camera = new Camera(82.5f, 1.0f, 0.001f, 1000.0f);

        ShaderProgram shaderProgram = ShaderProgram.getStandardShader();


        while (!glfwWindowShouldClose(win.getWindowID())) {
            glfwPollEvents();

            if (keys[GLFW_KEY_W]) {
                camera.move(new Vector3f(0.0f, 0.f, -0.01f));
            }


            if (keys[GLFW_KEY_S]) {
                camera.move(new Vector3f(0.0f, 0.f, 0.01f));
            }


            if (keys[GLFW_KEY_A]) {
                camera.move(new Vector3f(-0.010f, 0.f, 0.f));
            }


            if (keys[GLFW_KEY_D]) {
                camera.move(new Vector3f(0.01f, 0.f, 0.0f));
            }


            if (keys[GLFW_KEY_LEFT_SHIFT]) {
                camera.move(new Vector3f(0.0f, -0.01f, 0.f));
            }


            if (keys[GLFW_KEY_SPACE]) {
                camera.move(new Vector3f(0.0f, 0.01f, 0.0f));
            }

            if(keys[GLFW_KEY_MINUS])
            {
                camera.rotate(0.0f, -0.01f, 0.0f);
            }

            if(keys[GLFW_KEY_EQUAL])
            {
                camera.rotate(0.0f, 0.01f, 0.0f);
            }

            if(keys[GLFW_KEY_O])
            {
                camera.rotate(0.01f, 0.0f, 0.0f);
            }

            if(keys[GLFW_KEY_P])
            {
                camera.rotate(-0.01f, 0.0f, 0.0f);
            }

            if (keys[GLFW_KEY_ESCAPE]) {
                break;
            }

            shaderProgram.use();

            shaderProgram.setUniform4fv("modelMatrix", new Matrix4f().identity());
            shaderProgram.setUniform4fv("viewMatrix", camera.getViewMatrix());
            shaderProgram.setUniform4fv("projectionMatrix", camera.getProjectionMatrix());


            mesh.render();
            shaderProgram.stop();


            win.swapBuffers();
            win.clear();
        }

        win.close();
    }
}
