package Engine.Graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;


public class Camera {
    private Vector3f position;
    private float yaw;   // Rotation around Y-axis
    private float pitch; // Rotation around X-axis
    private float roll;  // Rotation around Z-axis

    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;

    // Constructor for perspective camera
    public Camera(float fov, float aspectRatio, float near, float far) {
        this.position = new Vector3f(0, 0, 0);
        this.yaw = 0.0f;
        this.pitch = 0.0f;
        this.roll = 0.0f;

        this.projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(fov), aspectRatio, near, far);
        this.viewMatrix = new Matrix4f();
        updateViewMatrix();
    }

    // Update the view matrix based on position and rotation
    public void updateViewMatrix() {
        viewMatrix.identity();

        // Apply rotations (yaw, pitch, roll)
        viewMatrix.rotate((float) Math.toRadians(roll), new Vector3f(0, 0, 1))   // Roll
                .rotate((float) Math.toRadians(pitch), new Vector3f(1, 0, 0))  // Pitch
                .rotate((float) Math.toRadians(yaw), new Vector3f(0, 1, 0));   // Yaw

        // Apply translation (inverse position for view matrix)
        viewMatrix.translate(-position.x, -position.y, -position.z);
    }

    // Move the camera by a delta
    public void move(Vector3f delta) {
        position.add(delta);
        updateViewMatrix();
    }

    // Set camera position
    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        updateViewMatrix();
    }

    // Rotate camera by delta yaw, pitch, and roll
    public void rotate(float deltaYaw, float deltaPitch, float deltaRoll) {
        this.yaw += deltaYaw;
        this.pitch += deltaPitch;
        this.roll += deltaRoll;
        updateViewMatrix();
    }

    // Set camera rotation
    public void setRotation(float yaw, float pitch, float roll) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
        updateViewMatrix();
    }

    // Get the projection matrix
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    // Get the view matrix
    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    // Get the camera position
    public Vector3f getPosition() {
        return position;
    }

    // Reset camera to default position and rotation
    public void reset() {
        this.position.set(0, 0, 0);
        this.yaw = 0.0f;
        this.pitch = 0.0f;
        this.roll = 0.0f;
        updateViewMatrix();
    }
}
