package chModel.Checkers;

import chModel.Checkers.Pieces.AbstractPiece;

import java.util.HashMap;

public class Board {

    HashMap<Position, AbstractPiece> boardSpace;

    int size;

    public Board(int size)
    {
        this.size = size;

        boardSpace = new HashMap<>();
    }

    public int getWidth(){
        return this.size;
    }

    public int getHeight()
    {
        return this.size;
    }

    public int getNumberOfPieces()
    {
        return boardSpace.size();
    }

    public void addPiece(AbstractPiece piece)
    {
        if(!isOccupied(piece.getPosition()))
            boardSpace.put(piece.getPosition(), piece);
        else throw new IllegalArgumentException("Invalid Addition Spot. Piece Already Exists @" + boardSpace.get(piece.getPosition()).getPosition().toString());
    }

    public boolean isOccupied(Position position)
    {
        return boardSpace.get(position) != null;
    }

    @Override
    public String toString() {
        return "Board{" +
                "boardSpace=" + boardSpace +
                ", size=" + size +
                '}';
    }

    //Using for debugging...
    public void printBoard() {
        char[] columns = "abcdefgh".toCharArray(); // Column labels

        // Print column headers
        System.out.print("  ");
        for (char c : columns) {
            System.out.print(c + " ");
        }
        System.out.println();

        // Print rows
        for (int i = size; i > 0; i--) { // Start from the top row (8 down to 1)
            System.out.print(i + " "); // Row label

            for (int j = 1; j <= size; j++) { // Columns (1 to 8)
                AbstractPiece piece = boardSpace.get(new Position(i, j));

                if (piece != null) {
                    System.out.print((piece.getTeam() == AbstractPiece.PEICE_TEAM.WHITE) ? "W" : "B" + " "); // Print piece ID
                } else {
                    // Alternate between `#` and `.` for checkerboard pattern
                    if ((i + j) % 2 == 0) System.out.print("# ");
                    else System.out.print(". ");
                }
            }

            System.out.println(); // New line after each row
        }
    }


    public void clearBoard()
    {
        boardSpace = new HashMap<Position, AbstractPiece>();
    }


}
