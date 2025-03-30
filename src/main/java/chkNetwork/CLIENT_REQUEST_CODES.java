package chkNetwork;

public enum CLIENT_REQUEST_CODES {

    // Connection requests
    CONNECT(1000),
    DISCONNECT(1001),
    PING(1002),

    // Game-related requests
    JOIN_GAME(1100),
    LEAVE_GAME(1101),
    START_GAME(1102),

    // Player actions (Checkers specific)
    MOVE_PIECE(1200),      // Move a piece (normal or jump)
    END_TURN(1201),        // End turn manually (if needed)
    FORFEIT_GAME(1202),    // Player gives up
    REQUEST_REMATCH(1203), // Ask for a rematch
    SEND_CHAT_MESSAGE(1204); // Send a chat message

    private final int code;

    CLIENT_REQUEST_CODES(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
