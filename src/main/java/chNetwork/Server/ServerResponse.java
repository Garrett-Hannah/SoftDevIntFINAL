package chNetwork.Server;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Enum for different types of messages the server might send
enum ResponseType {
    JOIN_NOTIFICATION,  // "User X has joined..."
    LEAVE_NOTIFICATION, // "User Y has left..."
    BROADCAST_MESSAGE,  // "User Z: some message"
    USER_LIST,          // "Connected users: A, B, C"
    SERVER_MESSAGE,     // General server info/error
    UNKNOWN             // If parsing fails
}

// The class to represent a structured server response
class ServerResponse {
    private final ResponseType type;
    private final String sender;      // For BROADCAST, JOIN, LEAVE
    private final String payload;     // Message content for BROADCAST, SERVER_MESSAGE
    private final List<String> users; // For USER_LIST
    private final String rawMessage;  // Keep the original for debugging

    // Private constructor, use static factory method 'parse'
    private ServerResponse(ResponseType type, String sender, String payload, List<String> users, String rawMessage) {
        this.type = type;
        this.sender = sender;
        this.payload = payload;
        this.users = users;
        this.rawMessage = rawMessage;
    }

    // --- Getters ---
    public ResponseType getType() { return type; }
    public String getSender() { return sender; }
    public String getPayload() { return payload; }
    public List<String> getUsers() { return users; }
    public String getRawMessage() { return rawMessage; }


    // --- Static Parsing Logic ---
    // Define patterns for expected message formats
    private static final Pattern JOIN_PATTERN = Pattern.compile("^(\\w+) has joined the chat!?$");
    private static final Pattern LEAVE_PATTERN = Pattern.compile("^(\\w+) has left the chat\\.?$");
    private static final Pattern BROADCAST_PATTERN = Pattern.compile("^(\\w+):\\s*(.*)$");
    private static final Pattern USER_LIST_PATTERN = Pattern.compile("^Connected users:\\s*(.*)$");


    /**
     * Parses a raw string message from the server into a ServerResponse object.
     * This is where you adapt the logic to your server's specific output format.
     */
    public static ServerResponse parse(String rawMessage) {
        if (rawMessage == null) {
            return new ServerResponse(ResponseType.UNKNOWN, null, null, null, null);
        }

        Matcher matcher;

        matcher = JOIN_PATTERN.matcher(rawMessage);
        if (matcher.matches()) {
            String user = matcher.group(1);
            return new ServerResponse(ResponseType.JOIN_NOTIFICATION, user, null, null, rawMessage);
        }

        matcher = LEAVE_PATTERN.matcher(rawMessage);
        if (matcher.matches()) {
            String user = matcher.group(1);
            return new ServerResponse(ResponseType.LEAVE_NOTIFICATION, user, null, null, rawMessage);
        }

        matcher = BROADCAST_PATTERN.matcher(rawMessage);
        if (matcher.matches()) {
            String sender = matcher.group(1);
            String message = matcher.group(2);
            return new ServerResponse(ResponseType.BROADCAST_MESSAGE, sender, message, null, rawMessage);
        }

        matcher = USER_LIST_PATTERN.matcher(rawMessage);
        if (matcher.matches()) {
            String userListStr = matcher.group(1);
            List<String> users = Arrays.asList(userListStr.split(",\\s*")); // Split by comma and optional space
            // Handle potential empty list if the string was just "Connected users: "
            if (users.size() == 1 && users.get(0).isEmpty()) {
                users = List.of(); // Represent as empty list
            }
            return new ServerResponse(ResponseType.USER_LIST, null, null, users, rawMessage);
        }


        // If none of the specific patterns match, treat as a generic server message or unknown
        // You might refine this based on other expected server messages
        System.err.println("WARN: Could not parse server message: " + rawMessage);
        return new ServerResponse(ResponseType.UNKNOWN, null, rawMessage, null, rawMessage); // Or SERVER_MESSAGE?
    }

    @Override
    public String toString() {
        return "ServerResponse{" +
                "type=" + type +
                ", sender='" + sender + '\'' +
                ", payload='" + payload + '\'' +
                ", users=" + users +
                ", raw='" + rawMessage.substring(0, Math.min(rawMessage.length(), 50)) + "...'" + // Avoid huge raw output
                '}';
    }

    // Optional: Implement equals/hashCode if needed for comparisons
}