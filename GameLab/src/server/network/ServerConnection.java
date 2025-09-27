package server.network;

import server.utils.Logger;
import shared.network.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Manages server-side network connections and client handlers
 * Provides centralized connection management and broadcasting capabilities
 */
public class ServerConnection {
    private static ServerConnection instance;
    private ServerSocket serverSocket;
    private ExecutorService clientExecutor;
    private Map<String, ClientHandler> connectedClients;
    private boolean isRunning;
    private int port;
    private int maxConnections;
    
    private ServerConnection() {
        this.connectedClients = new ConcurrentHashMap<>();
        this.isRunning = false;
        this.maxConnections = 100;
    }
    
    public static synchronized ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }
    
    public boolean start(int port) {
        this.port = port;
        
        try {
            serverSocket = new ServerSocket(port);
            clientExecutor = Executors.newFixedThreadPool(maxConnections);
            isRunning = true;
            
            Logger.logSync("ServerConnection started on port: " + port);
            return true;
            
        } catch (IOException e) {
            Logger.logSync("Failed to start ServerConnection: " + e.getMessage());
            return false;
        }
    }
    
    public void acceptConnections() {
        while (isRunning && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                
                if (connectedClients.size() < maxConnections) {
                    String clientId = generateClientId(clientSocket);
                    ClientHandler handler = new ClientHandler(clientSocket);
                    
                    connectedClients.put(clientId, handler);
                    clientExecutor.submit(handler);
                    
                    Logger.logSync("New client connected: " + clientId);
                } else {
                    Logger.logSync("Connection limit reached, rejecting client");
                    clientSocket.close();
                }
                
            } catch (IOException e) {
                if (isRunning) {
                    Logger.logSync("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }
    
    public void registerClient(String username, ClientHandler handler) {
        connectedClients.put(username, handler);
        Logger.logSync("Client registered: " + username);
    }
    
    public void unregisterClient(String username) {
        ClientHandler removed = connectedClients.remove(username);
        if (removed != null) {
            Logger.logSync("Client unregistered: " + username);
        }
    }
    
    public void broadcastMessage(Message message) {
        List<ClientHandler> disconnectedClients = new ArrayList<>();
        
        for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {
            ClientHandler handler = entry.getValue();
            
            if (handler.isConnected()) {
                handler.sendMessage(message);
            } else {
                disconnectedClients.add(handler);
            }
        }
        
        // Clean up disconnected clients
        for (ClientHandler handler : disconnectedClients) {
            String username = handler.getUsername();
            if (username != null) {
                unregisterClient(username);
            }
        }
    }
    
    public void broadcastToRoom(String roomId, Message message) {
        for (ClientHandler handler : connectedClients.values()) {
            if (handler.isConnected() && roomId.equals(handler.getCurrentRoomId())) {
                handler.sendMessage(message);
            }
        }
    }
    
    public void sendMessageToUser(String username, Message message) {
        ClientHandler handler = connectedClients.get(username);
        if (handler != null && handler.isConnected()) {
            handler.sendMessage(message);
        }
    }
    
    public void broadcastToOthers(String excludeUsername, Message message) {
        for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {
            if (!entry.getKey().equals(excludeUsername)) {
                ClientHandler handler = entry.getValue();
                if (handler.isConnected()) {
                    handler.sendMessage(message);
                }
            }
        }
    }
    
    public void broadcastToRoomExclude(String roomId, String excludeUsername, Message message) {
        for (ClientHandler handler : connectedClients.values()) {
            if (handler.isConnected() && 
                roomId.equals(handler.getCurrentRoomId()) && 
                !excludeUsername.equals(handler.getUsername())) {
                handler.sendMessage(message);
            }
        }
    }
    
    public boolean isUserConnected(String username) {
        ClientHandler handler = connectedClients.get(username);
        return handler != null && handler.isConnected();
    }
    
    public ClientHandler getClientHandler(String username) {
        return connectedClients.get(username);
    }
    
    public List<String> getConnectedUsernames() {
        return new ArrayList<>(connectedClients.keySet());
    }
    
    public List<String> getUsersInRoom(String roomId) {
        List<String> usersInRoom = new ArrayList<>();
        
        for (ClientHandler handler : connectedClients.values()) {
            if (handler.isConnected() && roomId.equals(handler.getCurrentRoomId())) {
                String username = handler.getUsername();
                if (username != null) {
                    usersInRoom.add(username);
                }
            }
        }
        
        return usersInRoom;
    }
    
    public int getConnectedCount() {
        return connectedClients.size();
    }
    
    public int getActiveConnectionsCount() {
        return (int) connectedClients.values().stream()
            .filter(ClientHandler::isConnected)
            .count();
    }
    
    public void disconnectUser(String username) {
        ClientHandler handler = connectedClients.get(username);
        if (handler != null) {
            handler.disconnect();
            unregisterClient(username);
        }
    }
    
    public void disconnectAllClients() {
        Logger.logSync("Disconnecting all clients...");
        
        for (ClientHandler handler : connectedClients.values()) {
            handler.disconnect();
        }
        
        connectedClients.clear();
        Logger.logSync("All clients disconnected");
    }
    
    public void stop() {
        Logger.logSync("Stopping ServerConnection...");
        isRunning = false;
        
        // Disconnect all clients
        disconnectAllClients();
        
        // Shutdown executor
        if (clientExecutor != null && !clientExecutor.isShutdown()) {
            clientExecutor.shutdown();
        }
        
        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Logger.logSync("Error closing server socket: " + e.getMessage());
        }
        
        Logger.logSync("ServerConnection stopped");
    }
    
    private String generateClientId(Socket socket) {
        return "client_" + socket.getInetAddress().getHostAddress() + "_" + 
               socket.getPort() + "_" + System.currentTimeMillis();
    }
    
    // Statistics and monitoring
    public Map<String, Object> getConnectionStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalConnections", connectedClients.size());
        stats.put("activeConnections", getActiveConnectionsCount());
        stats.put("maxConnections", maxConnections);
        stats.put("serverPort", port);
        stats.put("isRunning", isRunning);
        
        return stats;
    }
    
    // Getters and setters
    public boolean isRunning() { return isRunning; }
    public int getPort() { return port; }
    public int getMaxConnections() { return maxConnections; }
    public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
}