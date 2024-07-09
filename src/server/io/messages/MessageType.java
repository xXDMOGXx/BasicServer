package server.io.messages;

// These are the types of messages that can be sent between the bot and server.
// The bot and server must have synced MessageTypes in the exact same order as each other
// You can use whatever messages you want, these are just most of the ones that I personally use
public enum MessageType {
    HANDSHAKE_REQUEST,
    HANDSHAKE_ACCEPTED,
    PING_REQUEST,
    PING_RESPONDED,
    DISABLE_WEAPON,
    ENABLE_WEAPON,
    DISABLE_DRIVE,
    ENABLE_DRIVE,
    SEND_LEFT,
    SEND_RIGHT,
    SEND_WEAPON;

    // Unlike c++, Java doesn't store enumerators as ascending
    // integers starting from zero. Thus, we have to do crap like this
    public static MessageType fromInt(int x) {
        return switch (x) {
            case 0 -> HANDSHAKE_REQUEST;
            case 1 -> HANDSHAKE_ACCEPTED;
            case 2 -> PING_REQUEST;
            case 3 -> PING_RESPONDED;
            case 4 -> DISABLE_WEAPON;
            case 5 -> ENABLE_WEAPON;
            case 6 -> DISABLE_DRIVE;
            case 7 -> ENABLE_DRIVE;
            case 8 -> SEND_LEFT;
            case 9 -> SEND_RIGHT;
            case 10 -> SEND_WEAPON;
            default -> null;
        };
    }
}
