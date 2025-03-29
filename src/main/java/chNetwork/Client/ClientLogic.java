package chNetwork.Client;

import chNetwork.CLIENT_REQUEST_CODES;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientLogic {

    private final String host;
    private final int port;
    private String username;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ChatView view; // Reference to the GUI
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor(); // For listener thread

    public ClientLogic(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // Setter for the View (GUI)
    public void setView(ChatView view) {
        this.view = view;
    }

    // Attempt to connect to the server
    public boolean connect(String username) {
        if (isRunning.get()) {
            System.err.println("Already connected or connecting.");
            return true; // Or false, depending on desired behavior
        }
        this.username = username;
        if (this.username == null || this.username.trim().isEmpty()) {
            if (view != null) view.showErrorMessage("Login Error", "Username cannot be empty.");
            return false;
        }

        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send username immediately
            out.println(this.username);

            // Start the listener thread using an ExecutorService
            isRunning.set(true);
            networkExecutor.submit(this::listenToServer); // Pass method reference

            if (view != null) {
                view.setWindowTitle(this.username + "'s Chat Client - Connected");
            }
            System.out.println("Connected successfully as " + this.username);
            return true;

        } catch (UnknownHostException e) {
            if (view != null) view.showErrorMessage("Connection Error", "Unknown host: " + host);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            if (view != null)
                view.showErrorMessage("Connection Error", "Couldn't connect to server: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Background task to listen for messages from the server
    private void listenToServer() {
        try {
            String messageFromServer;
            while (isRunning.get() && (messageFromServer = in.readLine()) != null) {
                // Process different types of messages from server
                // For now, just display everything
                if (view != null) {
                    // Ensure GUI updates happen on the Event Dispatch Thread
                    final String finalMessage = messageFromServer; // Need final variable for lambda
                    javax.swing.SwingUtilities.invokeLater(() -> view.appendMessage(finalMessage));
                } else {
                    System.out.println("Received (no view): " + messageFromServer);
                }

                // Example: Check for specific server messages
                if ("ERROR: Username".equals(messageFromServer.substring(0, Math.min(messageFromServer.length(), 16)))) {
                    String finalMessageFromServer = messageFromServer;
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        view.showErrorMessage("Login Failed", finalMessageFromServer);
                        disconnect(); // Disconnect if username is invalid/taken
                        view.closeWindow(); // Maybe close the window too
                    });
                }
            }
        } catch (SocketException e) {
            if (isRunning.get()) { // Only show error if we didn't intentionally disconnect
                System.err.println("SocketException in listener: " + e.getMessage() + " (Likely server disconnected)");
                if (view != null)
                    javax.swing.SwingUtilities.invokeLater(() -> view.showErrorMessage("Connection Lost", "Lost connection to the server."));
            } else {
                System.out.println("Listener stopped due to intended disconnect.");
            }
        } catch (IOException e) {
            if (isRunning.get()) {
                System.err.println("IOException in listener: " + e.getMessage());
                e.printStackTrace();
                if (view != null)
                    javax.swing.SwingUtilities.invokeLater(() -> view.showErrorMessage("Network Error", "Error reading from server: " + e.getMessage()));
            }
        } finally {
            // Ensure cleanup happens even if loop exits unexpectedly
            disconnect(); // Clean up resources if listener thread ends
            System.out.println("Server listener thread finished.");
        }
    }

    // Send a standard chat message
    public void sendMessage(String message) {
        if (out != null && isRunning.get() && message != null && !message.trim().isEmpty()) {
            out.println(message);
            if (view != null) {
                javax.swing.SwingUtilities.invokeLater(() -> view.clearInputField());
            }
        } else if (!isRunning.get()) {
            if (view != null)
                javax.swing.SwingUtilities.invokeLater(() -> view.showErrorMessage("Send Error", "Not connected to the server."));
        }
    }

    // Send a formatted command (implementation based on your previous request)
    public void sendCommand(CLIENT_REQUEST_CODES request, List<String> args) {
        if (out != null && isRunning.get()) {
            StringBuilder commandString = new StringBuilder();
            commandString.append("/").append(request.name()); // Assuming commands start with '/'
            if (args != null && !args.isEmpty()) {
                for (String arg : args) {
                    // Basic argument joining, might need better escaping if args can contain spaces
                    commandString.append(" ").append(arg);
                }
            }
            System.out.println("Sending command: " + commandString); // Log command
            out.println(commandString.toString());
        } else {
            System.err.println("Cannot send command - not connected.");
            if (view != null)
                javax.swing.SwingUtilities.invokeLater(() -> view.showErrorMessage("Command Error", "Not connected, cannot send command."));
        }
    }


    // Disconnect from the server and clean up resources
    public void disconnect() {
        if (!isRunning.compareAndSet(true, false)) {
            return; // Already disconnected or not connected
        }
        System.out.println("Disconnecting...");

        networkExecutor.shutdown(); // Signal listener thread to stop accepting tasks

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // This will cause readLine() in listener to throw SocketException
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        } finally {
            // Nullify resources
            in = null;
            out = null;
            socket = null;
            if (view != null) {
                javax.swing.SwingUtilities.invokeLater(() -> view.setWindowTitle(this.username + "'s Chat Client - Disconnected"));
            }
            System.out.println("Disconnected.");
        }
        try {
            // Wait for the listener thread to terminate
            if (!networkExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                networkExecutor.shutdownNow(); // Force stop if it doesn't finish
            }
        } catch (InterruptedException e) {
            networkExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}