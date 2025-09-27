package shared.models;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

public class RoomState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Status {
        WAITING, STARTING, IN_PROGRESS, FINISHED
    }
    
    private String roomId;
    private String hostUsername;
    private Status status;
    private Map<String, PlayerState> players;
    private GameState gameState;
    private int maxPlayers;
    private long createdTime;
    private long lastActivityTime;
    private boolean isPrivate;
    private String password;
    private Map<String, Object> gameSettings;
    
    public RoomState(String roomId, String hostUsername) {
        this.roomId = roomId;
        this.hostUsername = hostUsername;
        this.status = Status.WAITING;
        this.players = new ConcurrentHashMap<>();
        this.gameState = new GameState(roomId);
        this.maxPlayers = 3;
        this.createdTime = System.currentTimeMillis();
        this.lastActivityTime = System.currentTimeMillis();
        this.isPrivate = false;
        this.gameSettings = new ConcurrentHashMap<>();
        
        // Initialize default game settings
        initializeDefaultSettings();
    }
    
    private void initializeDefaultSettings() {
        gameSettings.put("gameDuration", 300); // 5 minutes
        gameSettings.put("maxHealth", 100);
        gameSettings.put("attackDamage", 20);
        gameSettings.put("respawnTime", 6000); // 6 seconds
        gameSettings.put("friendlyFire", false);
        gameSettings.put("allowSpectators", true);
    }
    
    public boolean addPlayer(PlayerState player) {
        if (players.size() >= maxPlayers || status != Status.WAITING) {
            return false;
        }
        
        players.put(player.getUsername(), player);
        gameState.addPlayer(player);
        updateActivity();
        
        return true;
    }
    
    public boolean removePlayer(String username) {
        PlayerState removed = players.remove(username);
        if (removed != null) {
            gameState.removePlayer(username);
            
            // Transfer host if needed
            if (username.equals(hostUsername) && !players.isEmpty()) {
                hostUsername = players.keySet().iterator().next();
            }
            
            updateActivity();
            return true;
        }
        return false;
    }
    
    public void setPlayerReady(String username, boolean ready) {
        PlayerState player = players.get(username);
        if (player != null) {
            gameState.setPlayerReady(username, ready);
            updateActivity();
        }
    }
    
    public boolean canStartGame() {
        return status == Status.WAITING && 
               players.size() >= 2 && 
               gameState.canStartGame();
    }
    
    public void startGame() {
        if (canStartGame()) {
            status = Status.IN_PROGRESS;
            gameState.startGame();
            updateActivity();
        }
    }
    
    public void endGame(String winner) {
        status = Status.FINISHED;
        gameState.endGame(winner);
        updateActivity();
    }
    
    public boolean isHost(String username) {
        return hostUsername.equals(username);
    }
    
    public boolean isFull() {
        return players.size() >= maxPlayers;
    }
    
    public boolean isEmpty() {
        return players.isEmpty();
    }
    
    public boolean isActive() {
        long inactiveThreshold = 30 * 60 * 1000; // 30 minutes
        return System.currentTimeMillis() - lastActivityTime < inactiveThreshold;
    }
    
    public List<PlayerState> getPlayersList() {
        return new ArrayList<>(players.values());
    }
    
    public List<PlayerState> getTopPlayers() {
        return players.values().stream()
            .sorted((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    public PlayerState getPlayer(String username) {
        return players.get(username);
    }
    
    public boolean hasPlayer(String username) {
        return players.containsKey(username);
    }
    
    public int getReadyCount() {
        return (int) players.values().stream()
            .mapToInt(p -> p.isReady() ? 1 : 0)
            .sum();
    }
    
    public boolean allPlayersReady() {
        return players.size() >= 2 && 
               players.values().stream().allMatch(PlayerState::isReady);
    }
        
    public boolean canStart() {
        return canStartGame();
    }

    public void endGame() {
        status = Status.FINISHED;
        if (gameState != null) {
            // Find winner based on top score
            List<PlayerState> topPlayers = getTopPlayers();
            String winner = topPlayers.isEmpty() ? "No Winner" : topPlayers.get(0).getUsername();
            gameState.endGame(winner);
        }
        updateActivity();
    }

    public void checkAllPlayersReady() {
        // This method updates the ready status - already handled by allPlayersReady()
        // Just update activity timestamp
        updateActivity();
    }

    public boolean isGameTimeUp() {
        return gameState != null && gameState.isTimeUp();
    }

    public long getRemainingTime() {
        return gameState != null ? gameState.getRemainingTime() : 0;
    }

    public void updateGameSetting(String key, Object value) {
        gameSettings.put(key, value);
        updateActivity();
    }
    
    public Object getGameSetting(String key) {
        return gameSettings.get(key);
    }
    
    public void setPrivate(boolean isPrivate, String password) {
        this.isPrivate = isPrivate;
        this.password = isPrivate ? password : null;
        updateActivity();
    }
    
    public boolean checkPassword(String inputPassword) {
        if (!isPrivate) return true;
        return password != null && password.equals(inputPassword);
    }
    
    private void updateActivity() {
        this.lastActivityTime = System.currentTimeMillis();
    }
    
    public long getAge() {
        return System.currentTimeMillis() - createdTime;
    }
    
    public long getTimeSinceLastActivity() {
        return System.currentTimeMillis() - lastActivityTime;
    }
    
    // Getters and setters
    public String getRoomId() { return roomId; }
    public String getHostUsername() { return hostUsername; }
    public void setHostUsername(String hostUsername) { this.hostUsername = hostUsername; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { 
        this.status = status; 
        updateActivity();
    }
    
    public Map<String, PlayerState> getPlayers() { return players; }
    public GameState getGameState() { return gameState; }
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
    
    public int getCurrentPlayers() { return players.size(); }
    public long getCreatedTime() { return createdTime; }
    public long getLastActivityTime() { return lastActivityTime; }
    
    public boolean isPrivate() { return isPrivate; }
    public Map<String, Object> getGameSettings() { return gameSettings; }
    
    @Override
    public String toString() {
        return String.format("RoomState{id='%s', host='%s', players=%d/%d, status=%s, ready=%d}", 
            roomId, hostUsername, players.size(), maxPlayers, status, getReadyCount());
    }
}