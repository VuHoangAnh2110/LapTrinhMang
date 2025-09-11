package server;

import shared.models.Player;
import shared.models.GameState;
import shared.utils.Constants;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameEngine implements Runnable {
    private Map<String, Player> players;
    private volatile GameState currentGameState;
    private boolean isRunning;
    private static final int TICK_RATE = 30;
    private GameServer server;
    private final Object stateLock = new Object();
    
    public GameEngine(GameServer server) {
        this.server = server;
        players = new ConcurrentHashMap<>();
        updateGameState();
        isRunning = true;
        System.out.println("GameEngine initialized");
    }
    
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = TICK_RATE;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        
        System.out.println("GameEngine started");
        
        while (isRunning) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            
            if (delta >= 1) {
                update();
                delta--;
            }
            
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private void update() {
        synchronized (stateLock) {
            boolean hasChanges = false;
            for (Player player : players.values()) {
                if (player.isAttacking()) {
                    player.setAttacking(false);
                    hasChanges = true;
                }
            }
            
            if (hasChanges) {
                updateGameState();
                server.broadcastGameState();
            }
        }
    }
    
    public synchronized void addPlayer(Player player) {
        System.out.println("Adding player to game engine: " + player.getUsername());
        
        // Set initial position for new player
        int spawnX = players.size() * 100 + 50;
        player.setX(spawnX);
        player.setY(Constants.WINDOW_HEIGHT - 100);
        player.setHealth(Constants.MAX_HEALTH);
        player.setDirection("right");
        
        players.put(player.getUsername(), player);
        System.out.println("Player added. Total players: " + players.size());
        
        updateGameStateMode();
        updateGameState();
        
        System.out.println("About to broadcast game state with " + players.size() + " players");
        server.broadcastGameState();
    }
    
    public synchronized void removePlayer(String username) {
        System.out.println("Removing player from game engine: " + username);
        Player removed = players.remove(username);
        
        if (removed != null) {
            System.out.println("Player removed. Total players: " + players.size());
            updateGameStateMode();
            updateGameState();
            server.broadcastGameState();
        }
    }
    
    private void updateGameStateMode() {
        if (players.size() >= 2 && (currentGameState == null || !currentGameState.isGameStarted())) {
            System.out.println("Game started with " + players.size() + " players!");
        } else if (players.size() < 2 && (currentGameState != null && currentGameState.isGameStarted())) {
            System.out.println("Game stopped - not enough players");
        }
    }
    
    public synchronized void updatePlayerPosition(String username, Object positionData) {
        Player player = players.get(username);
        if (player != null && positionData instanceof int[]) {
            int[] pos = (int[]) positionData;
            if (pos.length >= 2) {
                int newX = Math.max(0, Math.min(Constants.WINDOW_WIDTH - Constants.PLAYER_WIDTH, pos[0]));
                int newY = Math.max(0, Math.min(Constants.WINDOW_HEIGHT - Constants.PLAYER_HEIGHT, pos[1]));
                
                player.setX(newX);
                player.setY(newY);
                
                if (pos.length >= 3) {
                    player.setDirection(pos[2] > 0 ? "right" : "left");
                }
                
                System.out.println("Updated " + username + " position to (" + newX + ", " + newY + ")");
            }
            updateGameState();
        }
    }
    
    public synchronized void processAttack(String username) {
        Player attacker = players.get(username);
        if (attacker != null && currentGameState != null && currentGameState.isGameStarted()) {
            attacker.setAttacking(true);
            System.out.println(username + " is attacking!");
            
            checkAttackCollisions(attacker);
            updateGameState();
        }
    }
    
    private void checkAttackCollisions(Player attacker) {
        for (Player other : players.values()) {
            if (!other.getUsername().equals(attacker.getUsername())) {
                if (isColliding(attacker, other)) {
                    other.setHealth(other.getHealth() - Constants.ATTACK_DAMAGE);
                    System.out.println(other.getUsername() + " took damage! Health: " + other.getHealth());
                    
                    if (other.getHealth() <= 0) {
                        System.out.println(other.getUsername() + " was defeated by " + attacker.getUsername() + "!");
                        server.notifyGameEnd(attacker.getUsername());
                        resetPlayer(other);
                    }
                }
            }
        }
    }
    
    private void resetPlayer(Player player) {
        player.setHealth(Constants.MAX_HEALTH);
        player.setX(50);
        player.setY(Constants.WINDOW_HEIGHT - 100);
    }
    
    private boolean isColliding(Player p1, Player p2) {
        int distance = Math.abs(p1.getX() - p2.getX());
        int verticalDistance = Math.abs(p1.getY() - p2.getY());
        return distance < Constants.ATTACK_RANGE && verticalDistance < Constants.PLAYER_HEIGHT;
    }
    
    private synchronized void updateGameState() {
        synchronized (stateLock) {
            // Create completely new GameState object
            GameState newGameState = new GameState();
            
            // Create fresh copies of all players
            Map<String, Player> playersCopy = new ConcurrentHashMap<>();
            for (Map.Entry<String, Player> entry : players.entrySet()) {
                Player original = entry.getValue();
                Player copy = new Player(original.getId(), original.getUsername());
                copy.setX(original.getX());
                copy.setY(original.getY());
                copy.setHealth(original.getHealth());
                copy.setDirection(original.getDirection());
                copy.setAttacking(original.isAttacking());
                playersCopy.put(entry.getKey(), copy);
            }
            
            newGameState.setPlayers(playersCopy);
            
            // Set game mode
            if (players.size() >= 2) {
                newGameState.setGameStarted(true);
                newGameState.setGameMode("FIGHTING");
            } else {
                newGameState.setGameStarted(false);
                newGameState.setGameMode("WAITING");
            }
            
            newGameState.setTimestamp(System.currentTimeMillis());
            
            this.currentGameState = newGameState;
            
            System.out.println("GameState updated: " + newGameState.toString());
        }
    }
    
    public GameState getGameState() {
        synchronized (stateLock) {
            if (currentGameState == null) {
                updateGameState();
            }
            // Return a deep copy to prevent reference issues
            return new GameState(currentGameState);
        }
    }
    
    public void stop() {
        isRunning = false;
    }
}