package shared.network;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

public class Command implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Type {
        // Connection commands
        CONNECT, DISCONNECT, PING, PONG,
        
        // Authentication commands
        LOGIN, LOGOUT, REGISTER, CHANGE_PASSWORD,
        
        // Room commands
        CREATE_ROOM, JOIN_ROOM, LEAVE_ROOM, LIST_ROOMS,
        SET_READY, START_GAME, END_GAME,
        
        // Game commands
        MOVE_PLAYER, ATTACK, USE_SKILL, CHAT,
        UPDATE_GAME_STATE, SYNC_PLAYERS,
        
        // Admin commands
        KICK_PLAYER, BAN_PLAYER, SET_HOST,
        PAUSE_GAME, RESUME_GAME, RESTART_GAME,
        
        // System commands
        ERROR, SUCCESS, INFO, WARNING
    }
    
    private Type type;
    private String senderId;
    private String targetId;
    private Map<String, Object> parameters;
    private long timestamp;
    private String sessionId;
    private boolean requiresResponse;
    
    public Command(Type type, String senderId) {
        this.type = type;
        this.senderId = senderId;
        this.parameters = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
        this.requiresResponse = false;
    }
    
    public Command(Type type, String senderId, String targetId) {
        this(type, senderId);
        this.targetId = targetId;
    }
    
    public Command(Type type, String senderId, Map<String, Object> parameters) {
        this(type, senderId);
        this.parameters = new HashMap<>(parameters);
    }
    
    // Builder pattern methods
    public Command withParameter(String key, Object value) {
        this.parameters.put(key, value);
        return this;
    }
    
    public Command withTarget(String targetId) {
        this.targetId = targetId;
        return this;
    }
    
    public Command withSession(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }
    
    public Command requireResponse() {
        this.requiresResponse = true;
        return this;
    }
    
    // Parameter helper methods
    public String getStringParameter(String key) {
        Object value = parameters.get(key);
        return value != null ? value.toString() : null;
    }
    
    public Integer getIntParameter(String key) {
        Object value = parameters.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Boolean getBooleanParameter(String key) {
        Object value = parameters.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }
    
    public Double getDoubleParameter(String key) {
        Object value = parameters.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key, Class<T> clazz) {
        Object value = parameters.get(key);
        if (value != null && clazz.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    public boolean hasParameter(String key) {
        return parameters.containsKey(key);
    }
    
    // Validation methods
    public boolean isValid() {
        return type != null && senderId != null && !senderId.trim().isEmpty();
    }
    
    public boolean isGameCommand() {
        return type == Type.MOVE_PLAYER || type == Type.ATTACK || 
               type == Type.USE_SKILL || type == Type.UPDATE_GAME_STATE;
    }
    
    public boolean isRoomCommand() {
        return type == Type.CREATE_ROOM || type == Type.JOIN_ROOM || 
               type == Type.LEAVE_ROOM || type == Type.SET_READY || 
               type == Type.START_GAME;
    }
    
    public boolean isAuthCommand() {
        return type == Type.LOGIN || type == Type.LOGOUT || 
               type == Type.REGISTER || type == Type.CHANGE_PASSWORD;
    }
    
    public boolean isAdminCommand() {
        return type == Type.KICK_PLAYER || type == Type.BAN_PLAYER || 
               type == Type.SET_HOST || type == Type.PAUSE_GAME || 
               type == Type.RESUME_GAME;
    }
    
    // Static factory methods
    public static Command login(String username, String password) {
        return new Command(Type.LOGIN, username)
            .withParameter("password", password);
    }
    
    public static Command movePlayer(String playerId, int x, int y) {
        return new Command(Type.MOVE_PLAYER, playerId)
            .withParameter("x", x)
            .withParameter("y", y);
    }
    
    public static Command attack(String attackerId, String targetId) {
        return new Command(Type.ATTACK, attackerId)
            .withTarget(targetId);
    }
    
    public static Command createRoom(String hostId, String roomName) {
        return new Command(Type.CREATE_ROOM, hostId)
            .withParameter("roomName", roomName);
    }
    
    public static Command joinRoom(String playerId, String roomId) {
        return new Command(Type.JOIN_ROOM, playerId)
            .withParameter("roomId", roomId);
    }
    
    public static Command chat(String senderId, String message, String roomId) {
        return new Command(Type.CHAT, senderId)
            .withParameter("message", message)
            .withParameter("roomId", roomId);
    }
    
    public static Command error(String message) {
        return new Command(Type.ERROR, "system")
            .withParameter("message", message);
    }
    
    public static Command success(String message) {
        return new Command(Type.SUCCESS, "system")
            .withParameter("message", message);
    }
    
    // Getters and setters
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { 
        this.parameters = parameters != null ? parameters : new HashMap<>(); 
    }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public boolean requiresResponse() { return requiresResponse; }
    public void setRequiresResponse(boolean requiresResponse) { 
        this.requiresResponse = requiresResponse; 
    }
    
    @Override
    public String toString() {
        return String.format("Command{type=%s, sender=%s, target=%s, params=%s, timestamp=%d}", 
            type, senderId, targetId, parameters.keySet(), timestamp);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Command command = (Command) obj;
        return type == command.type &&
               senderId.equals(command.senderId) &&
               timestamp == command.timestamp;
    }
    
    @Override
    public int hashCode() {
        return type.hashCode() + senderId.hashCode() + Long.hashCode(timestamp);
    }
}