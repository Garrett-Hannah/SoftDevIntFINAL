package chkMVC.chModel.Checkers.Pieces;

import chkMVC.chModel.Checkers.BoardModel;
import chkMVC.chModel.Checkers.Position;

import java.util.ArrayList;
import java.util.List;

public class SerfPiece extends AbstractPiece {

    public SerfPiece(Position position, PEICE_TEAM team) {
        super(position, team);
    }

    @Override
    public List<Position> getValidSimpleMoves(BoardModel board) {
        List<Position> validMoves = new ArrayList<>();
        Position current = getPosition();
        int forwardY = getDirection(); // Get the Y direction based on team

        // Check forward-left
        try {
            Position potentialPos = current.getDeltaPosition(-1, forwardY);
            if (!board.isOccupied(potentialPos)) {
                validMoves.add(potentialPos);
            }
        } catch (IllegalArgumentException e) { /* Off board */ }

        // Check forward-right
        try {
            Position potentialPos = current.getDeltaPosition(1, forwardY);
            if (!board.isOccupied(potentialPos)) {
                validMoves.add(potentialPos);
            }
        } catch (IllegalArgumentException e) { /* Off board */ }

        return validMoves;
    }

    @Override
    public List<Position> getValidJumpMoves(BoardModel board) {
        List<Position> jumpMoves = new ArrayList<>();
        Position current = getPosition();
        int forwardY = getDirection(); // Y direction for moving forward

        // Check jump forward-left
        try {
            Position jumpOverPos = current.getDeltaPosition(-1, forwardY);
            Position landingPos = current.getDeltaPosition(-2, forwardY * 2); // Jump lands 2 steps away
            AbstractPiece pieceToJump = board.getPieceAt(jumpOverPos);
            // Must jump over an OPPONENT piece onto an EMPTY square
            if (pieceToJump != null && pieceToJump.getTeam() != this.getTeam() && !board.isOccupied(landingPos)) {
                jumpMoves.add(landingPos);
            }
        } catch (IllegalArgumentException e) { /* Off board */ }

        // Check jump forward-right
        try {
            Position jumpOverPos = current.getDeltaPosition(1, forwardY);
            Position landingPos = current.getDeltaPosition(2, forwardY * 2);
            AbstractPiece pieceToJump = board.getPieceAt(jumpOverPos);
            if (pieceToJump != null && pieceToJump.getTeam() != this.getTeam() && !board.isOccupied(landingPos)) {
                jumpMoves.add(landingPos);
            }
        } catch (IllegalArgumentException e) { /* Off board */ }

        // TODO: Add multi-jump logic if required (often handled in the Controller/Game Logic)

        return jumpMoves;
    }
}