package Engine.Graphics;

import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class glContextWindow {

    private int width;
    private int height;

    long windowID;

    public glContextWindow(Vector2i windowSize)
    {
        // Set error callback for debugging
        GLFW.glfwInit();

        // Create an invisible window
        glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_TRUE);
        long window = GLFW.glfwCreateWindow(800, 800, "Window", MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create hidden OpenGL context.");
        }

        glfwWindowHint(GLFW_DEPTH_BITS, 24);


        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();

        glEnable(GL_CULL_FACE | GL_DEPTH_TEST);
        glCullFace(GL_BACK);
        glDepthFunc(GL_LESS);

        GL30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

        System.out.println("OpenGL Version: " + org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_VERSION));

        windowID = window;
    }

    public void clear()
    {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }


    public void swapBuffers()
    {
        glfwSwapBuffers(windowID);
    }

    public void close()
    {
        glfwDestroyWindow(windowID);  // Destroy the window
        glfwTerminate();
    }

    public long getWindowID() {
        return windowID;
    }
}
