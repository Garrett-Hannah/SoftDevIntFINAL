package chNetwork.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Set;

class ClientHandler implements Runnable { // Implement Runnable

    private final Socket socket;
    private final ServerLogic serverInstance; // Reference to the parent server
    private PrintWriter out;
    private BufferedReader in;
    private volatile String username; // Make username volatile as it's set after thread start
    private volatile boolean clientRunning = true;

    public ClientHandler(Socket socket, ServerLogic serverInstance) {
        this.socket = socket;
        this.serverInstance = serverInstance; // Store the server instance
    }

    public String getUsername() {
        return username;
    }

    // Method to send a message to this specific client
    public void sendResponse(String message) {
        if (out != null && clientRunning) { // Check if output stream is ready and running
            out.println(message);
        }
    }

    // Send the list of currently connected users to this client
    public void sendUserList() {
        Set<String> userNames = serverInstance.getConnectedUsernames();
        // Fix the user list string!
        StringBuilder clientsList = new StringBuilder("Connected users (");
        clientsList.append(userNames.size()).append("): "); // Use the actual size
        clientsList.append(String.join(" ", userNames));
        sendResponse(clientsList.toString());
    }


    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 1. Get username
            // Add a timeout for username entry?
            String receivedUsername = in.readLine();
            if (receivedUsername == null) {
                System.out.println("Client disconnected before sending username.");
                return; // Exit run method
            }
            this.username = receivedUsername.trim(); // Set the username for this handler


            serverInstance.registerClient(this, this.username);
            // If registration fails (e.g., duplicate name), registerClient will close the connection


            // 3. Listen for messages from this client
            String message;
            while (clientRunning && (message = in.readLine()) != null) {
                System.out.println(username + ": " + message); // Log server side

                // Process potential commands or broadcast chat
                // TODO: Add command parsing logic here (e.g., "/move", "/resign")
                if (message.startsWith("/")) {
                    handleCommand(message);
                } else {
                    // Broadcast chat message
                    serverInstance.broadcastMessage(username + ": " + message, this);
                }
            }

        } catch (SocketException e) {
            if (!clientRunning) {
                System.out.println("Client socket closed for " + (username != null ? username : "unknown user") + " as requested.");
            } else {
                System.err.println("SocketException for " + (username != null ? username : "unknown user") + ": " + e.getMessage() + " (Likely client disconnected abruptly)");
            }
        } catch (IOException e) {
            if (clientRunning) { // Avoid error message if we closed intentionally
                System.err.println("IOException for client " + (username != null ? username : "unknown user") + ": " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            closeConnection(null); // Ensure cleanup happens
        }
        System.out.println("Client handler finished for: " + (username != null ? username : "unknown user"));
    }

    private void handleCommand(String command) {
        // Basic command handling placeholder
        System.out.println("Received command from " + username + ": " + command);
        sendResponse("SERVER: Command '" + command + "' received (not implemented yet).");
        // Example: if (command.equalsIgnoreCase("/ready")) { serverInstance.markPlayerReady(this); }
    }

    // Gracefully close connection for this client
    public void closeConnection(String reason) {
        if (!clientRunning) return; // Already closing/closed
        clientRunning = false; // Signal loops to stop

        System.out.println("Closing connection for " + (username != null ? username : "unknown user") + (reason != null ? ". Reason: " + reason : ""));

        // Unregister *before* closing socket if possible
        serverInstance.unregisterClient(this);

        try {
            if (socket != null && !socket.isClosed()) {
                // Maybe send a final "goodbye" message before closing?
                // sendMessage("SERVER: Disconnecting. " + (reason != null ? reason : ""));
                socket.close(); // This also closes associated streams (in/out)
            }
        } catch (IOException e) {
            System.err.println("Error closing socket for " + (username != null ? username : "unknown user") + ": " + e.getMessage());
        } finally {
            // Nullify resources
            in = null;
            out = null;
        }
    }
}

