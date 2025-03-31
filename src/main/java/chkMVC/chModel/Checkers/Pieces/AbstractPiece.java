package chkMVC.chModel.Checkers.Pieces;

import chkGameUtil.IncrementerSingleton;
import chkMVC.chModel.Checkers.BoardModel;
import chkMVC.chModel.Checkers.Position;
import chkMVC.chModel.Math.Vector2i;

import java.util.ArrayList;
import java.util.List;

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
    public ArrayList<Position> getValidPositions(BoardModel board) {
        Position currentPosition = this.getPosition();
        Position positionOption1 = currentPosition.getDeltaPosition(1, this.direction.value);
        Position positionOption2 = currentPosition.getDeltaPosition(-1, this.direction.value);

        ArrayList<Position> validPositions = new ArrayList<>();

        if(!board.isOccupied(positionOption1)) validPositions.add(positionOption1);
        if(!board.isOccupied(positionOption2)) validPositions.add(positionOption2);

        return validPositions;
    }

    public Position getPosition() {
        return position;
    }

    public int getId() {
        return id;
    }

    public void move(BoardModel board, Vector2i delta) {

        Position newPosition = position.getDeltaPosition(delta);

        if (!isValidMove(board, newPosition)) {  // Assuming
            throw new IllegalArgumentException("Invalid move: " + delta);
        }

        this.position = newPosition;
    }


    public PEICE_TEAM getTeam() {
        return this.team;
    }

    public abstract List<Position> getValidSimpleMoves(BoardModel board);

    public abstract List<Position> getValidJumpMoves(BoardModel board);

    public void setPositionInternal(Position to) {
        this.position = to;
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

    public int getDirection()
    {
        return this.team.value;
    }


    public boolean isValidMove(BoardModel model, Position position)
    {
        return !model.isOccupied(position);
    }
}


