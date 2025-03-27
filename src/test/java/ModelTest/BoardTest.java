package ModelTest;

import GameUtil.Game;
import Model.Board;
import Model.AbstractPiece;
import Model.Position;
import Model.SerfPiece;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

public class BoardTest {


    @Test
    void initBoard()
    {
        Game.initialize(8);

        Assertions.assertEquals(8, Game.getInstance().getBoardHeight(), "Game did not initialize to the expected size.");
    }

    @RepeatedTest(1)  // Use @RepeatedTest(1) because we'll loop through all 64 spots within the test.
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

        Assertions.assertEquals("Invalid Addition Spot. Piece Already Exists @" + position.toString(), thrown.getMessage(), "Did not receive the expected result");
    }
}
