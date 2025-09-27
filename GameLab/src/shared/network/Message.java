package shared.network;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Type {
        // Authentication
        LOGIN, LOGIN_SUCCESS, LOGIN_FAILED,
        REGISTER, REGISTER_SUCCESS, REGISTER_FAILED,
        
        // Room management
        GET_ROOMS, ROOMS_LIST, CREATE_ROOM, JOIN_ROOM, LEAVE_ROOM,
        ROOM_CREATED, ROOM_JOINED, ROOM_LEFT, ROOM_FULL,
        ROOM_UPDATE, PLAYER_READY, START_GAME,
        
        // Game actions
        MOVE, ATTACK, LOGOUT,
        PLAYER_JOINED, PLAYER_LEFT, PLAYER_RESPAWN,
        GAME_STATE, GAME_END, GAME_TIMER,
        
        // User data
        USER_STATS, GET_HISTORY, HISTORY_DATA,
        SCORE_UPDATE, RANK_UPDATE
    }
    
    private Type type;
    private Object data;
    private String username;
    private String message;
    private long timestamp;
    private String roomId;
    
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
    
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    @Override
    public String toString() {
        return String.format("Message{type=%s, username='%s', message='%s', roomId='%s', timestamp=%d}", 
            type, username, message, roomId, timestamp);
    }
}