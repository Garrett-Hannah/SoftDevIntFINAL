package ModelTest;

import chkMVC.chModel.Checkers.BoardModel;
import chkMVC.chModel.Checkers.Pieces.AbstractPiece;
import chkMVC.chModel.Checkers.Position;
import chkMVC.chModel.Checkers.Pieces.SerfPiece;
import chkMVC.chModel.Math.Vector2i;
import org.junit.jupiter.api.*;

import java.util.List; // Use List instead of ArrayList for return type consistency

import static org.junit.jupiter.api.Assertions.*; // Use static imports for brevity

public class BoardModelTest {

    private static BoardModel gameBoardModel; // Hold the board model instance

    @BeforeAll
    static void initBoard() {
        gameBoardModel = new BoardModel(8); // Get the board instance
        System.out.println("Board Initialized for Tests.");
    }

    @AfterEach
    void clearBoardAfterTest() {
        gameBoardModel.clearBoard(); // Clear board after each test
        assertEquals(0, gameBoardModel.getNumberOfPieces(), "Board should be empty after clearing.");
        System.out.println("--- Test Finished, Board Cleared ---");
    }

    @Test
    void testBoardInitProper() {
        System.out.println("Testing Board Initialization...");
        assertNotNull(gameBoardModel, "BoardModel instance should not be null.");
        assertEquals(8, gameBoardModel.getHeight(), "Game board height did not initialize to the expected size.");
        assertEquals(8, gameBoardModel.getWidth(), "Game board width did not initialize to the expected size.");
        assertEquals(0, gameBoardModel.getNumberOfPieces(), "Newly initialized board should have 0 pieces.");
        System.out.println("Board Initialization Test Passed.");
    }

    @Test
    void testPlaceAndGetPiece() {
        System.out.println("Testing Piece Placement and Retrieval...");
        Position pos = gameBoardModel.createPosition(3, 3);
        SerfPiece piece = new SerfPiece(pos, AbstractPiece.PEICE_TEAM.WHITE);

        // Place the piece
        assertDoesNotThrow(() -> gameBoardModel.placePiece(piece), "Placing a piece on an empty square should not throw.");
        assertEquals(1, gameBoardModel.getNumberOfPieces(), "Board should have 1 piece after placement.");
        assertTrue(gameBoardModel.isOccupied(pos), "Position should be occupied after placement.");

        // Retrieve the piece
        AbstractPiece retrievedPiece = gameBoardModel.getPieceAt(pos);
        assertNotNull(retrievedPiece, "Should be able to retrieve the placed piece.");
        assertSame(piece, retrievedPiece, "Retrieved piece should be the same instance as the placed piece.");
        assertEquals(piece.getId(), retrievedPiece.getId(), "Retrieved piece ID should match.");
        assertEquals(pos, retrievedPiece.getPosition(), "Retrieved piece position should match.");

        System.out.println("Piece Placement and Retrieval Test Passed.");
        gameBoardModel.printBoard(); // Optional: print board state
    }


    @Test
    void testAddPieceToOccupiedSpot() {
        Vector2i boardSize = gameBoardModel.getSize();
        System.out.println("Testing Adding Piece to Occupied Spot...");
        Position position = gameBoardModel.createPosition(3, 3);

        // Place the first piece
        SerfPiece piece1 = new SerfPiece(position, AbstractPiece.PEICE_TEAM.WHITE);
        gameBoardModel.placePiece(piece1);
        assertTrue(gameBoardModel.isOccupied(position), "The spot should be occupied after adding the first piece.");
        assertEquals(1, gameBoardModel.getNumberOfPieces(), "Should be 1 piece on board.");

        // Try to place a second piece at the same spot
        SerfPiece piece2 = new SerfPiece(position, AbstractPiece.PEICE_TEAM.BLACK); // Different team, still invalid

        // Expect an IllegalStateException
        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> gameBoardModel.placePiece(piece2), // Use placePiece, not addPiece
                "Expected placePiece() to throw an IllegalStateException when adding to an occupied spot."
        );

        // Check the exception message (optional, but good practice)
        // The message includes the piece occupying the spot, so we check the start
        assertTrue(thrown.getMessage().startsWith("Cannot place piece at " + position),
                "Exception message should indicate the occupied position. Got: " + thrown.getMessage());

        assertEquals(1, gameBoardModel.getNumberOfPieces(), "Number of pieces should remain 1 after failed placement.");

        System.out.println("Adding to Occupied Spot Test Passed.");
        gameBoardModel.printBoard(); // Optional: print board state
    }

    @Test
    void testGettingValidSimpleMovesEmptyBoard() {
        System.out.println("Testing Valid Simple Moves on Empty Board...");

        Position startPos = gameBoardModel.createPosition(3, 3); // C3
        SerfPiece myPiece = new SerfPiece(startPos, AbstractPiece.PEICE_TEAM.WHITE);
        gameBoardModel.placePiece(myPiece);

        // White moves forward (Y increases)
        Position expectedPos1 = gameBoardModel.createPosition(2, 4); // B4 (Forward-Left)
        Position expectedPos2 = gameBoardModel.createPosition(4, 4); // D4 (Forward-Right)

        // Use getValidSimpleMoves
        List<Position> validMoveSpots = myPiece.getValidSimpleMoves(gameBoardModel);

        assertNotNull(validMoveSpots, "Valid moves list should not be null.");
        assertEquals(2, validMoveSpots.size(), "White piece at C3 on empty board should have 2 simple moves.");

        // Check if both expected positions are present (order might vary, so use contains)
        assertTrue(validMoveSpots.contains(expectedPos1), "Valid moves should include B4.");
        assertTrue(validMoveSpots.contains(expectedPos2), "Valid moves should include D4.");

        System.out.println("Valid Simple Moves (Empty Board) Test Passed.");
        gameBoardModel.printBoard();
    }


    @Test
    void testGetValidSimpleMoveBlockedByOwnPiece() {
        System.out.println("Testing Valid Simple Moves Blocked by Own Piece...");
        Position startPos = gameBoardModel.createPosition(3, 3); // C3 (White)
        Position blockingPos = gameBoardModel.createPosition(2, 4); // B4 (White) - Blocks forward-left

        SerfPiece protagPiece = new SerfPiece(startPos, AbstractPiece.PEICE_TEAM.WHITE);
        SerfPiece blockingPiece = new SerfPiece(blockingPos, AbstractPiece.PEICE_TEAM.WHITE); // Same team

        gameBoardModel.placePiece(protagPiece);
        gameBoardModel.placePiece(blockingPiece);

        Position expectedPos = gameBoardModel.createPosition(4, 4); // D4 (Forward-Right) - Should still be valid

        // Use getValidSimpleMoves
        List<Position> validMoveSpots = protagPiece.getValidSimpleMoves(gameBoardModel);

        assertNotNull(validMoveSpots, "Valid moves list should not be null.");
        assertEquals(1, validMoveSpots.size(), "Should only have ONE available simple move when one path is blocked by own piece.");
        assertEquals(expectedPos, validMoveSpots.get(0), "The only valid move should be D4.");

        System.out.println("Valid Simple Moves (Blocked by Own Piece) Test Passed.");
        gameBoardModel.printBoard();
    }

    @Test
    void testGetValidSimpleMoveBlockedByOpponentPiece() {
        System.out.println("Testing Valid Simple Moves Blocked by Opponent Piece...");
        Position startPos = gameBoardModel.createPosition(3, 3); // C3 (White)
        Position blockingPos = gameBoardModel.createPosition(4, 4); // D4 (Black) - Blocks forward-right

        SerfPiece protagPiece = new SerfPiece(startPos, AbstractPiece.PEICE_TEAM.WHITE);
        SerfPiece blockingPiece = new SerfPiece(blockingPos, AbstractPiece.PEICE_TEAM.BLACK); // Opponent team

        gameBoardModel.placePiece(protagPiece);
        gameBoardModel.placePiece(blockingPiece);

        Position expectedPos = gameBoardModel.createPosition(2, 4); // B4 (Forward-Left) - Should still be valid

        // Use getValidSimpleMoves
        List<Position> validMoveSpots = protagPiece.getValidSimpleMoves(gameBoardModel);

        assertNotNull(validMoveSpots, "Valid moves list should not be null.");
        assertEquals(1, validMoveSpots.size(), "Should only have ONE available simple move when one path is blocked by opponent.");
        assertEquals(expectedPos, validMoveSpots.get(0), "The only valid move should be B4.");

        System.out.println("Valid Simple Moves (Blocked by Opponent Piece) Test Passed.");
        gameBoardModel.printBoard();
    }

    // Test for Black piece simple moves
    @Test
    void testBlackPieceSimpleMoves() {
        System.out.println("Testing Black Piece Simple Moves...");
        Position startPos = gameBoardModel.createPosition(5, 5); // E5
        SerfPiece blackPiece = new SerfPiece(startPos, AbstractPiece.PEICE_TEAM.BLACK);
        gameBoardModel.placePiece(blackPiece);

        // Black moves forward (Y decreases)
        Position expectedPos1 = gameBoardModel.createPosition(4, 4); // D4 (Forward-Left for Black)
        Position expectedPos2 = gameBoardModel.createPosition(6, 4); // F4 (Forward-Right for Black)

        List<Position> validMoveSpots = blackPiece.getValidSimpleMoves(gameBoardModel);

        assertNotNull(validMoveSpots, "Valid moves list should not be null.");
        assertEquals(2, validMoveSpots.size(), "Black piece at E5 on empty board should have 2 simple moves.");
        assertTrue(validMoveSpots.contains(expectedPos1), "Valid moves should include D4.");
        assertTrue(validMoveSpots.contains(expectedPos2), "Valid moves should include F4.");

        System.out.println("Black Piece Simple Moves Test Passed.");
        gameBoardModel.printBoard();
    }


    // TODO: Add tests for getValidJumpMoves in a similar fashion
    // - Jump over opponent to empty square
    // - No jump if landing square is occupied
    // - No jump over own piece
    // - No jump if intermediate square is empty
    // - Edge cases (near board edges)

    @AfterAll
    static void breakDown() {
        System.out.println("Board Closed After All Tests.");
    }
}
