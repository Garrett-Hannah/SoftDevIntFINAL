package Model.Checkers.Pieces;

import GameUtil.Game;
import GameUtil.Incrementer;
import Model.Checkers.Board;
import Model.Checkers.Position;
import Model.Math.Vector2i;

import java.util.ArrayList;

public abstract class AbstractPiece {




    private int id;
    private Position position;
    protected PIECE_DIRECTION direction;
    protected PEICE_TEAM team;



    AbstractPiece(Position position, PEICE_TEAM team)
    {
        this.id = Incrementer.getInstance().increment();
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

    public boolean move(Vector2i delta) {
        Position current = this.position;
        current.getDeltaPosition(delta);
        return false;
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
}


