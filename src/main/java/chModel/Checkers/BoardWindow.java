package chModel.Checkers;

import chModel.Checkers.Pieces.AbstractPiece;
import chNetwork.CLIENT_REQUEST_CODES;
import chNetwork.Client.ClientLogic;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.SequencedSet;

public class BoardWindow implements BoardView{



    private ClientLogic clientLogic;

    private JFrame windowFrame;
    private JButton announmentButton;




    BoardWindow(ClientLogic clientLogic)
    {
        this.clientLogic = clientLogic;

        ArrayList<String> commands = new ArrayList<>();

        commands.add("A1");
        commands.add("B2");

        clientLogic.sendCommand(CLIENT_REQUEST_CODES.MOVE_PIECE, commands);

        setupUI();
        addListener();

        windowFrame.setVisible(true);
    }

    JPanel buildBoardGrid(int boardSize)
    {
        JPanel gridPanel = new JPanel();

        gridPanel.setLayout(new GridLayout(boardSize, boardSize));

        for(int i = 0; i < boardSize * boardSize; i++)
        {
            JButton tempButton = new JButton(String.valueOf(i));
            gridPanel.add(tempButton);

            tempButton.addActionListener(e -> {
                clientLogic.sendMessage(e.toString());
            });
        }

        return gridPanel;

    }

    void setupUI()
    {
        windowFrame = new JFrame("Checkers board");
        windowFrame.setSize(new Dimension(200, 200));

        announmentButton = new JButton("Button");
        announmentButton.setPreferredSize(new Dimension(65, 50));

        //windowFrame.add(announmentButton);

        windowFrame.add(buildBoardGrid(8));

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

        clientLogic1.connect("test");

        BoardWindow newWindow = new BoardWindow(clientLogic1);


    }
}
