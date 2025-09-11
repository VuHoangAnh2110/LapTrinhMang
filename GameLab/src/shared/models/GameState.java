package shared.models;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Map<String, Player> players;
    private boolean gameStarted;
    private String gameMode;
    private long timestamp;
    
    public GameState() {
        this.players = new ConcurrentHashMap<>();
        this.gameStarted = false;
        this.gameMode = "WAITING";
        this.timestamp = System.currentTimeMillis();
    }
    
    // Deep copy constructor
    public GameState(GameState other) {
        this.players = new ConcurrentHashMap<>();
        if (other.players != null) {
            for (Map.Entry<String, Player> entry : other.players.entrySet()) {
                Player originalPlayer = entry.getValue();
                Player copiedPlayer = new Player(originalPlayer.getId(), originalPlayer.getUsername());
                copiedPlayer.setX(originalPlayer.getX());
                copiedPlayer.setY(originalPlayer.getY());
                copiedPlayer.setHealth(originalPlayer.getHealth());
                copiedPlayer.setDirection(originalPlayer.getDirection());
                copiedPlayer.setAttacking(originalPlayer.isAttacking());
                this.players.put(entry.getKey(), copiedPlayer);
            }
        }
        this.gameStarted = other.gameStarted;
        this.gameMode = other.gameMode;
        this.timestamp = System.currentTimeMillis();
    }
    
    public Map<String, Player> getPlayers() {
        return players;
    }
    
    public void setPlayers(Map<String, Player> players) {
        this.players = players;
    }
    
    public boolean isGameStarted() {
        return gameStarted;
    }
    
    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }
    
    public String getGameMode() {
        return gameMode;
    }
    
    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return String.format("GameState{players=%d, gameStarted=%s, gameMode='%s', timestamp=%d}",
            players != null ? players.size() : 0, gameStarted, gameMode, timestamp);
    }
}