package chModel.Checkers.Pieces;

import chGameUtil.BoardHelperSingleton;
import chGameUtil.IncrementerSingleton;
import chModel.Checkers.BoardModel;
import chModel.Checkers.Position;
import chModel.Math.Vector2i;

import java.util.ArrayList;

public abstract class AbstractPiece {




    private int id;
    private Position position;
    protected PIECE_DIRECTION direction;
    protected PEICE_TEAM team;



    AbstractPiece(Position position, PEICE_TEAM team)
    {
        this.id = IncrementerSingleton.getInstance().increment();
        this.position = position;
        this.direction = (team == PEICE_TEAM.WHITE) ? PIECE_DIRECTION.FORWARD : PIECE_DIRECTION.BACKWARD;
        this.team = team;
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

        BoardModel gameBoardModel = BoardHelperSingleton.getInstance().getBoard();

        ArrayList<Position> validPositions = new ArrayList<>();

        if(!gameBoardModel.isOccupied(positionOption1)) validPositions.add(positionOption1);
        if(!gameBoardModel.isOccupied(positionOption2)) validPositions.add(positionOption2);

        return validPositions;
    }

    public Position getPosition() {
        return position;
    }

    public int getId() {
        return id;
    }

    public void move(Vector2i delta) {

        Position newPosition = position.getDeltaPosition(delta);

        if (!isValidMove(newPosition)) {  // Assuming
            throw new IllegalArgumentException("Invalid move: " + delta);
        }

        this.position = newPosition;
    }


    public PEICE_TEAM getTeam() {
        return this.team;
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

    public enum PEICE_TEAM{
        WHITE(0),
        BLACK(1);

        private final int value;

        PEICE_TEAM(int value){
            this.value = value;
        }
    }


    public boolean isValidMove(Position position)
    {
        BoardModel gameBoardModel = BoardHelperSingleton.getInstance().getBoard();

        return !gameBoardModel.isOccupied(position);
    }
}


