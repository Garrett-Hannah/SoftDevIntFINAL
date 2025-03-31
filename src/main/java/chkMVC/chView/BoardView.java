package chkMVC.chView;

import chkMVC.chController.BoardController;
import chkMVC.chModel.Checkers.BoardModel;
import chkMVC.chModel.Checkers.Pieces.AbstractPiece;
import chkMVC.chModel.Checkers.Position;
import chkNetwork.Client.ClientLogic;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class BoardView {


    private ClientLogic clientLogic;
    public JFrame frame;
    private JButton announmentButton;
    private BoardModel boardModel;

    BoardController controller;

    ArrayList<JButton> boardButtons;


    BoardView(ClientLogic clientLogic, BoardModel boardModel)
    {
        this.clientLogic = clientLogic;
        this.boardModel = boardModel;
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

        frame.add(buildBoardGrid(boardModel.getHeight()));

    }

    void addListener()
    {
        announmentButton.addActionListener(e -> {
            System.out.println("gui: button pressed.");

            clientLogic.sendMessage("announcing new stuff...");

        });
    }


    public static void main(String[] args) {

        ClientLogic clientLogic1 = new ClientLogic("localhost", 5000);

        BoardModel boardModel = new BoardModel(8);

        BoardView boardWindow = new BoardView(clientLogic1, boardModel);

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
