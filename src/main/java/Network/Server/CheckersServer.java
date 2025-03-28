package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class CheckersServer {
    private static final Set<ClientHandler> clientHandlers = new HashSet<>();
    private static final Map<String, ClientHandler> clients = new HashMap<>();

    public static void main(String[] args) throws IOException {
        final int PORT = 5000;
        System.out.println("Chat server started at port " + PORT);
        ServerSocket serverSocket = new ServerSocket(PORT);

        while (true) {
            new ClientHandler(serverSocket.accept()).start();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        CheckersServer server;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                //Used to input from users.
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //Used to output to users.
                out = new PrintWriter(socket.getOutputStream(), true);

                // Prompt client for username
                username = in.readLine();

                //TODO: add to clients
                ClientHandler clientHandler = new ClientHandler(this.socket);
                synchronized (clients)
                {
                    clients.put(username, clientHandler);
                }



                // Send list of connected clients and their names
                StringBuilder clientsList = new StringBuilder("Connected users: \n");
                // TODO: send list to client

                synchronized (clients)
                {
                    for(String user : clients.keySet())
                    {
                        clientsList.append(user + "\n");
                    }
                }

                out.println(clientsList.toString());




                // Notify other clients about new user
                synchronized (clientHandlers) {
                    for (ClientHandler handler : clientHandlers) {
                        handler.out.println(username + " has joined the chat!");
                    }
                }

                synchronized (clientHandlers) {
                    clientHandlers.add(this);
                }

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(username + ": " + message);
                    // TODO: broadcast message

                    synchronized (clientHandlers)
                    {
                        for(ClientHandler handler : clientHandlers)
                        {
                            handler.out.println(username + ": " + message);
                        }

                    }


                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientHandlers) {
                    clientHandlers.remove(this);
                }
                synchronized (clients) {
                    clients.remove(username);
                }

                // Notify others when a client leaves
                synchronized (clientHandlers) {
                    for (ClientHandler handler : clientHandlers) {
                        handler.out.println(username + " has left the chat.");
                    }
                }
            }
        }
    }


}
