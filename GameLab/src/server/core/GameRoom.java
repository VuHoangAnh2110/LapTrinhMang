package server.core;

import shared.models.PlayerState;
import shared.models.RoomState;
import shared.network.Message;
import server.network.ClientHandler;
import server.utils.Logger;
import shared.utils.Constants;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameRoom {
    private RoomState roomData;
    private Map<String, ClientHandler> clientHandlers;
    private ScheduledExecutorService gameTimer;
    private ScheduledExecutorService respawnTimer;
    private volatile boolean gameRunning;
    private long gameStartTime;
    private long gameEndTime;
    private int gameId;
    private int gameDurationSeconds = 300; // 5 minutes default
    
    public GameRoom(String roomId, String hostUsername, int gameId) {
        this.roomData = new RoomState(roomId, hostUsername);
        this.clientHandlers = new ConcurrentHashMap<>();
        this.gameTimer = Executors.newScheduledThreadPool(2);
        this.respawnTimer = Executors.newScheduledThreadPool(1);
        this.gameRunning = false;
        this.gameId = gameId;
        
        Logger.logSync("Created room: " + roomId + " hosted by " + hostUsername);
    }
    
    public synchronized boolean addPlayer(String username, ClientHandler clientHandler) {
        if (!roomData.addPlayer(new PlayerState(clientHandlers.size() + 1, username))) {
            return false;
        }
        
        clientHandlers.put(username, clientHandler);
        
        // Notify all players about new player
        Message playerJoinedMsg = new Message(Message.Type.PLAYER_JOINED, 
            roomData.getPlayers().get(username), username);
        broadcastMessage(playerJoinedMsg);
        
        // Send room update to all players
        sendRoomUpdate();
        
        Logger.logSync("Player " + username + " joined room " + roomData.getRoomId());
        return true;
    }
    
    public synchronized void removePlayer(String username) {
        PlayerState player = roomData.getPlayers().remove(username);
        clientHandlers.remove(username);
        
        if (player != null) {
            // Notify remaining players
            Message playerLeftMsg = new Message(Message.Type.PLAYER_LEFT, 
                player, username);
            broadcastMessage(playerLeftMsg);
            
            // If game is running and no players left, end game
            if (gameRunning && roomData.getPlayers().isEmpty()) {
                endGame();
            }
            
            // Send room update
            sendRoomUpdate();
            
            Logger.logSync("Player " + username + " left room " + roomData.getRoomId());
        }
    }
    
    public synchronized void setPlayerReady(String username, boolean ready) {
        PlayerState player = roomData.getPlayers().get(username);
        if (player != null) {
            player.setReady(ready);
            sendRoomUpdate();
            
            Logger.logSync("Player " + username + " ready status: " + ready);
        }
    }
    
    public synchronized boolean startGame() {
        // Check if game can start
        if (!canStartGame()) {
            return false;
        }
        
        roomData.setStatus(RoomState.Status.IN_PROGRESS);
        gameRunning = true;
        gameStartTime = System.currentTimeMillis();
        gameEndTime = gameStartTime + (gameDurationSeconds * 1000L);
        
        // Reset all players to starting state
        for (PlayerState player : roomData.getPlayers().values()) {
            player.resetToDefault();
            player.setReady(false);
        }
        
        // Start game timer
        startGameTimer();
        
        // Notify all players that game started
        Message gameStartMsg = new Message(Message.Type.START_GAME, roomData, null);
        broadcastMessage(gameStartMsg);
        
        Logger.logSync("Game started in room " + roomData.getRoomId());
        return true;
    }
    
    private boolean canStartGame() {
        return roomData.getStatus() == RoomState.Status.WAITING && 
               roomData.getCurrentPlayers() >= 2 && 
               roomData.allPlayersReady();
    }
    
    private void startGameTimer() {
        gameTimer.scheduleAtFixedRate(() -> {
            if (!gameRunning) return;
            
            // Check respawns
            checkRespawns();
            
            // Send game state update
            Message gameStateMsg = new Message(Message.Type.GAME_STATE, roomData, null);
            broadcastMessage(gameStateMsg);
            
            // Check if time is up
            if (isGameTimeUp()) {
                endGame();
            }
            
            // Send timer update
            long remainingTime = getRemainingTime();
            Message timerMsg = new Message(Message.Type.GAME_TIMER, remainingTime, null);
            broadcastMessage(timerMsg);
            
        }, 0, 100, TimeUnit.MILLISECONDS); // Update every 100ms
    }
    
    private boolean isGameTimeUp() {
        return gameRunning && System.currentTimeMillis() >= gameEndTime;
    }
    
    private long getRemainingTime() {
        if (!gameRunning) return 0;
        return Math.max(0, gameEndTime - System.currentTimeMillis());
    }
    
    private void checkRespawns() {
        for (PlayerState player : roomData.getPlayers().values()) {
            if (player.canRespawn()) {
                player.respawn();
                
                Message respawnMsg = new Message(Message.Type.PLAYER_RESPAWN, 
                    player, player.getUsername());
                broadcastMessage(respawnMsg);
                
                Logger.logSync("Player " + player.getUsername() + " respawned");
            }
        }
    }
    
    public synchronized void endGame() {
        if (!gameRunning) return;
        
        gameRunning = false;
        roomData.setStatus(RoomState.Status.FINISHED);
        
        // Stop timers
        if (gameTimer != null && !gameTimer.isShutdown()) {
            gameTimer.shutdown();
        }
        
        // Calculate winner
        List<PlayerState> topPlayers = roomData.getTopPlayers();
        String winner = topPlayers.isEmpty() ? "No Winner" : topPlayers.get(0).getUsername();
        
        // Create game result
        Map<String, Object> gameResult = new HashMap<>();
        gameResult.put("winner", winner);
        gameResult.put("scores", topPlayers);
        gameResult.put("duration", (System.currentTimeMillis() - gameStartTime) / 1000);
        
        // Notify all players
        Message gameEndMsg = new Message(Message.Type.GAME_END, gameResult, null);
        broadcastMessage(gameEndMsg);
        
        Logger.logSync("Game ended in room " + roomData.getRoomId() + ". Winner: " + winner);
        
        // Reset room to waiting state after a delay
        resetRoomAfterDelay();
    }
    
    private void resetRoomAfterDelay() {
        Timer resetTimer = new Timer();
        resetTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                roomData.setStatus(RoomState.Status.WAITING);
                // Reset all players
                for (PlayerState player : roomData.getPlayers().values()) {
                    player.resetToDefault();
                    player.setReady(false);
                }
                sendRoomUpdate();
                resetTimer.cancel();
            }
        }, 5000); // Reset after 5 seconds
    }
    
    public void handlePlayerMove(String username, int x, int y) {
        PlayerState player = roomData.getPlayers().get(username);
        if (player != null && player.isAlive() && gameRunning) {
            player.setX(x);
            player.setY(y);
            
            // Broadcast movement to other players
            Message moveMsg = new Message(Message.Type.MOVE, player, username);
            broadcastToOthers(moveMsg, username);
        }
    }
    
    public void handlePlayerAttack(String username, String targetUsername) {
        PlayerState attacker = roomData.getPlayers().get(username);
        PlayerState target = roomData.getPlayers().get(targetUsername);
        
        if (attacker != null && target != null && 
            attacker.isAlive() && target.isAlive() && gameRunning) {
            
            // Check attack cooldown
            if (!attacker.canAttack()) {
                return;
            }
            
            // Calculate damage
            int damage = attacker.getDamage();
            target.takeDamage(damage);
            attacker.setLastAttackTime(System.currentTimeMillis());
            
            // Check if target died
            if (!target.isAlive()) {
                target.die();
                attacker.addKill();
                
                // Send score update
                Message scoreMsg = new Message(Message.Type.SCORE_UPDATE, 
                    roomData.getPlayers(), null);
                broadcastMessage(scoreMsg);
                
                Logger.logSync(username + " killed " + targetUsername + " in room " + roomData.getRoomId());
            }
            
            // Broadcast attack result
            Map<String, Object> attackData = new HashMap<>();
            attackData.put("attacker", attacker);
            attackData.put("target", target);
            attackData.put("damage", damage);
            
            Message attackMsg = new Message(Message.Type.ATTACK, attackData, username);
            broadcastMessage(attackMsg);
        }
    }
    
    private void sendRoomUpdate() {
        Message roomUpdateMsg = new Message(Message.Type.ROOM_UPDATE, roomData, null);
        broadcastMessage(roomUpdateMsg);
    }
    
    private void broadcastMessage(Message message) {
        for (ClientHandler handler : clientHandlers.values()) {
            if (handler != null) {
                handler.sendMessage(message);
            }
        }
    }
    
    private void broadcastToOthers(Message message, String excludeUsername) {
        for (Map.Entry<String, ClientHandler> entry : clientHandlers.entrySet()) {
            if (!entry.getKey().equals(excludeUsername) && entry.getValue() != null) {
                entry.getValue().sendMessage(message);
            }
        }
    }
    
    public void shutdown() {
        gameRunning = false;
        
        if (gameTimer != null && !gameTimer.isShutdown()) {
            gameTimer.shutdown();
        }
        
        if (respawnTimer != null && !respawnTimer.isShutdown()) {
            respawnTimer.shutdown();
        }
        
        clientHandlers.clear();
        Logger.logSync("Room " + roomData.getRoomId() + " shutdown");
    }
    
    // Getters
    public RoomState getRoomData() { return roomData; }
    public boolean isGameRunning() { return gameRunning; }
    public int getPlayerCount() { return clientHandlers.size(); }
    public boolean isEmpty() { return clientHandlers.isEmpty(); }
    public int getGameId() { return gameId; }
    public int getGameDurationSeconds() { return gameDurationSeconds; }
    public void setGameDurationSeconds(int seconds) { this.gameDurationSeconds = seconds; }
}