package Engine.Input;

import Engine.Graphics.glContextWindow;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFWKeyCallback;

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
        glContextWindow win = new glContextWindow(new Vector2i(800, 800));
        KeyboardInput keyboardInput = new KeyboardInput(win); // Ensure callback is set

        while (!glfwWindowShouldClose(win.getWindowID())) {
            glfwPollEvents();

            if (keys[GLFW_KEY_W]) {
                System.out.println("W key is pressed!");
            }

            if (keys[GLFW_KEY_ESCAPE]) {
                break;
            }

            win.clear();
            win.swapBuffers();
        }

        win.close();
    }
}
