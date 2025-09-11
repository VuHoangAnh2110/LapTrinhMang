package shared.models;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Type {
        LOGIN, LOGIN_SUCCESS, LOGIN_FAILED,
        REGISTER, REGISTER_SUCCESS, REGISTER_FAILED,
        JOIN_ROOM,  // Add this
        MOVE, ATTACK, LOGOUT,
        PLAYER_JOINED, PLAYER_LEFT,
        GAME_STATE, GAME_END,
        USER_STATS
    }
    
    private Type type;
    private Object data;
    private String username;
    private String message;
    private long timestamp;
    
    public Message(Type type, Object data, String username) {
        this.type = type;
        this.data = data;
        this.username = username;
        this.timestamp = System.currentTimeMillis();
    }
    
    public Message(Type type, Object data, String username, String message) {
        this.type = type;
        this.data = data;
        this.username = username;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and setters
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    @Override
    public String toString() {
        return String.format("Message{type=%s, username='%s', message='%s', timestamp=%d}", 
            type, username, message, timestamp);
    }
}