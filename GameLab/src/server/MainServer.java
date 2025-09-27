package server;

import server.core.GameEngine;
import server.database.DatabaseManager;
import server.network.ClientHandler;
import server.utils.Logger;
import shared.utils.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer {
    private static final int PORT = Constants.SERVER_PORT;
    private static final int MAX_CONNECTIONS = 100;
    
    private ServerSocket serverSocket;
    private ExecutorService clientThreadPool;
    private GameEngine gameEngine;
    private DatabaseManager databaseManager;
    private boolean isRunning;
    private int connectedClients;
    
    public MainServer() {
        this.clientThreadPool = Executors.newFixedThreadPool(MAX_CONNECTIONS);
        this.gameEngine = GameEngine.getInstance();
        this.databaseManager = DatabaseManager.getInstance();
        this.isRunning = false;
        this.connectedClients = 0;
    }
    
    public void start() {
        try {
            // USE: Synchronous logging for critical startup messages
            Logger.logSync("Starting server initialization...");
            Logger.logSync("Testing logger output - this should appear in both console and file");
            
            // Initialize database
            Logger.logSync("Attempting to connect to database...");
            boolean dbConnected = false;
            try {
                dbConnected = databaseManager.initialize();
            } catch (Exception e) {
                Logger.errorSync("Database initialization failed: " + e.getMessage());
            }
            
            if (!dbConnected) {
                Logger.warnSync("WARNING: Database connection failed. Server will run without database features.");
                Logger.logSync("Players can still connect and play, but stats won't be saved.");
                // CHANGE: Don't return - continue without database
            } else {
                Logger.logSync("Database connected successfully!");
            }
            
            // Create server socket
            Logger.logSync("Creating server socket on port " + PORT + "...");
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            
            // USE: Synchronous for startup completion messages
            Logger.logSync("=== FIGHTING GAME SERVER STARTED ===");
            Logger.logSync("Server listening on port: " + PORT);
            Logger.logSync("Max connections: " + MAX_CONNECTIONS);
            Logger.logSync("Database connected: " + databaseManager.isConnected());
            Logger.logSync("Server ready to accept client connections!");
            Logger.logSync("=====================================");
            
            // Accept client connections
            while (isRunning) {
                try {
                    Logger.logSync("Waiting for client connections...");
                    Socket clientSocket = serverSocket.accept();
                    
                    if (connectedClients < MAX_CONNECTIONS) {
                        ClientHandler clientHandler = new ClientHandler(clientSocket);
                        clientThreadPool.submit(clientHandler);
                        connectedClients++;
                        
                        // USE: Synchronous for important connection events
                        Logger.logSync("New client connected from: " + clientSocket.getInetAddress());
                        Logger.logSync("Total connected clients: " + connectedClients);
                    } else {
                        Logger.warnSync("Maximum connections reached. Rejecting client: " + 
                                     clientSocket.getInetAddress());
                        clientSocket.close();
                    }
                    
                } catch (IOException e) {
                    if (isRunning) {
                        Logger.errorSync("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            Logger.errorSync("FATAL: Failed to start server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            stop();
        }
    }
    
    public void stop() {
        Logger.logSync("Initiating server shutdown...");
        isRunning = false;
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                Logger.logSync("Server socket closed successfully");
            }
        } catch (IOException e) {
            Logger.errorSync("Error closing server socket: " + e.getMessage());
        }
        
        // Shutdown game engine
        try {
            if (gameEngine != null) {
                gameEngine.shutdown();
                Logger.logSync("Game engine shutdown completed");
            }
        } catch (Exception e) {
            Logger.errorSync("Error shutting down game engine: " + e.getMessage());
        }
        
        // Shutdown thread pool
        try {
            if (clientThreadPool != null && !clientThreadPool.isShutdown()) {
                clientThreadPool.shutdown();
                Logger.logSync("Client thread pool shutdown completed");
            }
        } catch (Exception e) {
            Logger.errorSync("Error shutting down thread pool: " + e.getMessage());
        }
        
        // Close database connections
        try {
            if (databaseManager != null) {
                databaseManager.close();
                Logger.logSync("Database connections closed");
            }
        } catch (Exception e) {
            Logger.errorSync("Error closing database: " + e.getMessage());
        }
        
        Logger.logSync("Server shutdown completed successfully");
        
        // IMPORTANT: Flush all logs before shutdown
        Logger.flushLogs();
    }
    
    public void clientDisconnected() {
        connectedClients--;
        Logger.logSync("Client disconnected. Total clients: " + connectedClients);
    }
    
    public int getConnectedClients() {
        return connectedClients;
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    // Main method
    public static void main(String[] args) {
        System.out.println("=== FIGHTING GAME SERVER ===");
        System.out.println("Initializing server...");
        
        MainServer server = new MainServer();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.logSync("Shutdown signal received from system");
            server.stop();
            
            // IMPORTANT: Final flush and shutdown
            Logger.flushLogs();
            try {
                Thread.sleep(500); // Give logger time to write
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Logger.getInstance().shutdown();
        }));
        
        // Start server
        server.start();
    }
}