package Model;

import java.util.ArrayList;
import java.util.List;

public class SerfPiece extends AbstractPiece{


    public SerfPiece(Position position, PIECE_DIRECTION direction) {
        super(position, direction);
    }

    List<Position> getValidPositions()
    {
        ArrayList<Position> positions = new ArrayList<>();

        Position piecePosition = this.getPosition();
        int x = piecePosition.getX();
        int y = piecePosition.getY();


        positions.add(new Position(x + 1, y + 1));
        positions.add(new Position(x - 1, y + 1));

        return positions;
    }
}
