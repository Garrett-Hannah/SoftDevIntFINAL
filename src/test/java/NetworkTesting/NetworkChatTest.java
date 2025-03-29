package NetworkTesting;

import chNetwork.Server.ServerLogic;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors; // For filtering/checking lists

//Test Some basic functionality with the test for the network.
public class NetworkChatTest {

    //Set the port and host.
    private static final int PORT = 5000; // Use a potentially different port for tests
    private static final String HOST = "localhost";

    //Keep track of the server.
    private static ServerLogic server;
    private static Thread serverThread; // Keep track of the server thread

    //Initialize the server because this is kind of important for everything
    //(no server = no anything >:()
    @BeforeAll
    static void startServer() throws IOException {
        System.out.println("Starting server for tests on port " + PORT + "...");

        // This starts the server in another thread so we can continue.
        server = new ServerLogic(PORT);
        server.start();

        // Wait a moment for the server socket to bind and start listening.
        // This is crucial to avoid "Connection refused" errors in the first test.
        try {
            Thread.sleep(1000); // Increased sleep to ensure server is ready
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for server to start.");
        }
        System.out.println("Server should be running.");
    }

    //Afterwards we can stop the server.
    @AfterAll
    static void stopServer() {
        System.out.println("Stopping server...");
        if (server != null) {
            server.stop(); // Signal the server to stop accepting and close sockets
        }
        if (serverThread != null) {
            // Optionally wait for the server thread to finish, with a timeout
            try {
                serverThread.join(1000); // Wait up to 1 second for the server thread to die
                if (serverThread.isAlive()) {
                    System.err.println("Server thread did not stop gracefully, interrupting...");
                    serverThread.interrupt(); // Force interruption if it didn't stop
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Interrupted while waiting for server thread to stop.");
                serverThread.interrupt(); // Ensure it's interrupted
            }
        }
        System.out.println("Server stopped.");
        // Add a small delay AFTER stopping to release the port fully before next potential test run
        try { Thread.sleep(500); } catch (InterruptedException e) {}
    }

    // TestClient remains largely the same, but ensure close is robust
    private static class TestClient implements AutoCloseable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        final String username; // Make username final

        TestClient(String username) throws IOException {
            this.username = username;
            // Add retry logic for connection refused, just in case server isn't instantly ready
            int attempts = 0;
            while(true) {
                try {
                    socket = new Socket(); // Create unbound socket
                    // Set a connection timeout
                    socket.connect(new InetSocketAddress(HOST, PORT), 1000); // 1 second timeout
                    break; // Connected successfully
                } catch (IOException e) {
                    attempts++;
                    if (attempts >= 5) { // Max 5 attempts
                        System.err.println("Failed to connect client " + username + " after " + attempts + " attempts.");
                        throw e; // Rethrow after max attempts
                    }
                    System.out.println("Connection attempt " + attempts + " failed for " + username + ", retrying...");
                    try { Thread.sleep(200 * attempts); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); throw new IOException("Interrupted during connect retry", ie); }
                }
            }

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Send username immediately
            out.println(username);
            System.out.println("Client " + username + " connected and sent username.");
        }

        void sendMessage(String message) {
            if (out != null && !socket.isClosed()) {
                System.out.println(username + " sending: " + message);
                out.println(message);
            } else {
                System.err.println("Warning: Attempted to send message on closed socket for " + username);
            }
        }

        // Reads a single line with a specific timeout.
        String readLineWithTimeout(long timeoutMillis) throws IOException, TimeoutException {
            long deadline = System.currentTimeMillis() + timeoutMillis;
            while (System.currentTimeMillis() < deadline) {
                if (in.ready()) {
                    String line = in.readLine();
                    if (line == null) { // End of stream reached (server likely closed connection)
                        throw new SocketException("Server closed connection while reading.");
                    }
                    System.out.println(username + " received: " + line);
                    return line;
                }
                try {
                    Thread.sleep(50); // Poll interval
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while waiting to read", e);
                }
            }
            throw new TimeoutException("Timeout waiting for message from server for " + username);
        }

        @Override
        public void close() throws IOException {
            System.out.println("Closing client: " + username);
            // Close resources in reverse order of creation
            // Closing the socket should close the associated streams automatically,
            // but closing them explicitly first can sometimes be safer.
            try { if (out != null) out.close(); } catch (Exception e) { /* Ignore */ }
            try { if (in != null) in.close(); } catch (Exception e) { /* Ignore */ }
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing socket for " + username + ": " + e.getMessage());
                // Don't rethrow from close() if possible, but log it.
            } finally {
                // Nullify to prevent reuse
                out = null;
                in = null;
                socket = null;
            }
            System.out.println("Client " + username + " closed.");
        }
    }

    /**
     * Helper method to receive all available messages within a timeout.
     * This reads messages until a TimeoutException occurs on readLineWithTimeout,
     * or until the overall deadline is hit.
     *
     * @param client The TestClient to read from.
     * @param totalTimeoutMillis The maximum time to wait for messages in total.
     * @return A List of messages received within the timeout.
     */
    private List<String> receiveAvailableMessages(TestClient client, long totalTimeoutMillis) throws IOException {
        List<String> received = new ArrayList<>();
        long deadline = System.currentTimeMillis() + totalTimeoutMillis;
        System.out.println("Receiving messages for " + client.username + " (timeout: " + totalTimeoutMillis + "ms)...");

        while (System.currentTimeMillis() < deadline) {
            try {
                // Use a shorter timeout for individual reads to make it responsive
                long remainingTime = deadline - System.currentTimeMillis();
                if (remainingTime <= 0) break;
                // Use a small read timeout (e.g., 100ms) to check if data is there
                String line = client.readLineWithTimeout(Math.min(remainingTime, 200));
                received.add(line);
                // Continue reading immediately if successful
            } catch (TimeoutException e) {
                // This is expected when no more messages are immediately available.
                // We break the loop as we've consumed all currently available messages.
                // System.out.println("Read timeout for " + client.username + ", finished receiving available messages.");
                break;
            } catch (SocketException se) {
                System.out.println("SocketException for " + client.username + " while receiving: " + se.getMessage() + ". Assuming disconnected.");
                break; // Stop reading if socket is closed
            }
            // No need for sleep here, readLineWithTimeout handles waiting
        }
        System.out.println("Finished receiving for " + client.username + ". Got " + received.size() + " messages.");
        return received;
    }

    // Give clients a moment to connect and server to process
    private void waitForNetwork() {
        try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt();}
    }


    @Test
    @Timeout(10) // Overall test timeout
    void testClientConnectAndJoinMessage() throws IOException {
        try (TestClient client1 = new TestClient("Alice");
             TestClient client2 = new TestClient("Bob")) {

            waitForNetwork(); // Allow time for connections and initial messages

            // Alice receives initial messages (likely just her own user list initially)
            List<String> aliceInitialMessages = receiveAvailableMessages(client1, 1500);
            // Check if Alice got a user list message after connecting
            assertTrue(aliceInitialMessages.stream().anyMatch(s -> s.contains("Connected users:")),
                    "Alice should receive a user list upon joining.");


            // Bob receives initial messages (should include Alice in the list)
            List<String> bobInitialMessages = receiveAvailableMessages(client2, 1500);
            assertTrue(bobInitialMessages.stream().anyMatch(s -> s.contains("Connected users:") && s.contains("Alice") && s.contains("Bob")),
                    "Bob's user list should contain Alice and Bob. Got: " + bobInitialMessages);


            // Now, check if Alice received the notification about Bob joining.
            // This might have arrived during the initial read or might arrive shortly after.
            List<String> aliceMoreMessages = receiveAvailableMessages(client1, 1000); // Read any further messages
            List<String> allAliceMessages = new ArrayList<>(aliceInitialMessages);
            allAliceMessages.addAll(aliceMoreMessages);

            String expectedJoinMsg = "Bob has joined the chat!";
            assertTrue(allAliceMessages.stream().anyMatch(s -> s.equals(expectedJoinMsg)),
                    "Alice did not receive Bob's join message. Received: " + allAliceMessages);

            // Optional: Check Bob *doesn't* get Alice's join message *after* his initial list
            // This depends heavily on server implementation (when join is broadcast)
            // List<String> bobMoreMessages = receiveAvailableMessages(client2, 500);
            // assertFalse(bobMoreMessages.stream().anyMatch(s -> s.contains("Alice has joined")), "Bob should not receive Alice's join message again");

        }
    }

    @Test
    @Timeout(10)
    void testMessageBroadcast() throws IOException {
        try (TestClient client1 = new TestClient("Charlie");
             TestClient client2 = new TestClient("David")) {

            waitForNetwork(); // Allow time for connections and join broadcasts

            // Consume any initial messages (user lists, join notifications)
            receiveAvailableMessages(client1, 1000);
            receiveAvailableMessages(client2, 1000);

            // Charlie sends a message
            String messageToSend = "Hello everyone!";
            client1.sendMessage(messageToSend);

            waitForNetwork(); // Allow time for broadcast propagation

            // Check messages received AFTER sending
            List<String> charlieReceived = receiveAvailableMessages(client1, 1000);
            List<String> davidReceived = receiveAvailableMessages(client2, 1000);

            String expectedBroadcast = "Charlie: " + messageToSend;

            // Assert that the broadcast message is present in the messages received by *both* clients
            assertTrue(charlieReceived.contains(expectedBroadcast),
                    "Charlie didn't receive own broadcast. Received: " + charlieReceived);
            assertTrue(davidReceived.contains(expectedBroadcast),
                    "David didn't receive Charlie's broadcast. Received: " + davidReceived);
        }
    }

    @Test
    @Timeout(10)
    void testClientLeaveMessage() throws IOException {
        TestClient client1 = null;
        try {
            client1 = new TestClient("Eve");
            waitForNetwork(); // Eve connects

            // Consume Eve's initial messages (user list)
            receiveAvailableMessages(client1, 1000);

            String client2Username = "Frank";
            try (TestClient client2 = new TestClient(client2Username)) {
                waitForNetwork(); // Frank connects, server broadcasts join

                // Consume messages resulting from Frank's join
                // Eve should get "Frank joined", Frank gets user list
                receiveAvailableMessages(client1, 1000); // Clear Eve's buffer (likely contains Frank's join)
                receiveAvailableMessages(client2, 1000); // Clear Frank's buffer (user list)

                // Frank leaves when this block ends
                System.out.println("Frank (client2) is about to leave...");
            } // client2.close() is called here automatically

            waitForNetwork(); // Allow server time to process disconnect and broadcast leave msg

            // Client 1 (Eve) should receive the leave message
            List<String> eveMessagesAfterLeave = receiveAvailableMessages(client1, 2000); // Allow more time for leave msg

            String expectedLeaveMsg = client2Username + " has left the chat.";
            assertTrue(eveMessagesAfterLeave.contains(expectedLeaveMsg),
                    "Client 1 (Eve) did not receive leave message for Frank. Received: " + eveMessagesAfterLeave);

        } finally {
            if (client1 != null) {
                client1.close(); // Ensure client1 is closed even on failure
            }
        }
    }
}