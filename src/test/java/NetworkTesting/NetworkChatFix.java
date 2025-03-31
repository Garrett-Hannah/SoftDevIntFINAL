package NetworkTesting;

import chkNetwork.Server.ServerLogic;
import org.junit.jupiter.api.*;
import org.opentest4j.AssertionFailedError; // Import for specific assertion error

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NetworkChatFix {

    private static final int PORT = 5001; // Changed port slightly just in case 5000 is stuck from previous runs
    private static final String HOST = "localhost";
    private static final long CONNECT_TIMEOUT_MS = 2000; // Timeout for client connection attempts
    private static final long READ_TIMEOUT_MS = 3000; // Default timeout for waiting for a specific message
    private static final long SERVER_START_TIMEOUT_MS = 5000; // Max time to wait for server to start

    private static ServerLogic server;

    @BeforeAll
    static void startServer() throws IOException {
        System.out.println("Starting server for tests on port " + PORT + "...");
        server = new ServerLogic(PORT);
        // Ensure server view is null or a dummy for tests if ServerLogic requires it non-null
        // server.setView(new DummyServerView()); // If needed
        server.start(); // Starts the server accept loop in its own thread

        // ** Wait for server to be ready **
        long startTime = System.currentTimeMillis();
        boolean serverReady = false;
        while (System.currentTimeMillis() - startTime < SERVER_START_TIMEOUT_MS) {
            try (Socket testSocket = new Socket()) {
                // Try to connect to the server port
                testSocket.connect(new InetSocketAddress(HOST, PORT), 200); // Short connect timeout
                serverReady = true; // Connection successful
                System.out.println("Server socket is ready.");
                break; // Exit loop
            } catch (IOException e) {
                // Connection failed, server likely not ready yet, wait and retry
                System.out.println("Waiting for server socket to bind...");
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    fail("Interrupted while waiting for server to start.");
                }
            }
        }

        if (!serverReady) {
            fail("Server did not become ready within " + SERVER_START_TIMEOUT_MS + "ms.");
        }
        System.out.println("Server started and is ready for connections.");
    }

    @AfterAll
    static void stopServer() {
        System.out.println("Stopping server...");
        if (server != null && server.isRunning()) {
            server.stop(); // Signal the server to stop
        } else {
            System.out.println("Server was null or not running.");
        }

        // Add a delay AFTER stopping to help release the port before next test class runs
        // This is still somewhat heuristic. Running tests in separate processes is more robust.
        try {
            System.out.println("Waiting briefly after server stop...");
            Thread.sleep(500); // Adjust if needed
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Server stop process complete.");
        server = null; // Help GC
    }

    // TestClient class remains mostly the same, but constructor uses defined timeout
    private static class TestClient implements AutoCloseable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        final String username;

        TestClient(String username) throws IOException {
            this.username = username;
            System.out.println("Attempting to connect client: " + username);
            int attempts = 0;
            while (true) {
                try {
                    socket = new Socket();
                    // Use the connection timeout
                    socket.connect(new InetSocketAddress(HOST, PORT), (int) CONNECT_TIMEOUT_MS);
                    // Set a read timeout on the socket itself for blocking readLine calls
                    socket.setSoTimeout((int)READ_TIMEOUT_MS + 1000); // Slightly longer than await helpers
                    System.out.println("Client " + username + " socket connected.");
                    break;
                } catch (IOException e) {
                    attempts++;
                    if (attempts >= 3) { // Reduced retries
                        System.err.println("Failed to connect client " + username + " after " + attempts + " attempts: " + e.getMessage());
                        throw e;
                    }
                    System.out.println("Connection attempt " + attempts + " failed for " + username + ", retrying...");
                    try {
                        Thread.sleep(200 * attempts);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during connect retry for " + username, ie);
                    }
                }
            }

            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // Send username immediately
                out.println(username);
                System.out.println("Client " + username + " connected and sent username.");
            } catch (IOException e) {
                System.err.println("Error getting streams or sending username for " + username);
                // Clean up socket if streams fail
                try { socket.close(); } catch (IOException ce) { /* ignore closing error */ }
                throw e; // Re-throw original error
            }
        }

        void sendMessage(String message) {
            if (out != null && socket != null && !socket.isClosed()) {
                System.out.println(username + " sending: " + message);
                out.println(message);
                // Check for PrintWriter errors (less common but possible)
                if (out.checkError()) {
                    System.err.println("PrintWriter error occurred for client " + username);
                    // Consider throwing an exception or handling the error
                }
            } else {
                System.err.println("Warning: Attempted to send message on closed or null socket/stream for " + username);
            }
        }

        // Reads lines until one containing the substring is found, or timeout.
        String awaitMessageContaining(String substring, long timeoutMillis) throws IOException, TimeoutException {
            long deadline = System.currentTimeMillis() + timeoutMillis;
            System.out.println(username + " waiting for message containing: \"" + substring + "\" (timeout: " + timeoutMillis + "ms)");
            StringBuilder receivedLines = new StringBuilder(); // Keep track of lines read during wait

            while (System.currentTimeMillis() < deadline) {
                try {
                    // Use a read timeout for the blocking call
                    String line = readLineWithSocketTimeout(); // Uses socket's SO_TIMEOUT
                    if (line != null) {
                        System.out.println(username + " received line: " + line);
                        receivedLines.append(line).append("\n");
                        if (line.contains(substring)) {
                            System.out.println(username + " FOUND message containing: \"" + substring + "\"");
                            return line;
                        }
                    } else {
                        // readLine returning null usually means end-of-stream (server disconnected)
                        throw new SocketException("Server disconnected (read null) while waiting for message containing: " + substring);
                    }
                } catch (SocketTimeoutException e) {
                    // This is expected if the read times out based on SO_TIMEOUT
                    // Continue loop to check overall deadline
                    System.out.println(username + " -> read timed out, continuing wait..."); // More informative
                } catch (IOException e) {
                    System.err.println(username + " IOException while waiting for message: " + e.getMessage());
                    throw e; // Re-throw other IO exceptions
                }
                // Check remaining time *before* sleeping
                if (System.currentTimeMillis() >= deadline) {
                    break;
                }
                // Optional short sleep to prevent tight loop if readLine returns quickly without matching
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            System.err.println(username + " timed out waiting for message containing: \"" + substring + "\"");
            System.err.println(username + " --- Lines received during wait ---\n" + receivedLines + "---------------------------------");
            throw new TimeoutException("Client " + username + " timed out after " + timeoutMillis + "ms waiting for message containing: \"" + substring + "\"");
        }

        // Reads lines until one exactly matching the string is found, or timeout.
        String awaitExactMessage(String exactMessage, long timeoutMillis) throws IOException, TimeoutException {
            long deadline = System.currentTimeMillis() + timeoutMillis;
            System.out.println(username + " waiting for exact message: \"" + exactMessage + "\" (timeout: " + timeoutMillis + "ms)");
            StringBuilder receivedLines = new StringBuilder();

            while (System.currentTimeMillis() < deadline) {
                try {
                    String line = readLineWithSocketTimeout();
                    if (line != null) {
                        System.out.println(username + " received line: " + line);
                        receivedLines.append(line).append("\n");
                        if (line.equals(exactMessage)) {
                            System.out.println(username + " FOUND exact message: \"" + exactMessage + "\"");
                            return line;
                        }
                    } else {
                        throw new SocketException("Server disconnected (read null) while waiting for exact message: " + exactMessage);
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println(username + " -> read timed out, continuing wait...");
                } catch (IOException e) {
                    System.err.println(username + " IOException while waiting for message: " + e.getMessage());
                    throw e;
                }
                if (System.currentTimeMillis() >= deadline) {
                    break;
                }
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            System.err.println(username + " timed out waiting for exact message: \"" + exactMessage + "\"");
            System.err.println(username + " --- Lines received during wait ---\n" + receivedLines + "---------------------------------");
            throw new TimeoutException("Client " + username + " timed out after " + timeoutMillis + "ms waiting for exact message: \"" + exactMessage + "\"");
        }

        // Helper to read using the socket's SO_TIMEOUT
        private String readLineWithSocketTimeout() throws IOException, SocketTimeoutException {
            if (in == null) throw new IOException("BufferedReader is null for " + username);
            // This call will block up to the SO_TIMEOUT value set on the socket
            return in.readLine();
                try {
        }


        // Reads and discards messages for a duration, useful for clearing initial messages.
        void consumeMessages(long durationMillis) throws IOException {
            long deadline = System.currentTimeMillis() + durationMillis;
            System.out.println(username + " consuming messages for " + durationMillis + "ms...");
            int count = 0;
            while (System.currentTimeMillis() < deadline) {
                    // Use a short timeout for individual reads within the consumption window
                    String line = readLineWithTimeout(100); // Read with short timeout
                    if(line != null) {
                        System.out.println(username + " consumed: " + line);
                        count++;
                    }
                    // If readLine returns without timeout, try reading again immediately
                } catch (TimeoutException e) {
                    // Expected if no message arrives within the short read timeout
                    // Continue consuming until the main duration deadline
                    if (System.currentTimeMillis() >= deadline) break;
                    try { Thread.sleep(50); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                } catch (SocketException se) {
                    System.out.println(username + " disconnected while consuming messages: " + se.getMessage());
                    break; // Stop consuming if disconnected
                }
            }
            System.out.println(username + " finished consuming messages. Consumed " + count + " lines.");
        }


        @Override
        public void close() { // Changed return type to void, AutoCloseable doesn't require IOException
            System.out.println("Closing client: " + username);
            // Close resources in reverse order of creation
            if (out != null) {
                out.close(); // Closing PrintWriter usually doesn't throw IOException
            }
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                System.err.println("Error closing BufferedReader for " + username + ": " + e.getMessage());
            }
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing socket for " + username + ": " + e.getMessage());
            } finally {
                out = null;
                in = null;
                socket = null;
            }
            System.out.println("Client " + username + " closed.");
        }
    }

    // Convenience method to assert that a specific message is received
    private void assertClientReceives(TestClient client, String expectedSubstring, long timeout) {
        try {
            client.awaitMessageContaining(expectedSubstring, timeout);
            // If await succeeds, the assertion passes implicitly
            System.out.println("Assertion PASSED: " + client.username + " received message containing \"" + expectedSubstring + "\"");
        } catch (IOException | TimeoutException e) {
            // If await fails, fail the test explicitly with a clear message
            fail("Client " + client.username + " did NOT receive message containing \"" + expectedSubstring + "\" within " + timeout + "ms. Cause: " + e.getMessage(), e);
        }
    }
    private void assertClientReceivesExact(TestClient client, String exactMessage, long timeout) {
        try {
            client.awaitExactMessage(exactMessage, timeout);
            System.out.println("Assertion PASSED: " + client.username + " received exact message \"" + exactMessage + "\"");
        } catch (IOException | TimeoutException e) {
            fail("Client " + client.username + " did NOT receive exact message \"" + exactMessage + "\" within " + timeout + "ms. Cause: " + e.getMessage(), e);
        }
    }


    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS) // Slightly longer overall test timeout
    void testClientConnectAndJoinMessage() {
        System.out.println("\n--- Starting testClientConnectAndJoinMessage ---");
        try (TestClient client1 = new TestClient("Alice");
             TestClient client2 = new TestClient("Bob")) {

            // Client 1 (Alice) connects. Wait for her initial messages (user list, roles)
            System.out.println("Alice connected. Waiting for her initial messages...");
            assertClientReceives(client1, "Connected users", READ_TIMEOUT_MS); // Wait for user list
            // Optionally consume other role messages if needed/predictable
            client1.consumeMessages(500); // Consume any other messages quickly


            // Client 2 (Bob) connects.
            System.out.println("Bob connected. Waiting for his initial messages...");
            // Bob should get a user list including Alice
            assertClientReceives(client2, "Connected users", READ_TIMEOUT_MS);
            assertClientReceives(client2, "Alice", READ_TIMEOUT_MS); // Ensure Alice is in Bob's list
            client2.consumeMessages(500);

            // Now, specifically wait for Alice to receive the notification about Bob joining.
            System.out.println("Waiting for Alice to receive Bob's join message...");
            String expectedJoinMsg = "Bob has joined the chat!";
            assertClientReceivesExact(client1, expectedJoinMsg, READ_TIMEOUT_MS); // Use exact match

        } catch (IOException e) {
            fail("IOException during testClientConnectAndJoinMessage: " + e.getMessage(), e);
        }
        System.out.println("--- Finished testClientConnectAndJoinMessage ---");
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testMessageBroadcast() {
        System.out.println("\n--- Starting testMessageBroadcast ---");
        try (TestClient client1 = new TestClient("Charlie");
             TestClient client2 = new TestClient("David")) {

            // Consume initial connection messages for both clients
            System.out.println("Consuming initial messages for Charlie & David...");
            client1.consumeMessages(1000);
            client2.consumeMessages(1000);
            System.out.println("Finished consuming initial messages.");


            // Charlie sends a message
            String messageToSend = "Hello from Charlie!";
            client1.sendMessage(messageToSend);
            System.out.println("Charlie sent message. Waiting for broadcast...");


            String expectedBroadcast = "Charlie: " + messageToSend;

            // Assert that David receives the broadcast.
            assertClientReceivesExact(client2, expectedBroadcast, READ_TIMEOUT_MS);

            // Assert that Charlie also receives the broadcast (server echoes).
            // Note: Your ServerLogic.broadcastMessage currently excludes the sender.
            // If you WANT echo, modify ServerLogic. If not, this assertion should fail or be removed.
            // assertClientReceivesExact(client1, expectedBroadcast, READ_TIMEOUT_MS);
            // Let's assume no echo for now based on ServerLogic code:
            System.out.println("Skipping check for echo to sender (Charlie) based on ServerLogic implementation.");


        } catch (IOException e) {
            fail("IOException during testMessageBroadcast: " + e.getMessage(), e);
        }
        System.out.println("--- Finished testMessageBroadcast ---");
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS) // Longer timeout for leave test
    void testClientLeaveMessage() {
        System.out.println("\n--- Starting testClientLeaveMessage ---");
        TestClient client1 = null; // Keep client1 outside try-with-resources
        String client1Username = "Eve";
        String client2Username = "Frank";

        try {
            client1 = new TestClient(client1Username);
            System.out.println("Eve connected. Consuming initial messages...");
            client1.consumeMessages(1000); // Consume Eve's initial stuff


            try (TestClient client2 = new TestClient(client2Username)) {
                System.out.println("Frank connected. Waiting for join broadcast to Eve...");
                // **Crucially, wait for Eve to see Frank join BEFORE Frank leaves**
                assertClientReceivesExact(client1, client2Username + " has joined the chat!", READ_TIMEOUT_MS);

                System.out.println("Frank consuming initial messages...");
                client2.consumeMessages(1000); // Consume Frank's initial stuff

                System.out.println("Frank (client2) is about to be closed by try-with-resources...");
                // Frank leaves automatically when this block ends
            } // client2.close() called here

            System.out.println("Frank has left. Waiting for Eve to receive leave message...");
            // Client 1 (Eve) should receive the leave message now that client2 is closed
            String expectedLeaveMsg = client2Username + " has left the chat.";
            assertClientReceivesExact(client1, expectedLeaveMsg, READ_TIMEOUT_MS * 2); // Allow slightly longer for leave propagation

        } catch (IOException e) {
            fail("IOException during testClientLeaveMessage: " + e.getMessage(), e);
        } finally {
            // Ensure client1 is always closed, even if an assertion fails above
            if (client1 != null) {
                client1.close();
            }
        }
        System.out.println("--- Finished testClientLeaveMessage ---");
    }
}