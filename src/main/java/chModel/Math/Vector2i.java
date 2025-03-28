package chModel.Math;

public class Vector2i {
    public int x, y;

    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2i add(Vector2i other) {
        return new Vector2i(this.x + other.x, this.y + other.y);
    }

    public Vector2i subtract(Vector2i other) {
        return new Vector2i(this.x - other.x, this.y - other.y);
    }

    public int dot(Vector2i other) {
        return this.x * other.x + this.y * other.y;
    }

    public int magnitudeSquared() {
        return this.x * this.x + this.y * this.y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
