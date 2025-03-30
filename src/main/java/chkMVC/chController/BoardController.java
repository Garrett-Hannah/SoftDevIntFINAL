package chkMVC.chController;

import chkMVC.chModel.Checkers.Pieces.AbstractPiece;
import chkMVC.chModel.Checkers.Position;

//I dont think that users should be able to
public interface BoardController {

    void MovePiece(AbstractPiece piece, Position newPosition); //Function that takes a piece and set new position.
    //Function to get the piece at a position.
    AbstractPiece getPieceAt(Position position);
    //Return the other thing i guess.............
    boolean isPositionOccupied(Position position);
}
