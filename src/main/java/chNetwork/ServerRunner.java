package chNetwork;

import chNetwork.Server.CheckersServerRef;

import java.io.IOException;
import java.util.Scanner;

public class ServerRunner {

    public static void main(String[] args) {
        CheckersServerRef myServer = new CheckersServerRef(5000); // Use a specific port

        try {
            myServer.start(); // Start the server in the background

            System.out.println("Server started. Press Enter to stop.");

            // Keep the main application running, wait for user input to stop
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine(); // Wait for Enter key

        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        } finally {
            System.out.println("Requesting server stop...");
            myServer.stop(); // Stop the server gracefully
            System.out.println("Server should now be stopped.");
        }
    }
}