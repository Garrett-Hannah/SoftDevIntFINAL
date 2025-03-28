package ModelTest;

import GameUtil.Game;
import Model.Board;
import Model.AbstractPiece;
import Model.Position;
import Model.SerfPiece;
import org.junit.jupiter.api.*;

import java.util.ArrayList;


public class BoardTest {

    @BeforeAll
    static void initBoard()
    {
        Game.initialize(8);
    }

    @Test
    void testBoardInitProper()
    {
        Assertions.assertEquals(8, Game.getInstance().getBoardHeight(), "Game did not initialize to the expected size.");
    }


    @AfterEach
    void postTest()
    {

        Game.getInstance().getBoard().clearBoard();
    }

    @Test  // Use @RepeatedTest(1) because we'll loop through all 64 spots within the test.
    void placePiece() {
        Board gameBoard = Game.getInstance().getBoard();

        // Loop through all 8x8 positions on the board (assuming 8x8 grid)
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                // Create a new piece for each position
                SerfPiece newPiece = new SerfPiece(new Position(x + 1, y + 1), AbstractPiece.PIECE_DIRECTION.FORWARD);

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

        gameBoard.clearBoard();
    }

    @Test
    void testAddPieceToOccupiedSpot() {
        Board gameBoard = Game.getInstance().getBoard();

        // Create a position for testing
        Position position = new Position(3, 3);

        // Create and add the first piece to the board
        SerfPiece piece1 = new SerfPiece(position, AbstractPiece.PIECE_DIRECTION.FORWARD);
        gameBoard.addPiece(piece1);

        // Check that the position is occupied
        Assertions.assertTrue(gameBoard.isOccupied(position), "The spot should be occupied after adding the first piece.");

        // Try to add a piece to the same position and check that an exception is thrown
        SerfPiece piece2 = new SerfPiece(position, AbstractPiece.PIECE_DIRECTION.FORWARD);
        IllegalArgumentException thrown = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> gameBoard.addPiece(piece2),
                "Expected addPiece() to throw an exception when trying to add a piece to an occupied spot."
        );

        Assertions.assertEquals("Invalid Addition Spot. Piece Already Exists @" + position, thrown.getMessage(), "Did not receive the expected result");

        gameBoard.clearBoard();
    }


    @Test
    void testGettingValidPositions()
    {
        Board gameBoard = Game.getInstance().getBoard();

        SerfPiece myPiece = new SerfPiece(new Position(3, 3), AbstractPiece.PIECE_DIRECTION.FORWARD);

        gameBoard.addPiece(myPiece);

        ArrayList<Position> validMoveSpots = myPiece.getValidPositions();

        Assertions.assertEquals(new Position(4, 4), validMoveSpots.get(0), "Did Not equal expected value.");
        Assertions.assertEquals(new Position(2, 4), validMoveSpots.get(1), "Did Not equal expected value.");
    }

    /*
    Board Setup:
     oooooooo
     oooooooo
     ooooXooo
     oooXoooo
     oooooooo
     */
    @Test
    void testGetValidPositionAgainstOtherPiece()
    {
        Board gameBoard = Game.getInstance().getBoard();

        SerfPiece protagPiece = new SerfPiece(new Position(3, 3), AbstractPiece.PIECE_DIRECTION.FORWARD);

        //Create secondary piece to check against (should return one valid position) (for now later it will return two)
        SerfPiece antagPiece = new SerfPiece(new Position(2, 4), AbstractPiece.PIECE_DIRECTION.FORWARD);

        gameBoard.addPiece(protagPiece);
        gameBoard.addPiece(antagPiece);

        ArrayList<Position> positions = protagPiece.getValidPositions();

        Assertions.assertEquals(1, positions.size(), "Expected only ONE available movement!");

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
