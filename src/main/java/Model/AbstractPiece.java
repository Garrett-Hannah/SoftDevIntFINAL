package Model;

import GameUtil.Incrementer;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPiece {

    int id;
    private Position position;
    PIECE_DIRECTION direction;


    AbstractPiece(Position position, PIECE_DIRECTION direction)
    {
        this.position = position;
        this.id = Incrementer.getInstance().increment();
    }

    List<Position> getValidPositions()
    {
        return null;
    }

    public Position getPosition() {
        return position;
    }

    public int getId() {
        return id;
    }

    //Provides info on the cardinal direction of the piece....
    //Just means that when dealing with y axis stuff things are easier.
    public enum PIECE_DIRECTION {
        FORWARD(1),
        BACKWARD(-1);

        private final int value;

        PIECE_DIRECTION(int value) {
            this.value = value;
        }
    }
}


