package chGameUtil;

import chModel.Checkers.BoardModel;
import chModel.Checkers.Position;

public class BoardHelperSingleton {
    private static BoardHelperSingleton instance;
    private BoardModel gameBoardModel;

    // Private constructor to prevent external instantiation
    private BoardHelperSingleton(int boardSize) {
        this.gameBoardModel = new BoardModel(boardSize);
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
        return gameBoardModel.getWidth();
    }

    public int getBoardHeight() {
        return gameBoardModel.getHeight();
    }


    public BoardModel getBoard() {
        return gameBoardModel;
    }


    //CLose out the board and reset the instance to nulllllllllllllllllllllll
    public void closeBoard()
    {
        if(gameBoardModel != null)
        {
            gameBoardModel.clearBoard();
            gameBoardModel = null;
        }

        instance = null;
    }
}

