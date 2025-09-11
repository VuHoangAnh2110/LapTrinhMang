package shared.models;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Type {
        LOGIN, LOGOUT, MOVE, ATTACK,
        GAME_STATE, JOIN_ROOM, PLAYER_JOINED, PLAYER_LEFT
    }
    
    private Type type;
    private Object data;
    private String username;
    
    public Message(Type type, Object data, String username) {
        this.type = type;
        this.data = data;
        this.username = username;
    }
    
    // Getters and setters
    public Type getType() { return type; }
    public Object getData() { return data; }
    public String getUsername() { return username; }
}