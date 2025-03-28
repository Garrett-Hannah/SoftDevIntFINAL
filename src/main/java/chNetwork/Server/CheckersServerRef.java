package chNetwork.Server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CheckersServerRef implements Runnable { // Implement Runnable for the main accept loop

    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean isRunning = false; // Flag to control the main loop
    private Thread serverThread; // Thread running the accept loop
    private final ExecutorService clientExecutor; // To manage client handler threads

    // Instance fields instead of static
    private final Set<ClientHandler> clientHandlers = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, ClientHandler> clients = Collections.synchronizedMap(new HashMap<>());

    // Game-specific logic (still needs proper integration)
    private ClientHandler host = null;
    private ClientHandler white = null;
    private ClientHandler black = null;

    public CheckersServerRef(int port) {
        this.port = port;
        // Use a thread pool for client handlers for better resource management
        this.clientExecutor = Executors.newCachedThreadPool();
    }

    // --- Server Lifecycle Methods ---

    public void start() throws IOException {
        if (isRunning) {
            System.out.println("Server is already running on port " + port);
            return;
        }
        System.out.println("Starting Checkers server on port " + port + "...");
        serverSocket = new ServerSocket(port);
        isRunning = true;
        // Start the main accept loop in its own thread
        serverThread = new Thread(this, "CheckersServer-AcceptThread");
        serverThread.start();
        System.out.println("Server started successfully.");
    }

    public void stop() {
        if (!isRunning) {
            System.out.println("Server is not running.");
            return;
        }
        System.out.println("Stopping Checkers server...");
        isRunning = false; // Signal the accept loop to stop

        // Close the server socket - this will interrupt the accept() call
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // Causes SocketException in run() method's accept()
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }

        // Gracefully shut down client threads
        clientExecutor.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!clientExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                clientExecutor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!clientExecutor.awaitTermination(5, TimeUnit.SECONDS))
                    System.err.println("Client handler pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            clientExecutor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }

        // Explicitly close remaining client sockets (redundant if shutdownNow worked)
        // Use a copy to avoid ConcurrentModificationException while iterating and removing
        synchronized (clientHandlers) {
            Set<ClientHandler> handlersCopy = new HashSet<>(clientHandlers);
            for(ClientHandler handler : handlersCopy) {
                handler.closeConnection("Server shutting down");
            }
        }
        clientHandlers.clear(); // Should be empty now
        clients.clear();

        // Wait for the server thread to die
        try {
            if (serverThread != null && serverThread.isAlive()) {
                serverThread.join(1000); // Wait max 1 sec for the thread to finish
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for server thread to stop.");
        }

        System.out.println("Server stopped.");
    }

    // --- Main Server Loop (runs in serverThread) ---

    @Override
    public void run() {
        System.out.println("Server accept loop started. Listening for connections...");
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept(); // Blocking call
                System.out.println("Connection received from " + clientSocket.getRemoteSocketAddress());
                // Create and start a handler for the new client
                ClientHandler handler = new ClientHandler(clientSocket, this); // Pass 'this' server instance
                clientExecutor.submit(handler); // Use executor service
                // handler.start(); // Old way: starting thread directly
            } catch (SocketException e) {
                // Expected when serverSocket.close() is called in stop()
                if (!isRunning) {
                    System.out.println("Server socket closed, accept loop terminating.");
                } else {
                    System.err.println("SocketException in accept loop: " + e.getMessage());
                    // Consider whether to try and recover or shut down
                    // stop(); // Option: Stop server on unexpected socket errors
                }
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("IOException in accept loop: " + e.getMessage());
                    e.printStackTrace();
                    // Consider stopping if the error is critical
                }
            }
        }
        System.out.println("Server accept loop finished.");
    }


    // --- Client Management Methods (Called by ClientHandler) ---

    // Called by ClientHandler *after* username is received
    void registerClient(ClientHandler handler, String username) {
        if (username == null || username.trim().isEmpty()) {
            System.err.println("Attempt to register client with null or empty username.");
            handler.closeConnection("Invalid username provided.");
            return;
        }
        if (clients.containsKey(username)) {
            System.err.println("Username '" + username + "' is already taken.");
            handler.sendMessage("ERROR: Username '" + username + "' is already taken. Please reconnect with a different name.");
            handler.closeConnection("Username taken");
            return;
        }

        synchronized (clients) {
            clients.put(username, handler);
        }
        synchronized (clientHandlers) {
            clientHandlers.add(handler);
        }

        // Assign roles (basic example, needs more logic)
        assignRoles(handler);

        System.out.println(username + " successfully registered.");

        // Send current user list to the new client
        handler.sendUserList();

        // Notify others
        broadcastMessage(username + " has joined the chat!", handler); // Exclude sender
    }

    void unregisterClient(ClientHandler handler) {
        String username = handler.getUsername(); // Get username before removing
        boolean removed = false;
        synchronized (clientHandlers) {
            removed = clientHandlers.remove(handler);
        }
        if (username != null) {
            synchronized (clients) {
                clients.remove(username);
            }
            // Reset roles if the leaving client held one
            if (handler == host) host = null;
            if (handler == white) white = null;
            if (handler == black) black = null;
            // Could re-assign roles here if needed

            System.out.println(username + " unregistered.");
            if (removed) { // Only broadcast leave if they were fully registered
                broadcastMessage(username + " has left the chat.", null); // Send to everyone
            }
        } else {
            System.out.println("An unregistered client disconnected.");
        }


    }

    void broadcastMessage(String message, ClientHandler sender) {
        // Use a snapshot to avoid issues if clientHandlers changes during iteration
        Set<ClientHandler> handlersSnapshot;
        synchronized (clientHandlers) {
            handlersSnapshot = new HashSet<>(clientHandlers);
        }

        System.out.println("Broadcasting: " + message + (sender != null ? " (from " + sender.getUsername() + ")" : " (from Server)"));
        for (ClientHandler handler : handlersSnapshot) {
            if (handler != sender) { // Avoid sending message back to sender unless needed
                handler.sendMessage(message);
            }
        }
    }

    // --- Game Specific Logic Helper ---
    // Needs refinement based on actual game flow requirements
    private synchronized void assignRoles(ClientHandler newHandler) {
        // Simple first-come, first-served assignment
        if (host == null) {
            host = newHandler;
            newHandler.sendMessage("SERVER: You are the host.");
            System.out.println(newHandler.getUsername() + " assigned as host.");
        }

        if (white == null) {
            white = newHandler;
            newHandler.sendMessage("SERVER: You are playing as White.");
            System.out.println(newHandler.getUsername() + " assigned as White.");
        } else if (black == null) {
            black = newHandler;
            newHandler.sendMessage("SERVER: You are playing as Black.");
            System.out.println(newHandler.getUsername() + " assigned as Black.");
        } else {
            newHandler.sendMessage("SERVER: The game is full, you are spectating.");
            System.out.println(newHandler.getUsername() + " is spectating.");
        }
    }

    // --- Getters (Optional, for status checking) ---
    public int getPort() {
        return port;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public Set<String> getConnectedUsernames() {
        // Return a copy to prevent external modification
        synchronized (clients) {
            return new HashSet<>(clients.keySet());
        }
    }


    // --- Inner ClientHandler Class ---
    private static class ClientHandler implements Runnable { // Implement Runnable
        private final Socket socket;
        private final CheckersServerRef serverInstance; // Reference to the parent server
        private PrintWriter out;
        private BufferedReader in;
        private volatile String username; // Make username volatile as it's set after thread start
        private volatile boolean clientRunning = true;

        public ClientHandler(Socket socket, CheckersServerRef serverInstance) {
            this.socket = socket;
            this.serverInstance = serverInstance; // Store the server instance
        }

        public String getUsername() {
            return username;
        }

        // Method to send a message to this specific client
        public void sendMessage(String message) {
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
            sendMessage(clientsList.toString());
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


                // 2. Register with the server *after* getting username
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
            }
            catch (IOException e) {
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
            sendMessage("SERVER: Command '" + command + "' received (not implemented yet).");
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


    // --- Main method (for standalone execution) ---
    public static void main(String[] args) {
        final int DEFAULT_PORT = 5000;
        CheckersServerRef server = new CheckersServerRef(DEFAULT_PORT);
        try {
            server.start();

            // Keep the main thread alive, or add shutdown hooks
            // Example: Add a shutdown hook to stop the server gracefully on Ctrl+C
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutdown hook triggered. Stopping server...");
                server.stop();
            }));

            // Keep main alive indefinitely (or until shutdown hook)
            // server.serverThread.join(); // Alternatively, wait for the server thread itself


        } catch (IOException e) {
            System.err.println("Failed to start server on port " + DEFAULT_PORT + ": " + e.getMessage());
            e.printStackTrace();
        }
        // Note: If start() throws an exception, the shutdown hook won't have anything to stop.
    }
}