package chkNetwork;

public enum SERVER_RESPONSE_CODES {

    // General success & error responses
    SUCCESS(2000),
    ERROR(2001),
    INVALID_REQUEST(2002),

    // Game state updates
    GAME_STATE_UPDATE(2100),  // Broadcast updated board state
    GAME_START(2101),         // Game officially starts
    GAME_END(2102),           // Game ends
    ROUND_UPDATE(2103),       // Round state update
    PLAYER_JOINED(2104),      // A new player has joined
    PLAYER_LEFT(2105),        // A player left the game

    // Player actions & validation
    MOVE_ACCEPTED(2200),      // Move was valid
    MOVE_REJECTED(2201),      // Move was invalid
    INVALID_TURN(2202),       // Player moved when it's not their turn
    ACTION_NOT_ALLOWED(2203), // Player action not allowed (e.g., wrong piece type)

    // Connection & server issues
    SERVER_ERROR(2300),       // Internal server issue
    CONNECTION_LOST(2301),    // Player lost connection
    RECONNECT_SUCCESS(2302),  // Reconnection successful
    TIMEOUT_WARNING(2303),    // Player timeout warning

    // Messaging & events
    CHAT_MESSAGE(2400),       // Chat message received
    NOTIFICATION(2401);       // General game notifications (e.g., "Your turn!")

    private final int code;

    SERVER_RESPONSE_CODES(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
