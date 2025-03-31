package chkMVC.chModel.Checkers;

import chkMVC.chModel.Checkers.Pieces.AbstractPiece;
import chkMVC.chModel.Checkers.Position;
import chkMVC.chModel.Math.Vector2i;

import java.util.HashMap;
import java.util.Map; // Use interface type
import java.util.Optional; // Better way to handle potentially null pieces

public class BoardModel {

    private final Map<Position, AbstractPiece> boardSpace; // Use Map interface
    private final int width;
    private final int height;

    public BoardModel(int size) {
        this(size, size); // Square board constructor
    }

    public BoardModel(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Board dimensions must be positive.");
        }
        this.width = width;
        this.height = height;
        this.boardSpace = new HashMap<>();
        // TODO: Add initial piece setup logic here if needed
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getNumberOfPieces() {
        return boardSpace.size();
    }

    // Get piece using Optional to avoid null checks elsewhere
    public Optional<AbstractPiece> getPieceOptional(Position position) {
        // Validate position belongs to this board conceptually
        if (position.getBoardWidth() != this.width || position.getBoardHeight() != this.height) {
            // Or log a warning, depending on desired strictness
            throw new IllegalArgumentException("Position " + position + " dimensions do not match board dimensions (" + width + "x" + height + ")");
        }
        return Optional.ofNullable(boardSpace.get(position));
    }

    // Get piece, returning null (less safe, but sometimes needed)
    public AbstractPiece getPieceAt(Position position) {
        if (position.getBoardWidth() != this.width || position.getBoardHeight() != this.height) {
            throw new IllegalArgumentException("Position dimensions mismatch");
        }
        return boardSpace.get(position);
    }


    public boolean isOccupied(Position position) {
        if (position.getBoardWidth() != this.width || position.getBoardHeight() != this.height) {
            throw new IllegalArgumentException("Position dimensions mismatch");
        }
        return boardSpace.containsKey(position);
    }

    // Internal method to place a piece - used during setup or potentially moves
    // Consider making this package-private or protected if only specific classes should use it
    public void placePiece(AbstractPiece piece) {
        Position pos = piece.getPosition();
        if (pos.getBoardWidth() != this.width || pos.getBoardHeight() != this.height) {
            throw new IllegalArgumentException("Piece position dimensions mismatch");
        }
        if (isOccupied(pos)) {
            throw new IllegalStateException("Cannot place piece at " + pos + ", already occupied by " + boardSpace.get(pos));
        }
        boardSpace.put(pos, piece);
    }

    // Removes a piece - returns true if a piece was removed
    public boolean removePiece(Position position) {
        if (position.getBoardWidth() != this.width || position.getBoardHeight() != this.height) {
            throw new IllegalArgumentException("Position dimensions mismatch");
        }
        return boardSpace.remove(position) != null;
    }

    // Moves a piece - handles removal and placement, checks if 'from' is occupied
    public void movePiece(Position from, Position to) {
        if (from.equals(to)) {
            throw new IllegalArgumentException("Cannot move piece to the same position: " + from);
        }
        AbstractPiece piece = getPieceAt(from);
        if (piece == null) {
            throw new IllegalStateException("No piece found at starting position: " + from);
        }
        if (isOccupied(to)) {
            throw new IllegalStateException("Cannot move to occupied position: " + to);
        }

        // Remove from old, place at new, update piece's internal state
        boardSpace.remove(from);
        boardSpace.put(to, piece);
        piece.setPositionInternal(to); // Update the piece's internal position
    }


    @Override
    public String toString() {
        // Basic toString, printBoard is better for visualization
        return "BoardModel{" +
                "width=" + width +
                ", height=" + height +
                ", pieces=" + boardSpace.size() +
                '}';
    }

    // Using for debugging... (Keep this for testing/console use)
    public void printBoard() {
        System.out.println("Board State (" + width + "x" + height + "):");
        // Assuming (1,1) is top-left for printing
        // Column Headers (A, B, C...)
        System.out.print("  ");
        for (int j = 0; j < width; j++) {
            System.out.print(" " + (char)('A' + j));
        }
        System.out.println();
        System.out.print("  ");
        for (int j = 0; j < width; j++) {
            System.out.print("--");
        }
        System.out.println("-");


        for (int i = 1; i <= height; i++) { // Rows 1 to height
            System.out.printf("%d|", i); // Row label

            for (int j = 1; j <= width; j++) { // Columns 1 to width
                try {
                    Position currentPos = new Position(j, i, width, height);
                    Optional<AbstractPiece> pieceOpt = getPieceOptional(currentPos);
                    if (pieceOpt.isPresent()) {
                        AbstractPiece piece = pieceOpt.get();
                        System.out.print(" " + (piece.getTeam() == AbstractPiece.PEICE_TEAM.WHITE ? 'W' : 'B'));
                    } else {
                        // Checkerboard pattern (optional, can just print '.')
                        // if ((i + j) % 2 == 0) System.out.print(" #"); // Dark square
                        // else System.out.print(" .");                 // Light square
                        System.out.print(" .");
                    }
                } catch(IllegalArgumentException e) {
                    System.out.print(" X"); // Should not happen if loop is correct
                }
            }
            System.out.println(" |"); // End of row
        }
        System.out.print("  ");
        for (int j = 0; j < width; j++) {
            System.out.print("--");
        }
        System.out.println("-");
    }

    public void clearBoard() {
        boardSpace.clear();
    }

    public Vector2i getSize() {
        return new Vector2i(this.getWidth(), this.getHeight());
    }

    public Position createPosition(int x, int y)
    {
        //Create the position based from the baord.
        return new Position(x, y, this.width, this.height);
    }

    /**
     * Converts a 0-based linear integer index (e.g., from a flattened array or UI grid)
     * to a 1-based (x, y) Position object for a board of given dimensions.
     * Assumes row-major order (0 is top-left, width-1 is top-right).
     *
     * @param linearInt The 0-based linear index (0 to width*height - 1).
     * @return The corresponding Position object.
     * @throws IllegalArgumentException if linearInt is out of range [0, width*height - 1]
     *         or if board dimensions are invalid.
     */
    public Position positionFromInt(int linearInt) {
        int boardHeight = this.getHeight();
        int boardWidth = this.getWidth();

        if (boardHeight <= 0 || boardWidth <= 0) {
            throw new IllegalArgumentException("Board dimensions must be positive.");
        }

        int maxIndex = boardWidth * boardHeight - 1;
        if (linearInt < 0 || linearInt > maxIndex) {
            throw new IllegalArgumentException(String.format(
                    "Linear index %d is out of range [0, %d] for a %dx%d board.",
                    linearInt, maxIndex, boardWidth, boardHeight));
        }

        // Calculate 0-based row and column
        int zeroBasedRow = linearInt / boardWidth;  // Row (0 to height-1)
        int zeroBasedCol = linearInt % boardWidth;  // Column (0 to width-1)

        // Convert to 1-based coordinates
        int xValue = zeroBasedCol + 1;
        int yValue = boardHeight - zeroBasedRow;  // If board is inverted (A1 at bottom)

        // Create position with board validation
        return new Position(xValue, yValue, boardWidth, boardHeight);
    }


    // Creates a deep copy for passing state around immutably if needed
    // Note: This requires AbstractPiece subclasses to have copy constructors or similar
    /*
    public BoardModel copy() {
        BoardModel newBoard = new BoardModel(this.width, this.height);
        for (Map.Entry<Position, AbstractPiece> entry : this.boardSpace.entrySet()) {
             // Assuming AbstractPiece has a copy() method or copy constructor
             // newBoard.placePiece(entry.getValue().copy());
        }
        return newBoard;
    }
    */
}