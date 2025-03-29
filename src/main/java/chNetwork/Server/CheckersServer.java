package chNetwork.Server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CheckersServer implements Runnable { // Implement Runnable for the main accept loop

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
    private ServerWindow view;

    public CheckersServer(int port) {
        this.port = port;
        // Use a thread pool for client handlers for better resource management
        this.clientExecutor = Executors.newCachedThreadPool();
    }


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


    void registerClient(ClientHandler handler, String username) {
        if (username == null || username.trim().isEmpty()) {
            System.err.println("Attempt to register client with null or empty username.");
            handler.closeConnection("Invalid username provided.");
            return;
        }
        if (clients.containsKey(username)) {
            System.err.println("Username '" + username + "' is already taken.");
            handler.sendResponse("ERROR: Username '" + username + "' is already taken. Please reconnect with a different name.");
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
                handler.sendResponse(message);
            }
        }
    }

    private synchronized void assignRoles(ClientHandler newHandler) {
        // Simple first-come, first-served assignment
        if (host == null) {
            host = newHandler;
            newHandler.sendResponse("SERVER: You are the host.");
            System.out.println(newHandler.getUsername() + " assigned as host.");
        }

        if (white == null) {
            white = newHandler;
            newHandler.sendResponse("SERVER: You are playing as White.");
            System.out.println(newHandler.getUsername() + " assigned as White.");
        } else if (black == null) {
            black = newHandler;
            newHandler.sendResponse("SERVER: You are playing as Black.");
            System.out.println(newHandler.getUsername() + " assigned as Black.");
        } else {
            newHandler.sendResponse("SERVER: The game is full, you are spectating.");
            System.out.println(newHandler.getUsername() + " is spectating.");
        }
    }

    public int getPort() {
        return port;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public Set<String> getConnectedUsernames() {
        synchronized (clients) {
            return new HashSet<>(clients.keySet());
        }
    }

    // --- Main method (for standalone execution) ---
    public static void main(String[] args) {
        final int DEFAULT_PORT = 5000;
        CheckersServer server = new CheckersServer(DEFAULT_PORT);

        ServerWindow window = new ServerWindow(server);

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

    public void setView(ServerWindow serverWindow) {this.view = serverWindow;}
}