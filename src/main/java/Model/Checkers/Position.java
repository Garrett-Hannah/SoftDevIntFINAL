package Model.Checkers;

import GameUtil.Game;
import Model.Math.Vector2i;

import static java.util.Objects.hash;

public class Position {
    private Vector2i position;


    public Position(int x, int y) {
        position = new Vector2i(x, y);
        this.setX(x);
        this.setY(y);
    }

    public Position(Vector2i position)
    {
        this(position.x, position.y);
    }

    public void setX(int x) {
        if (!isValueValidBoardSpot(x)) {
            throw new IllegalArgumentException("Invalid x position: " + x + ". Must be between 1 and " + getBoardHeight());
        }
        this.position.x = x;
    }

    public void setY(int y) {
        if (!isValueValidBoardSpot(y)) {
            throw new IllegalArgumentException("Invalid y position: " + y + ". Must be between 1 and " + getBoardHeight());
        }
        this.position.y = y;
    }

    private boolean isValueValidBoardSpot(int value) {
        return value >= 1 && value <= getBoardHeight();
    }

    private int getBoardHeight() {
        try {
            return Game.getInstance().getBoardHeight();
        } catch (IllegalStateException e) {
            System.err.println("Warning: Game instance not initialized. Using default board height of 8.");
            return 8; // Default size, adjust as needed
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return this.getX() == position.getX() && this.getY() == position.getY();
    }

    @Override
    public int hashCode() {
        return hash(this.getX(), this.getY());
    }

    @Override
    public String toString() {
        // Convert x to letters A-H
        char column = (char) ('@' + this.getX());  // Convert the column index to a letter (A-H)

        // Convert y to numbers 1-8
        int row = this.getY();
        // Return the position as "A1", "B2", ..., "H8"
        return column + Integer.toString(row);
    }

    /**
     *
     * @param dx
     * @param dy
     * @return The Value of the new directions added to the thing.
     */
    public Position getDeltaPosition(int dx, int dy)
    {
        int x = this.getX();
        int y = this.getY();

        return new Position(x + dx, y + dy);
    }

    public Position getDeltaPosition(Vector2i dv)
    {
        return getDeltaPosition(dv.x, dv.y);
    }

    public int getY() {
        return this.position.y;
    }

    public int getX() {
        return this.position.x;
    }
}
