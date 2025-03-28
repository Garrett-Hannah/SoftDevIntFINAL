package Model;

import GameUtil.Game;
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
        this.direction = direction;
    }

    //Will need a recursive function to build up all possible moves. going to need a lot of testing for that.
    public ArrayList<Position> getRecursiveValidPositions()
    {
        return null;
    }

    //Return the valid positions.
    public ArrayList<Position> getValidPositions() {
        Position currentPosition = this.getPosition();
        Position positionOption1 = currentPosition.getDeltaPosition(1, this.direction.value);
        Position positionOption2 = currentPosition.getDeltaPosition(-1, this.direction.value);

        Board gameBoard = Game.getInstance().getBoard();

        ArrayList<Position> validPositions = new ArrayList<>();

        if(!gameBoard.isOccupied(positionOption1)) validPositions.add(positionOption1);
        if(!gameBoard.isOccupied(positionOption2)) validPositions.add(positionOption2);

        return validPositions;
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


