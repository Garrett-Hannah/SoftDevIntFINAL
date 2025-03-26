import Engine.Graphics.Mesh.Mesh;
import Engine.Graphics.Mesh.VAO;
import Engine.Graphics.Shaders.ShaderProgram;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.GLFW_DEPTH_BITS;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL30.*;

/***
 * <h1>IsolateProgramHelper</h1>
 *
 * really just provides some funcitonality for setting up a quick program.
 * shouldnt be used outside of testing...
 *
 */
public class isolateProgramHelper {

    public static long setUpGenericWindow()
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

        return window;
    }

    public static void closeGenericWindow(long window)
    {
        // Destroy OpenGL context
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    // Function to print out a texture's image data to a PNG file
    public static void saveTextureToFile(int textureID, String filename) {
        // Bind the texture
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureID);

        // Get the width and height of the texture
        IntBuffer width = MemoryUtil.memAllocInt(1);
        IntBuffer height = MemoryUtil.memAllocInt(1);
        IntBuffer channels = MemoryUtil.memAllocInt(1);

        // Fetch the texture size and information
        GL30.glGetTexLevelParameteriv(GL30.GL_TEXTURE_2D, 0, GL30.GL_TEXTURE_WIDTH, width);
        GL30.glGetTexLevelParameteriv(GL30.GL_TEXTURE_2D, 0, GL30.GL_TEXTURE_HEIGHT, height);

        int textureWidth = width.get(0);
        int textureHeight = height.get(0);

        // Allocate a buffer to store the pixel data
        IntBuffer pixels = MemoryUtil.memAllocInt(textureWidth * textureHeight);

        // Read the texture data into the buffer (assuming RGBA format)
        GL30.glGetTexImage(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, pixels);

        // Convert the pixel data into a BufferedImage
        BufferedImage image = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < textureHeight; y++) {
            for (int x = 0; x < textureWidth; x++) {
                int pixelIndex = (textureHeight - y - 1) * textureWidth + x; // Flip vertically
                int pixel = pixels.get(pixelIndex);
                image.setRGB(x, y, pixel);
            }
        }

        // Save the image as a PNG file
        try {
            ImageIO.write(image, "PNG", new File(filename));
            System.out.println("Image saved as " + filename);
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
        }

        // Clean up
        MemoryUtil.memFree(width);
        MemoryUtil.memFree(height);
        MemoryUtil.memFree(channels);
        MemoryUtil.memFree(pixels);
    }

    public static int renderToTexture(Mesh myMesh, ShaderProgram shaderProgram, int width, int height) {
        // Generate and bind a framebuffer
        int framebuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);

        // Create a texture to render to
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);

        // Set texture parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // Attach texture to the framebuffer
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureID, 0);

        // Create and attach a depth buffer
        int depthBuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBuffer);

        // Check if framebuffer is complete
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer is not complete!");
        }

        // Set the viewport to the size of the texture
        glViewport(0, 0, width, height);

        // Clear the framebuffer
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Render the mesh
        shaderProgram.use();
        myMesh.render();
        shaderProgram.stop();

        // Unbind the framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Restore the viewport if needed
        // glViewport(0, 0, originalWidth, originalHeight); // Use original dimensions if required

        // Clean up
        glDeleteFramebuffers(framebuffer);
        glDeleteRenderbuffers(depthBuffer);

        return textureID;
    }

}
