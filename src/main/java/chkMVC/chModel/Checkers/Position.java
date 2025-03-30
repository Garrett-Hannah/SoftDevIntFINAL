package chkMVC.chModel.Checkers;

import chkMVC.chModel.Math.Vector2i;
import java.util.Objects; // Use Objects.hash

public class Position {
    private final Vector2i position; // Made final
    private final int boardWidth;    // Made final
    private final int boardHeight;   // Made final

    /**
     * Creates a new Position.
     * @param x The x-coordinate (column), 1-based.
     * @param y The y-coordinate (row), 1-based.
     * @param boardWidth The width of the board this position belongs to.
     * @param boardHeight The height of the board this position belongs to.
     * @throws IllegalArgumentException if x or y are out of board bounds [1, width/height].
     */
    public Position(int x, int y, int boardWidth, int boardHeight) {
        if (boardWidth <= 0 || boardHeight <= 0) {
            throw new IllegalArgumentException("Board dimensions must be positive.");
        }
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;

        // Validate coordinates against the provided bounds immediately
        if (!isValidCoordinate(x, this.boardWidth)) {
            throw new IllegalArgumentException(String.format(
                    "Invalid x position: %d. Must be between 1 and %d.", x, this.boardWidth));
        }
        if (!isValidCoordinate(y, this.boardHeight)) {
            throw new IllegalArgumentException(String.format(
                    "Invalid y position: %d. Must be between 1 and %d.", y, this.boardHeight));
        }

        this.position = new Vector2i(x, y);
    }

    /**
     * Creates a new Position using Vector2i for coordinates and dimensions.
     * @param positionVector Vector containing 1-based x and y coordinates.
     * @param boardSizeVector Vector containing board width and height.
     * @throws IllegalArgumentException if coordinates are out of bounds.
     */
    public Position(Vector2i positionVector, Vector2i boardSizeVector) {
        this(positionVector.x, positionVector.y, boardSizeVector.x, boardSizeVector.y);
    }

    // Private helper for coordinate validation
    private boolean isValidCoordinate(int value, int maxValue) {
        return value >= 1 && value <= maxValue;
    }

    // No setters needed as fields are final, promoting immutability

    public int getBoardWidth() {
        return this.boardWidth;
    }

    public int getBoardHeight() {
        return this.boardHeight;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position other = (Position) obj;
        // Positions are equal if coordinates are the same.
        // Board dimensions don't strictly need to match for equality,
        // but logically they should represent the same conceptual square.
        return this.getX() == other.getX() && this.getY() == other.getY();
        // Optional stricter check: && this.boardWidth == other.boardWidth && this.boardHeight == other.boardHeight;
    }

    @Override
    public int hashCode() {
        // Hash code depends only on coordinates for map lookups.
        return Objects.hash(this.getX(), this.getY());
        // Optional stricter hash: return Objects.hash(this.getX(), this.getY(), this.boardWidth, this.boardHeight);
    }

    @Override
    public String toString() {
        // Convert x to letters (A, B, ...)
        char column = (char) ('A' + this.getX() - 1); // Adjust for 1-based index
        // Row is just the Y value
        int row = this.getY();
        // Return standard algebraic notation e.g., "A1", "H8"
        return String.format("%c%d", column, row);
    }

    /**
     * Calculates a new Position relative to this one.
     * @param dx Change in x-coordinate.
     * @param dy Change in y-coordinate.
     * @return The new Position.
     * @throws IllegalArgumentException if the resulting coordinates are off the board.
     */
    public Position getDeltaPosition(int dx, int dy) {
        int newX = this.getX() + dx;
        int newY = this.getY() + dy;
        // The constructor will validate the new coordinates against the board dimensions
        return new Position(newX, newY, this.boardWidth, this.boardHeight);
    }

    /**
     * Calculates a new Position relative to this one using a Vector2i delta.
     * @param dv Vector representing the change in x and y.
     * @return The new Position.
     * @throws IllegalArgumentException if the resulting coordinates are off the board.
     */
    public Position getDeltaPosition(Vector2i dv) {
        return getDeltaPosition(dv.x, dv.y);
    }

    public int getY() {
        return this.position.y;
    }

    public int getX() {
        return this.position.x;
    }
}