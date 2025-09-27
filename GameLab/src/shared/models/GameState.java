package shared.models;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum State {
        WAITING_FOR_PLAYERS,
        READY_CHECK,
        STARTING,
        IN_PROGRESS,
        PAUSED,
        FINISHED
    }
    
    private State currentState;
    private String roomId;
    private Map<String, PlayerState> players;
    private long gameStartTime;
    private long gameEndTime;
    private int gameDurationSeconds;
    private String winner;
    private boolean allPlayersReady;
    private int readyCount;
    private long lastUpdateTime;
    
    public GameState(String roomId) {
        this.roomId = roomId;
        this.currentState = State.WAITING_FOR_PLAYERS;
        this.players = new ConcurrentHashMap<>();
        this.gameDurationSeconds = 300; // 5 minutes default
        this.allPlayersReady = false;
        this.readyCount = 0;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    public void addPlayer(PlayerState player) {
        players.put(player.getUsername(), player);
        updateReadyStatus();
        updateTimestamp();
    }
    
    public void removePlayer(String username) {
        PlayerState removed = players.remove(username);
        if (removed != null && removed.isReady()) {
            readyCount--;
        }
        updateReadyStatus();
        updateTimestamp();
    }
    
    public void setPlayerReady(String username, boolean ready) {
        PlayerState player = players.get(username);
        if (player != null) {
            boolean wasReady = player.isReady();
            player.setReady(ready);
            
            if (ready && !wasReady) {
                readyCount++;
            } else if (!ready && wasReady) {
                readyCount--;
            }
            
            updateReadyStatus();
            updateTimestamp();
        }
    }
    
    private void updateReadyStatus() {
        int minPlayers = 2;
        allPlayersReady = players.size() >= minPlayers && 
                         readyCount == players.size() && 
                         readyCount >= minPlayers;
        
        if (allPlayersReady && currentState == State.WAITING_FOR_PLAYERS) {
            currentState = State.READY_CHECK;
        } else if (!allPlayersReady && currentState == State.READY_CHECK) {
            currentState = State.WAITING_FOR_PLAYERS;
        }
    }
    
    public void startGame() {
        if (canStartGame()) {
            currentState = State.STARTING;
            gameStartTime = System.currentTimeMillis();
            gameEndTime = gameStartTime + (gameDurationSeconds * 1000L);
            
            // Reset all players to game state
            for (PlayerState player : players.values()) {
                player.resetToDefault();
                player.setReady(false);
            }
            
            currentState = State.IN_PROGRESS;
            updateTimestamp();
        }
    }
    
    public void endGame(String winnerUsername) {
        currentState = State.FINISHED;
        this.winner = winnerUsername;
        gameEndTime = System.currentTimeMillis();
        updateTimestamp();
    }
    
    public void pauseGame() {
        if (currentState == State.IN_PROGRESS) {
            currentState = State.PAUSED;
            updateTimestamp();
        }
    }
    
    public void resumeGame() {
        if (currentState == State.PAUSED) {
            currentState = State.IN_PROGRESS;
            updateTimestamp();
        }
    }
    
    public boolean canStartGame() {
        return currentState == State.READY_CHECK && allPlayersReady;
    }
    
    public boolean isGameActive() {
        return currentState == State.IN_PROGRESS || currentState == State.PAUSED;
    }
    
    public boolean isGameFinished() {
        return currentState == State.FINISHED;
    }
    
    public long getRemainingTime() {
        if (currentState != State.IN_PROGRESS) {
            return 0;
        }
        return Math.max(0, gameEndTime - System.currentTimeMillis());
    }
    
    public boolean isTimeUp() {
        return currentState == State.IN_PROGRESS && System.currentTimeMillis() >= gameEndTime;
    }
    
    public int getGameProgress() {
        if (currentState != State.IN_PROGRESS) {
            return 0;
        }
        
        long totalTime = gameEndTime - gameStartTime;
        long elapsedTime = System.currentTimeMillis() - gameStartTime;
        
        return (int) Math.min(100, (elapsedTime * 100) / totalTime);
    }
    
    private void updateTimestamp() {
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    // Getters and setters
    public State getCurrentState() { return currentState; }
    public void setCurrentState(State currentState) { 
        this.currentState = currentState; 
        updateTimestamp();
    }
    
    public String getRoomId() { return roomId; }
    public Map<String, PlayerState> getPlayers() { return players; }
    public long getGameStartTime() { return gameStartTime; }
    public long getGameEndTime() { return gameEndTime; }
    public int getGameDurationSeconds() { return gameDurationSeconds; }
    public void setGameDurationSeconds(int gameDurationSeconds) { 
        this.gameDurationSeconds = gameDurationSeconds; 
    }
    
    public String getWinner() { return winner; }
    public boolean isAllPlayersReady() { return allPlayersReady; }
    public int getReadyCount() { return readyCount; }
    public int getPlayerCount() { return players.size(); }
    public long getLastUpdateTime() { return lastUpdateTime; }
    
    @Override
    public String toString() {
        return String.format("GameState{room=%s, state=%s, players=%d, ready=%d/%d, winner=%s}", 
            roomId, currentState, players.size(), readyCount, players.size(), winner);
    }
}