package Model;

import GameUtil.Incrementer;

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

    public void addPiece(AbstractPiece piece)
    {
        boardSpace.put(piece.getPosition(), piece);
    }

    @Override
    public String toString() {
        return "Board{" +
                "boardSpace=" + boardSpace +
                ", size=" + size +
                '}';
    }

    public void printBoard()
    {
        for(int i = 0; i < size; i++)
        {
            for(int j = 0; j < size; j++)
            {
                AbstractPiece piece = (boardSpace.get(new Position(i + 1, j + 1)));

                if(piece == null) System.out.print("*");
                else System.out.print(piece.id);
            }

            System.out.println("");
        }
    }
}
