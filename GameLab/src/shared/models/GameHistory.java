package shared.models;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class GameHistory implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int gameId;
    private String roomId;
    private List<String> players;
    private String winner;
    private int duration;
    private long timestamp;
    private String gameMode;
    
    public GameHistory(int gameId, String roomId, List<String> players, String winner, int duration) {
        this.gameId = gameId;
        this.roomId = roomId;
        this.players = new ArrayList<>(players);
        this.winner = winner;
        this.duration = duration;
        this.timestamp = System.currentTimeMillis();
        this.gameMode = "Battle Royale";
    }
    
    // Getters and setters
    public int getGameId() { return gameId; }
    public String getRoomId() { return roomId; }
    public List<String> getPlayers() { return players; }
    public String getWinner() { return winner; }
    public int getDuration() { return duration; }
    public long getTimestamp() { return timestamp; }
    public String getGameMode() { return gameMode; }
    
    @Override
    public String toString() {
        return String.format("Game #%d - Room: %s, Winner: %s, Duration: %ds, Players: %s", 
            gameId, roomId, winner, duration, players);
    }
}