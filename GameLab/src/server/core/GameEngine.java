package server.core;

import shared.models.GameHistory;
import shared.models.RoomState;
import shared.network.Message;
import server.network.ClientHandler;
import server.database.DatabaseManager;
import server.utils.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GameEngine {
    private static GameEngine instance;
    private Map<String, GameRoom> rooms;
    private AtomicInteger roomIdCounter;
    private AtomicInteger gameIdCounter;
    private DatabaseManager dbManager;
    
    private GameEngine() {
        this.rooms = new ConcurrentHashMap<>();
        this.roomIdCounter = new AtomicInteger(1000);
        this.gameIdCounter = new AtomicInteger(1);
        this.dbManager = DatabaseManager.getInstance();
    }
    
    public static synchronized GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }
        return instance;
    }
    
    public synchronized String createRoom(String hostUsername, ClientHandler clientHandler) {
        String roomId = "ROOM" + roomIdCounter.incrementAndGet();
        int gameId = gameIdCounter.incrementAndGet();
        
        GameRoom gameRoom = new GameRoom(roomId, hostUsername, gameId);
        
        if (gameRoom.addPlayer(hostUsername, clientHandler)) {
            rooms.put(roomId, gameRoom);
            Logger.logSync("Room " + roomId + " created by " + hostUsername);
            return roomId;
        }
        
        return null;
    }
    
    public boolean joinRoom(String roomId, String username, ClientHandler clientHandler) {
        GameRoom room = rooms.get(roomId);
        if (room != null) {
            return room.addPlayer(username, clientHandler);
        }
        return false;
    }
    
    public void leaveRoom(String roomId, String username) {
        GameRoom room = rooms.get(roomId);
        if (room != null) {
            room.removePlayer(username);
            
            // Remove empty rooms
            if (room.isEmpty()) {
                rooms.remove(roomId);
                room.shutdown();
                Logger.logSync("Room " + roomId + " removed (empty)");
            }
        }
    }
    
    public void setPlayerReady(String roomId, String username, boolean ready) {
        GameRoom room = rooms.get(roomId);
        if (room != null) {
            room.setPlayerReady(username, ready);
        }
    }
    
    public boolean startGame(String roomId, String hostUsername) {
        GameRoom room = rooms.get(roomId);
        if (room != null && room.getRoomData().getHostUsername().equals(hostUsername)) {
            return room.startGame();
        }
        return false;
    }
    
    public void handlePlayerMove(String roomId, String username, int x, int y) {
        GameRoom room = rooms.get(roomId);
        if (room != null) {
            room.handlePlayerMove(username, x, y);
        }
    }
    
    public void handlePlayerAttack(String roomId, String username, String targetUsername) {
        GameRoom room = rooms.get(roomId);
        if (room != null) {
            room.handlePlayerAttack(username, targetUsername);
        }
    }
    
    public List<RoomState> getAvailableRooms() {
        List<RoomState> availableRooms = new ArrayList<>();
        for (GameRoom gameRoom : rooms.values()) {
            RoomState roomState = gameRoom.getRoomData();
            // Only include waiting rooms that are not full
            if (roomState.getStatus() == RoomState.Status.WAITING && !roomState.isFull()) {
                availableRooms.add(roomState);
            }
        }
        return availableRooms;
    }
    
    public List<RoomState> getAllRooms() {
        List<RoomState> allRooms = new ArrayList<>();
        for (GameRoom gameRoom : rooms.values()) {
            allRooms.add(gameRoom.getRoomData());
        }
        return allRooms;
    }
    
    public void saveGameHistory(String roomId) {
        GameRoom room = rooms.get(roomId);
        if (room != null && dbManager != null) {
            try {
                RoomState roomData = room.getRoomData();
                List<String> players = new ArrayList<>(roomData.getPlayers().keySet());
                
                // Get winner (player with highest score)
                String winner = roomData.getTopPlayers().isEmpty() ? 
                    "No Winner" : roomData.getTopPlayers().get(0).getUsername();
                
                int duration = (int) ((System.currentTimeMillis() - roomData.getCreatedTime()) / 1000);
                
                GameHistory history = new GameHistory(room.getGameId(), roomId, players, winner, duration);
                
                dbManager.saveGameHistory(history, roomData.getPlayers());
                
                Logger.logSync("Game history saved for room " + roomId);
                
            } catch (Exception e) {
                Logger.logSync("Error saving game history: " + e.getMessage());
            }
        }
    }
    
    public GameRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }
    
    public void shutdown() {
        Logger.logSync("Shutting down Game Engine...");
        
        for (GameRoom room : rooms.values()) {
            room.shutdown();
        }
        
        rooms.clear();
        Logger.logSync("Game Engine shutdown complete");
    }
    
    // Statistics
    public int getTotalRooms() {
        return rooms.size();
    }
    
    public int getActiveGames() {
        return (int) rooms.values().stream()
            .filter(GameRoom::isGameRunning)
            .count();
    }
    
    public int getTotalPlayers() {
        return rooms.values().stream()
            .mapToInt(GameRoom::getPlayerCount)
            .sum();
    }
}