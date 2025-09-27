package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class MainServer {
    private static final int PORT = 8888;
    private ServerSocket serverSocket;
    private List<ClientHandler> waitingClients;
    private List<GameRoom> gameRooms;
    private boolean running;
    
    public MainServer() {
        waitingClients = new ArrayList<>();
        gameRooms = new ArrayList<>();
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("Server started on port " + PORT);
            
            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                
                synchronized (waitingClients) {
                    if (waitingClients.size() == 0) {
                        // First player waiting
                        waitingClients.add(clientHandler);
                        clientHandler.sendMessage("WAITING");
                        System.out.println("Player 1 waiting for opponent");
                    } else {
                        // Second player, create game room
                        ClientHandler player1 = waitingClients.remove(0);
                        GameRoom gameRoom = new GameRoom(player1, clientHandler, this);
                        gameRooms.add(gameRoom);
                        System.out.println("Game room created with 2 players");
                        gameRoom.startGame();
                    }
                }
                
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
    
    public synchronized void removeGameRoom(GameRoom gameRoom) {
        gameRooms.remove(gameRoom);
        System.out.println("Game room removed. Active rooms: " + gameRooms.size());
    }
    
    public synchronized void removeWaitingClient(ClientHandler client) {
        waitingClients.remove(client);
        System.out.println("Waiting client removed. Waiting: " + waitingClients.size());
    }
    
    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        MainServer server = new MainServer();
        server.start();
    }
}