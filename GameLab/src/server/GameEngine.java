package server;

import shared.models.Player;
import shared.models.GameState;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameEngine implements Runnable {
    private Map<String, Player> players;
    private GameState gameState;
    private boolean isRunning;
    private static final int TICK_RATE = 60; // 60 FPS
    
    public GameEngine() {
        players = new ConcurrentHashMap<>();
        gameState = new GameState();
        isRunning = true;
    }
    
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = TICK_RATE;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        
        while (isRunning) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            
            if (delta >= 1) {
                update();
                delta--;
            }
        }
    }
    
    private void update() {
        // Update game logic here
        updateGameState();
    }
    
    public void addPlayer(Player player) {
        players.put(player.getUsername(), player);
        updateGameState();
    }
    
    public void removePlayer(String username) {
        players.remove(username);
        updateGameState();
    }
    
    public void updatePlayerPosition(String username, Object positionData) {
        Player player = players.get(username);
        if (player != null) {
            // Update player position based on positionData
            updateGameState();
        }
    }
    
    public void processAttack(String username) {
        Player attacker = players.get(username);
        if (attacker != null) {
            attacker.setAttacking(true);
            // Check for collision with other players
            checkAttackCollisions(attacker);
            updateGameState();
        }
    }
    
    private void checkAttackCollisions(Player attacker) {
        for (Player other : players.values()) {
            if (!other.getUsername().equals(attacker.getUsername())) {
                if (isColliding(attacker, other)) {
                    other.setHealth(other.getHealth() - 10);
                    if (other.getHealth() <= 0) {
                        // Handle player defeat
                        System.out.println(other.getUsername() + " was defeated!");
                    }
                }
            }
        }
    }
    
    private boolean isColliding(Player p1, Player p2) {
        int distance = Math.abs(p1.getX() - p2.getX());
        return distance < 50; // Attack range
    }
    
    private void updateGameState() {
        gameState.setPlayers(players);
        gameState.setTimestamp(System.currentTimeMillis());
    }
    
    public GameState getGameState() {
        return gameState;
    }
}