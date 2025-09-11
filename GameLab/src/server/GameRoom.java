package server;

import shared.models.Player;
import shared.models.GameState;
import shared.models.Message;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

public class GameRoom {
    private String roomId;
    private Map<String, ClientHandler> players;
    private GameState gameState;
    private int maxPlayers;
    private boolean isActive;
    private long createdTime;
    
    public GameRoom(String roomId, int maxPlayers) {
        this.roomId = roomId;
        this.maxPlayers = maxPlayers;
        this.players = new ConcurrentHashMap<>();
        this.gameState = new GameState();
        this.isActive = false;
        this.createdTime = System.currentTimeMillis();
    }
    
    public boolean addPlayer(String username, ClientHandler handler) {
        if (players.size() >= maxPlayers) {
            return false;
        }
        
        players.put(username, handler);
        Player player = new Player(players.size(), username);
        
        // Set spawn position
        int spawnX = (players.size() - 1) * 100 + 50;
        player.setX(spawnX);
        player.setY(500);
        
        Map<String, Player> gamePlayers = gameState.getPlayers();
        gamePlayers.put(username, player);
        
        // Start game if we have enough players
        if (players.size() >= 2 && !isActive) {
            startGame();
        }
        
        broadcastMessage(new Message(Message.Type.PLAYER_JOINED, null, username));
        broadcastGameState();
        
        return true;
    }
    
    public void removePlayer(String username) {
        players.remove(username);
        gameState.getPlayers().remove(username);
        
        // Stop game if not enough players
        if (players.size() < 2 && isActive) {
            stopGame();
        }
        
        broadcastMessage(new Message(Message.Type.PLAYER_LEFT, null, username));
        broadcastGameState();
    }
    
    public void startGame() {
        isActive = true;
        gameState.setGameStarted(true);
        gameState.setGameMode("FIGHTING");
        
        System.out.println("Game started in room " + roomId);
        broadcastMessage(new Message(Message.Type.GAME_STATE, gameState, null));
    }
    
    public void stopGame() {
        isActive = false;
        gameState.setGameStarted(false);
        gameState.setGameMode("WAITING");
        
        System.out.println("Game stopped in room " + roomId);
        broadcastGameState();
    }
    
    public void updatePlayerPosition(String username, int[] position) {
        Player player = gameState.getPlayers().get(username);
        if (player != null && position.length >= 2) {
            player.setX(position[0]);
            player.setY(position[1]);
            if (position.length >= 3) {
                player.setDirection(position[2] > 0 ? "right" : "left");
            }
            broadcastGameState();
        }
    }
    
    public void processAttack(String username) {
        Player attacker = gameState.getPlayers().get(username);
        if (attacker != null && isActive) {
            attacker.setAttacking(true);
            
            // Check collisions with other players
            for (Player other : gameState.getPlayers().values()) {
                if (!other.getUsername().equals(username) && isColliding(attacker, other)) {
                    other.setHealth(other.getHealth() - 10);
                    
                    if (other.getHealth() <= 0) {
                        handlePlayerDefeat(attacker.getUsername(), other.getUsername());
                    }
                }
            }
            
            broadcastGameState();
        }
    }
    
    private boolean isColliding(Player p1, Player p2) {
        int distance = Math.abs(p1.getX() - p2.getX());
        int verticalDistance = Math.abs(p1.getY() - p2.getY());
        return distance < 50 && verticalDistance < 60;
    }
    
    private void handlePlayerDefeat(String winner, String loser) {
        System.out.println("Player " + loser + " defeated by " + winner + " in room " + roomId);
        
        // Reset defeated player
        Player defeatedPlayer = gameState.getPlayers().get(loser);
        if (defeatedPlayer != null) {
            defeatedPlayer.setHealth(100);
            defeatedPlayer.setX(50);
            defeatedPlayer.setY(500);
        }
        
        // Notify players
        broadcastMessage(new Message(Message.Type.GAME_END, winner, null, 
            winner + " wins the round!"));
    }
    
    public void broadcastMessage(Message message) {
        for (ClientHandler handler : players.values()) {
            handler.sendMessage(message);
        }
    }
    
    public void broadcastGameState() {
        gameState.setTimestamp(System.currentTimeMillis());
        for (ClientHandler handler : players.values()) {
            handler.sendMessage(gameState);
        }
    }
    
    // Getters
    public String getRoomId() { return roomId; }
    public int getPlayerCount() { return players.size(); }
    public int getMaxPlayers() { return maxPlayers; }
    public boolean isActive() { return isActive; }
    public boolean isFull() { return players.size() >= maxPlayers; }
    public long getCreatedTime() { return createdTime; }
    public GameState getGameState() { return gameState; }
    
    public List<String> getPlayerNames() {
        return new ArrayList<>(players.keySet());
    }
}