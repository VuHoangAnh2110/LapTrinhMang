package server;

import server.database.DatabaseManager;
import server.database.UserDAO;
import shared.models.Player;
import shared.models.Message;
import shared.models.GameState;
import java.net.*;
import java.io.*;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class GameServer {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private Map<String, ClientHandler> connectedClients;
    private GameEngine gameEngine;
    private UserDAO userDAO;
    
    public GameServer() {
        connectedClients = new ConcurrentHashMap<>();
        gameEngine = new GameEngine(this);
        
        try {
            DatabaseManager.getInstance();
            userDAO = new UserDAO();
            System.out.println("Database connection established successfully");
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            System.err.println("Please make sure MySQL is running and database 'fighting_game' exists");
            System.exit(1);
        }
    }
    
    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        isRunning = true;
        System.out.println("Game Server started on port " + PORT);
        
        new Thread(gameEngine).start();
        
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                new Thread(clientHandler).start();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }
    
    public synchronized void addClient(String username, ClientHandler handler) {
        connectedClients.put(username, handler);
        
        Player newPlayer = new Player(connectedClients.size(), username);
        gameEngine.addPlayer(newPlayer);
        
        System.out.println("Player " + username + " joined the game. Total players: " + connectedClients.size());
        
        broadcastMessage(new Message(Message.Type.PLAYER_JOINED, null, username), username);
    }
    
    public synchronized void removeClient(String username) {
        connectedClients.remove(username);
        gameEngine.removePlayer(username);
        System.out.println("Player " + username + " left the game. Total players: " + connectedClients.size());
        
        broadcastMessage(new Message(Message.Type.PLAYER_LEFT, null, username), null);
    }
    
    public void broadcastMessage(Object message, String excludeUser) {
        System.out.println("Broadcasting message to " + connectedClients.size() + " clients" + 
            (excludeUser != null ? " (excluding " + excludeUser + ")" : ""));
        
        for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {
            if (!entry.getKey().equals(excludeUser)) {
                entry.getValue().sendMessage(message);
            }
        }
    }
    
    public void broadcastGameState() {
        GameState gameState = gameEngine.getGameState();
        
        System.out.println("Broadcasting GameState: " + gameState.toString());
        System.out.println("Connected clients: " + connectedClients.keySet());
        
        for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {
            // Create a fresh copy for each client to avoid reference issues
            GameState freshCopy = new GameState(gameState);
            System.out.println("Sending GameState to " + entry.getKey() + " with " + 
                freshCopy.getPlayers().size() + " players");
            entry.getValue().sendMessage(freshCopy);
        }
        
        System.out.println("Finished broadcasting GameState to all clients");
    }
    
    public void handlePlayerMove(String username, Object positionData) {
        gameEngine.updatePlayerPosition(username, positionData);
        broadcastGameState();
    }
    
    public void handlePlayerAttack(String username) {
        gameEngine.processAttack(username);
        broadcastGameState();
    }
    
    public void notifyGameEnd(String winner) {
        for (ClientHandler handler : connectedClients.values()) {
            handler.notifyGameEnd(winner);
        }
        
        if (connectedClients.size() == 2) {
            String[] players = connectedClients.keySet().toArray(new String[0]);
            if (players.length == 2) {
                userDAO.saveGameSession(players[0], players[1], winner, 0);
            }
        }
    }
    
    public GameEngine getGameEngine() {
        return gameEngine;
    }
    
    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        try {
            GameServer server = new GameServer();
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                server.stop();
            }));
            
            server.start();
        } catch (IOException e) {
            System.err.println("Server failed to start: " + e.getMessage());
        }
    }
}