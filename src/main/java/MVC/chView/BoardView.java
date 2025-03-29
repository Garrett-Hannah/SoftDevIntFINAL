package MVC.chView;

import MVC.chController.BoardController;
import MVC.chModel.Checkers.Pieces.AbstractPiece;
import MVC.chModel.Checkers.Position;
import chNetwork.Client.ClientLogic;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class BoardView implements BoardController {



    private ClientLogic clientLogic;

    public JFrame frame;
    private JButton announmentButton;

    ArrayList<JButton> boardButtons;


    BoardView(ClientLogic clientLogic)
    {
        this.clientLogic = clientLogic;

        setupUI();
        addListener();

        frame.setVisible(true);
    }

    JPanel buildBoardGrid(int boardSize)
    {
        JPanel gridPanel = new JPanel();

        gridPanel.setLayout(new GridLayout(boardSize, boardSize));

        for(int i = 0; i < boardSize * boardSize; i++)
        {
            JButton tempButton = new JButton(String.valueOf(i));

            gridPanel.add(tempButton);


            int finalI = i + 1;
            tempButton.addActionListener(e -> {
                clientLogic.sendMessage(String.valueOf(finalI));
            });
        }

        return gridPanel;

    }

    void setupUI()
    {
        frame = new JFrame("Checkers board");
        frame.setSize(new Dimension(400, 400));

        announmentButton = new JButton("Button");
        announmentButton.setPreferredSize(new Dimension(65, 50));

        //windowFrame.add(announmentButton);

        frame.add(buildBoardGrid(8));

    }

    void addListener()
    {
        announmentButton.addActionListener(e -> {
            System.out.println("gui: button pressed.");

            clientLogic.sendMessage("announcing new stuff...");

        });
    }


    @Override
    public void MovePiece(AbstractPiece piece, Position newPosition) {

    }

    @Override
    public AbstractPiece getPieceAt(Position position) {
        return null;
    }

    @Override
    public boolean isPositionOccupied(Position position) {
        return false;
    }


    public static void main(String[] args) {

        ClientLogic clientLogic1 = new ClientLogic("localhost", 5000);

        BoardView boardWindow = new BoardView(clientLogic1);

        boolean connected = clientLogic1.connect("boardTest");

        if (!connected) {
            // Handle connection failure - maybe close the initial window
            System.err.println("Initial connection failed. Exiting.");
            // Ensure GUI resources are cleaned up if connection fails immediately
            SwingUtilities.invokeLater(() -> {
                if (boardWindow.frame != null) {
                    boardWindow.frame.dispose();
                }
            });
            System.exit(1); // Exit with error status
        }
    }
}
