package ModelTest;

import Model.AbstractPiece.*;
import Model.Position;
import Model.SerfPiece;
import org.junit.jupiter.api.Test;

public class PieceTest {

    @Test
    void testPieceCreation()
    {
        SerfPiece newPiece = new SerfPiece(new Position(1, 1), PIECE_DIRECTION.FORWARD);
    }

}
