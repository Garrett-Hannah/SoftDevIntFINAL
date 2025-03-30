package chkNetwork.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

//This class takes the implements the functions required in the view...
public class ChatWindow implements ChatView { // Implement the interface

    private JFrame frame;
    private JTextArea textArea;
    private JTextField textField;
    private JButton sendButton;
    private ClientLogic clientLogic; // Reference to the logic class

    // Constructor takes the logic controller
    public ChatWindow(ClientLogic clientLogic) {
        this.clientLogic = clientLogic;

        this.clientLogic.setView(this);
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        // Use SwingUtilities.invokeLater to ensure GUI creation is on the EDT
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Chat Client"); // Initial title
            textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            JScrollPane scrollPane = new JScrollPane(textArea);

            textField = new JTextField();
            sendButton = new JButton("Send");

            // Action listener now calls the logic's method
            ActionListener sendAction = e -> handleSendAction();
            textField.addActionListener(sendAction);
            sendButton.addActionListener(sendAction);

            frame.setLayout(new BorderLayout());
            frame.add(scrollPane, BorderLayout.CENTER);
            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new BorderLayout());
            bottomPanel.add(textField, BorderLayout.CENTER);
            bottomPanel.add(sendButton, BorderLayout.EAST);
            frame.add(bottomPanel, BorderLayout.SOUTH);

            frame.setSize(400, 300);
            // Change default close operation to notify logic
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    // Tell the logic to disconnect before closing
                    clientLogic.disconnect();
                    frame.dispose(); // Close the window
                    System.exit(0); // Exit application if this is the main window
                }
            });

            frame.setVisible(true);
            textField.requestFocusInWindow(); // Set focus to input field
        });
    }

    // Handles sending text from the input field
    private void handleSendAction() {
        String message = textField.getText();
        if (!message.trim().isEmpty()) {
            // Check if it's a command (e.g., starts with '/') or plain chat
            if (message.startsWith("/")) {
                // Basic command parsing (needs improvement)
                // Example: /move A1 B2 -> sendCommand(MOVE, ["A1", "B2"])
                // This parsing logic might live here or be passed to clientLogic
                System.out.println("Command detected (implement parsing): " + message);
                // clientLogic.parseAndSendCommand(message); // You'd need this method in CheckersClient
                appendMessage("Client: Command handling not fully implemented yet."); // Feedback
                textField.setText("");
            } else {
                // Send as regular chat message via the logic class
                clientLogic.sendMessage(message);
                // Logic class now calls clearInputField via the interface upon success
            }
        }
    }

    // --- Implementation of ChatView Interface ---

    //Push new message onto stack.
    @Override
    public void appendMessage(String message) {
        // Ensure updates are on the EDT (already handled if called via SwingUtilities.invokeLater)
        if (SwingUtilities.isEventDispatchThread()) {
            textArea.append(message + "\n");
            // Auto-scroll to bottom
            textArea.setCaretPosition(textArea.getDocument().getLength());
        } else {
            SwingUtilities.invokeLater(() -> appendMessage(message));
        }
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
    public void clearInputField() {
        if (SwingUtilities.isEventDispatchThread()) {
            textField.setText("");
        } else {
            SwingUtilities.invokeLater(this::clearInputField);
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
    public void closeWindow() {
        if (SwingUtilities.isEventDispatchThread()) {
            frame.dispose();
        } else {
            SwingUtilities.invokeLater(this::closeWindow);
        }
    }


    // --- Main method to launch the application ---
    public static void main(String[] args) {
        // 1. Get username
        String username = JOptionPane.showInputDialog("Enter your username:");
        if (username == null || username.trim().isEmpty()) {
            System.out.println("Username cancelled or empty. Exiting.");
            System.exit(0);
        }

        // 2. Create the logic component
        // Ideally, get host/port from config or args
        ClientLogic clientLogic = new ClientLogic("localhost", 5000);

        // 3. Create the GUI component (View) and link it to the logic
        // The ChatWindow constructor now calls clientLogic.setView(this)
        ChatWindow chatWindow = new ChatWindow(clientLogic);

        // 4. Attempt to connect (after GUI is initialized and linked)
        boolean connected = clientLogic.connect(username);

        if (!connected) {
            // Handle connection failure - maybe close the initial window
            System.err.println("Initial connection failed. Exiting.");
            // Ensure GUI resources are cleaned up if connection fails immediately
            SwingUtilities.invokeLater(() -> {
                if (chatWindow.frame != null) {
                    chatWindow.frame.dispose();
                }
            });
            System.exit(1); // Exit with error status
        }

        // Application is now running, driven by events and the listener thread...
        System.out.println("Application setup complete. GUI visible. Listening for messages.");
    }
}