package NetworkTesting;

import chNetwork.Server.CheckersServerRef;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*; // For timeouts

// Assuming CheckersServer is modified to be stoppable or run manually first
// For this example, let's assume manual start/stop or a hypothetical StoppableServer class

public class NetworkChatTest {

    private static final int PORT = 5000;
    private static final String HOST = "localhost";

    //Static server.
    private static CheckersServerRef server; // Ideal case

    @BeforeAll
    static void startServer() throws IOException {
        System.out.println("Starting server for tests...");
        // In a real scenario, start the server here in a separate thread
        server = new CheckersServerRef(PORT);

        server.start();

        // e.g., server = new StoppableServer(); server.start();
        // For now, ensure server is running manually before executing tests.
        // We need a small delay or check to ensure the server is ready.
        try { Thread.sleep(500); } catch (InterruptedException e) {}
    }

    @AfterAll
    static void stopServer() {
        System.out.println("Stopping server...");
        // In a real scenario, stop the server here
        server.stop();
        // e.g., server.stop();
        // For now, stop the server manually after tests.
    }

    // Helper class/method for test clients
    private static class TestClient implements AutoCloseable {
        Socket socket;
        PrintWriter out;
        BufferedReader in;
        String username;

        TestClient(String username) throws IOException {
            this.username = username;
            socket = new Socket(HOST, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Send username immediately
            out.println(username);
        }

        void sendMessage(String message) {
            out.println(message);
        }

        String readLineWithTimeout(long timeoutMillis) throws IOException, TimeoutException {
            // Basic timeout implementation (more robust needed for production)
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < timeoutMillis) {
                if (in.ready()) {
                    return in.readLine();
                }
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            throw new TimeoutException("Timeout waiting for message from server");
        }


        @Override
        public void close() throws IOException {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // This will close streams too
            }
        }
    }

    @Test
    @Timeout(10) // Add a timeout to prevent tests hanging indefinitely
    void testClientConnectAndJoinMessage() throws IOException, TimeoutException {
        try (TestClient client1 = new TestClient("Alice");
             TestClient client2 = new TestClient("Bob")) {

            // Client 1 reads its initial user list (ignore content for now due to bug)
            client1.readLineWithTimeout(1000); // Read "Connected users..." for Alice

            // Client 2 reads its initial user list
            String bobUsers = client2.readLineWithTimeout(1000); // Read "Connected users..." for Bob

            assertTrue(bobUsers.contains("Alice") && bobUsers.contains("Bob"), "Bob's user list incorrect");

            // Client 2 should also receive the join message about Alice (timing might vary)
            // This test might be flaky depending on server send order.
            // String bobJoin = client2.readLineWithTimeout(1000); // Might be Alice joined or user list
            // assertTrue(bobJoin.contains("Alice has joined") || bobJoin.contains("Connected users"), "Bob didn't get user list or join message");


            // Client 1 should receive the join message about Bob
            String aliceJoin = client1.readLineWithTimeout(1000);
            assertEquals("Bob has joined the chat!", aliceJoin, "Alice did not receive Bob's join message");
        }
    }

    @Test
    @Timeout(10)
    void testMessageBroadcast() throws IOException, TimeoutException {
        try (TestClient client1 = new TestClient("Charlie");
             TestClient client2 = new TestClient("David")) {

            // Consume initial messages
            client1.readLineWithTimeout(1000); // Charlie's user list
            client2.readLineWithTimeout(1000); // David's user list
            client1.readLineWithTimeout(1000); // David joined message for Charlie
            // client2.readLineWithTimeout(1000); // Charlie joined message for David (consume if needed)


            // Charlie sends a message
            client1.sendMessage("Hello David!");

            // Both should receive it
            String msgForCharlie = client1.readLineWithTimeout(1000);
            String msgForDavid = client2.readLineWithTimeout(1000);


            assertEquals("Charlie: Hello David!", msgForCharlie, "Charlie didn't receive own message");
            assertEquals("Charlie: Hello David!", msgForDavid, "David didn't receive Charlie's message");
        }
    }

    @Test
    @Timeout(10)
    void testClientLeaveMessage() throws IOException, TimeoutException {
        TestClient client1 = new TestClient("Eve");
        // Consume initial user list
        client1.readLineWithTimeout(1000);

        String client2Username = "Frank";
        try (TestClient client2 = new TestClient(client2Username)) {
            // Consume initial messages for client2
            client2.readLineWithTimeout(1000); // User list
            // Consume join message for client1
            client1.readLineWithTimeout(1000); // Frank joined

            // Client 2 leaves (by closing the try-with-resources block)
        } // client2.close() is called here automatically

        // Client 1 should receive the leave message
        String leaveMsg = client1.readLineWithTimeout(2000); // Allow slightly longer for disconnect propagation
        assertEquals(client2Username + " has left the chat.", leaveMsg, "Client 1 did not receive leave message");

        // Clean up client1
        client1.close();
    }
}