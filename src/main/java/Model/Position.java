package Model;

import GameUtil.Game;

import static java.util.Objects.hash;

public class Position {
    private int x;
    private int y;

    public Position(int x, int y) {
        this.setX(x);
        this.setY(y);
    }

    public void setX(int x) {
        if (!isValueValidBoardSpot(x)) {
            throw new IllegalArgumentException("Invalid x position: " + x + ". Must be between 1 and " + getBoardHeight());
        }
        this.x = x;
    }

    public void setY(int y) {
        if (!isValueValidBoardSpot(y)) {
            throw new IllegalArgumentException("Invalid y position: " + y + ". Must be between 1 and " + getBoardHeight());
        }
        this.y = y;
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
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return hash(x, y);
    }

    @Override
    public String toString() {
        // Convert x to letters A-H
        char column = (char) ('@' + x);  // Convert the column index to a letter (A-H)

        // Convert y to numbers 1-8
        int row = y;
        // Return the position as "A1", "B2", ..., "H8"
        return column + Integer.toString(row);
    }

    public Position getDeltaPosition(int dx, int dy)
    {
        int x = this.getX();
        int y = this.getY();

        return new Position(x + dx, y + dy);
    }

    public int getY() {
        return this.y;
    }

    public int getX() {
        return this.x;
    }
}
