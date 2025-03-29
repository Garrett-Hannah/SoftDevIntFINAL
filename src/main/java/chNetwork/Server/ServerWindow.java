package chNetwork.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

//This class takes the implements the functions required in the view...
public class ServerWindow implements ServerView { // Implement the interface

    private JFrame frame;
    private JTextArea textArea;
    private JList<String> activeUsers;

    private final ServerLogic serverLogic; // Reference to the logic class

    // Constructor takes the logic controller
    public ServerWindow(ServerLogic serverLogic) {
        this.serverLogic = serverLogic;

        this.serverLogic.setView(this);

        createAndShowGUI();
    }

    private void createAndShowGUI() {
        // Use SwingUtilities.invokeLater to ensure GUI creation is on the EDT
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Server Client Log"); // Initial title
            textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            JScrollPane scrollPane = new JScrollPane(textArea);



            frame.setLayout(new BorderLayout());
            frame.add(scrollPane, BorderLayout.CENTER);
            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new BorderLayout());
            frame.add(bottomPanel, BorderLayout.SOUTH);

            frame.setSize(400, 300);
            // Change default close operation to notify logic
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    serverLogic.stop();
                    frame.dispose(); // Close the window
                    System.exit(0); // Exit application if this is the main window
                }
            });


            JPanel rightPanel = new JPanel();

            this.activeUsers = new JList<>();

            rightPanel.add(activeUsers);

            frame.add(rightPanel, BorderLayout.EAST);



            frame.setVisible(true);
        });
    }

    // --- Implementation of ChatView Interface ---

    //Push new message onto stack.
    @Override
    public void appendMessage(String message) {
        System.out.println("Appending...... " + message);
        // Ensure updates are on the EDT (already handled if called via SwingUtilities.invokeLater)
        if (SwingUtilities.isEventDispatchThread()) {
            textArea.append(message + "\n");
            // Auto-scroll to bottom
            textArea.setCaretPosition(textArea.getDocument().getLength());
        } else {
            SwingUtilities.invokeLater(() -> appendMessage(message));
        }

        System.out.println("Should have been updated?");
    }

    @Override
    public void showErrorMessage(String title, String message) {
        if (SwingUtilities.isEventDispatchThread()) {
            JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
        } else {
            SwingUtilities.invokeLater(() -> showErrorMessage(title, message));
        }
    }

    @Override
    public void setWindowTitle(String title) {
        if (SwingUtilities.isEventDispatchThread()) {
            frame.setTitle(title);
        } else {
            SwingUtilities.invokeLater(() -> setWindowTitle(title));
        }
    }

    @Override
    public void updateUserList(ArrayList<String> userlist) {

        this.activeUsers.removeAll();

        for(String user: userlist)
        {
            this.activeUsers.add(new JButton(user));
        }

    }
}