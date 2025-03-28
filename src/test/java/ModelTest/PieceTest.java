package ModelTest;

import Model.Checkers.Pieces.AbstractPiece.*;
import Model.Checkers.Position;
import Model.Checkers.Pieces.SerfPiece;
import org.junit.jupiter.api.Test;

public class PieceTest {

    @Test
    void testPieceCreation()
    {
        SerfPiece newPiece = new SerfPiece(new Position(1, 1), PIECE_DIRECTION.FORWARD, PEICE_TEAM.WHITE);
    }

}
