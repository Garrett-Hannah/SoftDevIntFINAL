package ModelTest;

import chkMVC.chModel.Checkers.BoardModel;
import chkMVC.chModel.Checkers.Pieces.AbstractPiece;
import chkMVC.chModel.Checkers.Pieces.SerfPiece;
import chkMVC.chModel.Checkers.Position;
import chkMVC.chModel.Math.Vector2i;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PieceTest {

    private static BoardModel gameBoardModel;

    @BeforeAll
    static void init()
    {
        gameBoardModel = new BoardModel(8);
    }

    @AfterEach
    void resetBoard()
    {
        gameBoardModel.clearBoard();
    }

    @Test
    void testPieceCreation() {
        SerfPiece newPiece = new SerfPiece(gameBoardModel.createPosition(1, 1), AbstractPiece.PEICE_TEAM.WHITE);
        assertNotNull(newPiece, "Piece should be created successfully.");
    }

    @Test
    void testPieceInitialPosition() {
        SerfPiece piece = new SerfPiece(gameBoardModel.createPosition(3, 3), AbstractPiece.PEICE_TEAM.BLACK);
        assertEquals(3, piece.getPosition().getX());
        assertEquals(3, piece.getPosition().getY());
    }

    @Test
    void testPieceMovement() {
        SerfPiece piece = new SerfPiece(gameBoardModel.createPosition(2, 2), AbstractPiece.PEICE_TEAM.BLACK);

        piece.move(gameBoardModel, new Vector2i(1, 1));  // Assume move() updates position
        assertEquals(gameBoardModel.createPosition(3, 3), piece.getPosition());
    }

    @Test
    void testPieceTeamAssignment() {
        SerfPiece whitePiece = new SerfPiece(gameBoardModel.createPosition(4, 4), AbstractPiece.PEICE_TEAM.WHITE);
        SerfPiece blackPiece = new SerfPiece(gameBoardModel.createPosition(5, 5), AbstractPiece.PEICE_TEAM.BLACK);

        assertEquals(AbstractPiece.PEICE_TEAM.WHITE, whitePiece.getTeam());
        assertEquals(AbstractPiece.PEICE_TEAM.BLACK, blackPiece.getTeam());
    }

    @Test
    void testInvalidMovement() {
        SerfPiece piece = new SerfPiece(gameBoardModel.createPosition(5, 5), AbstractPiece.PEICE_TEAM.WHITE);

        // Expecting IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            piece.move(gameBoardModel, new Vector2i(8, 8));
        }, "Piece should throw an exception for illegal moves.");
    }

    @AfterAll
    static void breakDown()
    {
        //gameBoardModel.closeBoard();
    }
}
