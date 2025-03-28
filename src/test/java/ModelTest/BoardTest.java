package ModelTest;

import chGameUtil.BoardHelperSingleton;
import chModel.Checkers.Board;
import chModel.Checkers.Pieces.AbstractPiece;
import chModel.Checkers.Position;
import chModel.Checkers.Pieces.SerfPiece;
import org.junit.jupiter.api.*;

import java.util.ArrayList;


public class BoardTest {

    private static final boolean Verbose = false;

    @BeforeAll
    static void initBoard()
    {
        BoardHelperSingleton.initialize(8);
    }

    @Test
    void testBoardInitProper()
    {
        Assertions.assertEquals(8, BoardHelperSingleton.getInstance().getBoardHeight(), "Game did not initialize to the expected size.");
    }


    @AfterEach
    void postTest()
    {

        BoardHelperSingleton.getInstance().getBoard().clearBoard();
    }

    @Test
    void placePiece() {

        System.out.println("Function Test::" + new Object(){}.getClass().getEnclosingMethod().getName());

        Board gameBoard = BoardHelperSingleton.getInstance().getBoard();

        // Loop through all 8x8 positions on the board (assuming 8x8 grid)
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                // Create a new piece for each position
                SerfPiece newPiece = new SerfPiece(new Position(x + 1, y + 1), AbstractPiece.PEICE_TEAM.WHITE);

                // Add the piece to the board
                gameBoard.addPiece(newPiece);

                // Assert that the piece was added
                Assertions.assertEquals(
                        (x * 8 + y + 1), // The number of pieces should match the current piece count
                        gameBoard.getNumberOfPieces(),
                        "Error: Number of pieces on board not incremented correctly at position (" + x + ", " + y + ")."
                );
            }
        }

        gameBoard.printBoard();


        System.out.println("Test Passed.");

        gameBoard.clearBoard();
    }

    @Test
    void testAddPieceToOccupiedSpot() {
        System.out.println("Function Test::" + new Object(){}.getClass().getEnclosingMethod().getName());


        Board gameBoard = BoardHelperSingleton.getInstance().getBoard();

        // Create a position for testing
        Position position = new Position(3, 3);

        // Create and add the first piece to the board
        SerfPiece piece1 = new SerfPiece(position, AbstractPiece.PEICE_TEAM.WHITE);
        gameBoard.addPiece(piece1);

        // Check that the position is occupied
        Assertions.assertTrue(gameBoard.isOccupied(position), "The spot should be occupied after adding the first piece.");

        // Try to add a piece to the same position and check that an exception is thrown
        SerfPiece piece2 = new SerfPiece(position, AbstractPiece.PEICE_TEAM.WHITE);
        IllegalArgumentException thrown = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> gameBoard.addPiece(piece2),
                "Expected addPiece() to throw an exception when trying to add a piece to an occupied spot."
        );

        Assertions.assertEquals("Invalid Addition Spot. Piece Already Exists @" + position, thrown.getMessage(), "Did not receive the expected result");


        System.out.println("Test Passed.");

        gameBoard.clearBoard();
    }


    @Test
    void testGettingValidPositions()
    {
        System.out.println("Function Test::" + new Object(){}.getClass().getEnclosingMethod().getName());

        Board gameBoard = BoardHelperSingleton.getInstance().getBoard();

        SerfPiece myPiece = new SerfPiece(new Position(3, 3), AbstractPiece.PEICE_TEAM.WHITE);

        gameBoard.addPiece(myPiece);

        ArrayList<Position> validMoveSpots = myPiece.getValidPositions();

        Assertions.assertEquals(new Position(4, 4), validMoveSpots.get(0), "Did Not equal expected value.");
        Assertions.assertEquals(new Position(2, 4), validMoveSpots.get(1), "Did Not equal expected value.");


        System.out.println("Test Passed.");

        gameBoard.printBoard();
    }



    /*
    Board Setup:
  a b c d e f g h
8 # . # . # . # .
7 . # . # . # . #
6 # . # . # . # .
5 . # . # O # . #
4 # . # O # . # .
3 . # . # . # . #
2 # . # . # . # .
1 . # . # . # . #
     */
    //Expected result: there is one valid movement position.
    @Test
    void testGetValidPositionAgainstOtherPiece()
    {
        System.out.println("Function Test::" + new Object(){}.getClass().getEnclosingMethod().getName());

        Board gameBoard = BoardHelperSingleton.getInstance().getBoard();

        SerfPiece protagPiece = new SerfPiece(new Position(3, 3), AbstractPiece.PEICE_TEAM.WHITE);

        //Create secondary piece to check against (should return one valid position) (for now later it will return two)
        SerfPiece antagPiece = new SerfPiece(new Position(2, 4), AbstractPiece.PEICE_TEAM.WHITE);

        gameBoard.addPiece(protagPiece);
        gameBoard.addPiece(antagPiece);

        ArrayList<Position> positions = protagPiece.getValidPositions();

        Assertions.assertEquals(1, positions.size(), "Expected only ONE available movement!");

        System.out.println("Test Passed.");

        gameBoard.printBoard();
    }

    @AfterAll
    static void breakDown()
    {
        BoardHelperSingleton.getInstance().closeBoard();
    }

    /*
    Board Setup:
     oooooooo
     oooooooo
     ooooXooo
     oooXoooo
     oooooooo
     */




}
