package ModelTest;

import GameUtil.Game;
import Model.Checkers.Pieces.AbstractPiece;
import Model.Checkers.Pieces.SerfPiece;
import Model.Checkers.Position;
import Model.Math.Vector2i;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
;

public class PieceTest {

    @BeforeAll
    static void init()
    {
        Game.initialize(8);
    }

    @AfterEach
    void resetBoard()
    {
        Game.getInstance().getBoard().clearBoard();
    }

    @Test
    void testPieceCreation() {
        SerfPiece newPiece = new SerfPiece(new Position(1, 1), AbstractPiece.PEICE_TEAM.WHITE);
        assertNotNull(newPiece, "Piece should be created successfully.");
    }

    @Test
    void testPieceInitialPosition() {
        SerfPiece piece = new SerfPiece(new Position(3, 3), AbstractPiece.PEICE_TEAM.BLACK);
        assertEquals(3, piece.getPosition().getX());
        assertEquals(3, piece.getPosition().getY());
    }

    @Test
    void testPieceMovement() {
        SerfPiece piece = new SerfPiece(new Position(2, 2), AbstractPiece.PEICE_TEAM.BLACK);

        piece.move(new Vector2i(1, 1));  // Assume move() updates position
        assertEquals(new Position(3, 3), piece.getPosition());
    }

    @Test
    void testPieceTeamAssignment() {
        SerfPiece whitePiece = new SerfPiece(new Position(4, 4), AbstractPiece.PEICE_TEAM.WHITE);
        SerfPiece blackPiece = new SerfPiece(new Position(5, 5), AbstractPiece.PEICE_TEAM.BLACK);

        assertEquals(AbstractPiece.PEICE_TEAM.WHITE, whitePiece.getTeam());
        assertEquals(AbstractPiece.PEICE_TEAM.BLACK, blackPiece.getTeam());
    }

    @Test
    void testInvalidMovement() {
        SerfPiece piece = new SerfPiece(new Position(5, 5), AbstractPiece.PEICE_TEAM.WHITE);

        // Expecting IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            piece.move(new Vector2i(8, 8));
        }, "Piece should throw an exception for illegal moves.");
    }
}
