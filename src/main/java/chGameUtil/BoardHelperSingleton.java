package chGameUtil;

import chModel.Checkers.Board;

public class BoardHelperSingleton {
    private static BoardHelperSingleton instance;
    private Board gameBoard;

    // Private constructor to prevent external instantiation
    private BoardHelperSingleton(int boardSize) {
        this.gameBoard = new Board(boardSize);
    }

    // Method to initialize the instance
    public static void initialize(int boardSize) {
        if (instance == null) {
            instance = new BoardHelperSingleton(boardSize);
        } else {
            throw new IllegalStateException("Game has already been initialized.");
        }
    }

    // Method to get the singleton instance
    public static BoardHelperSingleton getInstance() {
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


    //CLose out the board and reset the instance to nulllllllllllllllllllllll
    public void closeBoard()
    {
        if(gameBoard != null)
        {
            gameBoard.clearBoard();
            gameBoard = null;
        }

        instance = null;
    }
}

