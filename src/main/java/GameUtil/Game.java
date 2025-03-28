package GameUtil;

import Model.Checkers.Board;

public class Game {
    private static Game instance;
    private Board gameBoard;

    // Private constructor to prevent external instantiation
    private Game(int boardSize) {
        this.gameBoard = new Board(boardSize);
    }

    // Method to initialize the instance
    public static void initialize(int boardSize) {
        if (instance == null) {
            instance = new Game(boardSize);
        } else {
            throw new IllegalStateException("Game has already been initialized.");
        }
    }

    // Method to get the singleton instance
    public static Game getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Game has not been initialized. Call initialize(width, height) first.");
        }
        return instance;
    }

    public int getBoardWidth() {
        return gameBoard.getWidth();
    }

    public int getBoardHeight() {
        return gameBoard.getHeight();
    }

    public Board getBoard() {
        return gameBoard;
    }
}

