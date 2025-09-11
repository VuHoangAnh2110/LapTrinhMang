package server;

import server.database.DatabaseManager;
import shared.models.Player;
import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class GameServer {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private Map<String, ClientHandler> connectedClients;
    private GameEngine gameEngine;
    
    public GameServer() {
        connectedClients = new ConcurrentHashMap<>();
        gameEngine = new GameEngine();
    }
    
    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        isRunning = true;
        System.out.println("Game Server started on port " + PORT);
        
        // Start game engine thread
        new Thread(gameEngine).start();
        
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                new Thread(clientHandler).start();
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }
    
    public void addClient(String username, ClientHandler handler) {
        connectedClients.put(username, handler);
        gameEngine.addPlayer(new Player(connectedClients.size(), username));
        System.out.println("Player " + username + " joined the game");
    }
    
    public void removeClient(String username) {
        connectedClients.remove(username);
        gameEngine.removePlayer(username);
        System.out.println("Player " + username + " left the game");
    }
    
    public void broadcastMessage(Object message, String excludeUser) {
        for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {
            if (!entry.getKey().equals(excludeUser)) {
                entry.getValue().sendMessage(message);
            }
        }
    }
    
    public GameEngine getGameEngine() {
        return gameEngine;
    }
    
    public static void main(String[] args) {
        try {
            GameServer server = new GameServer();
            server.start();
        } catch (IOException e) {
            System.err.println("Server failed to start: " + e.getMessage());
        }
    }
}