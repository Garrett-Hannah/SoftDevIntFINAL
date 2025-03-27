package ModelTest;

import GameUtil.Game;
import Model.Board;
import Model.AbstractPiece;
import Model.Position;
import Model.SerfPiece;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BoardTest {


    @Test
    void initBoard()
    {
        Game.initialize(8);

        Assertions.assertEquals(8, Game.getInstance().getBoardHeight(), "Game did not initialize to the expected size.");
    }

    @Test
    void placePiece()
    {
        Board gameBoard = Game.getInstance().getBoard();


        SerfPiece newPeice = new SerfPiece(new Position(3, 3), AbstractPiece.PIECE_DIRECTION.FORWARD);

        gameBoard.addPiece(newPeice);

        gameBoard.printBoard();
    }
}
