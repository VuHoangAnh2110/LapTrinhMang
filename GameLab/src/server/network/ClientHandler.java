package server.network;

import shared.network.Message;
import shared.models.RoomState;
import server.core.GameEngine;
import server.database.DatabaseManager;
import server.utils.Logger;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private String username;
    private String currentRoomId;
    private boolean isConnected;
    private GameEngine gameEngine;
    private DatabaseManager dbManager;
    
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.isConnected = true;
        this.gameEngine = GameEngine.getInstance();
        this.dbManager = DatabaseManager.getInstance();
        
        try {
            this.output = new ObjectOutputStream(clientSocket.getOutputStream());
            this.input = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            Logger.logSync("Error setting up client streams: " + e.getMessage());
            disconnect();
        }
    }
    
    @Override
    public void run() {
        Logger.logSync("Client connected: " + clientSocket.getInetAddress());
        
        try {
            while (isConnected && !clientSocket.isClosed()) {
                Message message = (Message) input.readObject();
                handleMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            Logger.logSync("Client disconnected: " + (username != null ? username : "Unknown"));
        } finally {
            disconnect();
        }
    }
    
    private void handleMessage(Message message) {
        try {
            switch (message.getType()) {
                case LOGIN:
                    handleLogin(message);
                    break;
                    
                case REGISTER:
                    handleRegister(message);
                    break;
                    
                case CREATE_ROOM:
                    handleCreateRoom(message);
                    break;
                    
                case GET_ROOMS:
                    handleGetRooms(message);
                    break;
                    
                case JOIN_ROOM:
                    handleJoinRoom(message);
                    break;
                    
                case LEAVE_ROOM:
                    handleLeaveRoom(message);
                    break;
                    
                case PLAYER_READY:
                    handlePlayerReady(message);
                    break;
                    
                case START_GAME:
                    handleStartGame(message);
                    break;
                    
                case MOVE:
                    handleMove(message);
                    break;
                    
                case ATTACK:
                    handleAttack(message);
                    break;
                    
                case GET_HISTORY:
                    handleGetHistory(message);
                    break;
                    
                case USER_STATS:
                    handleUserStats(message);
                    break;
                    
                case LOGOUT:
                    handleLogout(message);
                    break;
                    
                default:
                    Logger.logSync("Unknown message type: " + message.getType());
            }
        } catch (Exception e) {
            Logger.logSync("Error handling message: " + e.getMessage());
        }
    }
    
    private void handleLogin(Message message) {
        @SuppressWarnings("unchecked")
        Map<String, String> credentials = (Map<String, String>) message.getData();
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        if (dbManager.validateUser(username, password)) {
            this.username = username;
            sendMessage(new Message(Message.Type.LOGIN_SUCCESS, "Login successful", username));
            Logger.logSync("User logged in: " + username);
        } else {
            sendMessage(new Message(Message.Type.LOGIN_FAILED, "Invalid credentials", username));
        }
    }
    
    private void handleRegister(Message message) {
        @SuppressWarnings("unchecked")
        Map<String, String> userData = (Map<String, String>) message.getData();
        String username = userData.get("username");
        String password = userData.get("password");
        
        if (dbManager.createUser(username, password)) {
            sendMessage(new Message(Message.Type.REGISTER_SUCCESS, "Registration successful", username));
            Logger.logSync("User registered: " + username);
        } else {
            sendMessage(new Message(Message.Type.REGISTER_FAILED, "Username already exists", username));
        }
    }
    
    private void handleCreateRoom(Message message) {
        if (username == null) return;
        
        String roomId = gameEngine.createRoom(username, this);
        if (roomId != null) {
            currentRoomId = roomId;
            sendMessage(new Message(Message.Type.ROOM_CREATED, roomId, username));
        } else {
            sendMessage(new Message(Message.Type.ROOM_FULL, "Failed to create room", username));
        }
    }
    
    private void handleGetRooms(Message message) {
        List<RoomState> rooms = gameEngine.getAllRooms();
        sendMessage(new Message(Message.Type.ROOMS_LIST, rooms, username));
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }
    
    private void handleJoinRoom(Message message) {
        if (username == null) return;
        
        String roomId = (String) message.getData();
        if (gameEngine.joinRoom(roomId, username, this)) {
            currentRoomId = roomId;
            sendMessage(new Message(Message.Type.ROOM_JOINED, roomId, username));
        } else {
            sendMessage(new Message(Message.Type.ROOM_FULL, "Cannot join room", username));
        }
    }
    
    private void handleLeaveRoom(Message message) {
        if (currentRoomId != null && username != null) {
            gameEngine.leaveRoom(currentRoomId, username);
            sendMessage(new Message(Message.Type.ROOM_LEFT, currentRoomId, username));
            currentRoomId = null;
        }
    }
    
    private void handlePlayerReady(Message message) {
        if (currentRoomId != null && username != null) {
            Boolean ready = (Boolean) message.getData();
            gameEngine.setPlayerReady(currentRoomId, username, ready);
        }
    }
    
    private void handleStartGame(Message message) {
        if (currentRoomId != null && username != null) {
            gameEngine.startGame(currentRoomId, username);
        }
    }
    
    private void handleMove(Message message) {
        if (currentRoomId != null && username != null) {
            @SuppressWarnings("unchecked")
            Map<String, Integer> moveData = (Map<String, Integer>) message.getData();
            int x = moveData.get("x");
            int y = moveData.get("y");
            gameEngine.handlePlayerMove(currentRoomId, username, x, y);
        }
    }
    
    private void handleAttack(Message message) {
        if (currentRoomId != null && username != null) {
            String targetUsername = (String) message.getData();
            gameEngine.handlePlayerAttack(currentRoomId, username, targetUsername);
        }
    }
    
    private void handleGetHistory(Message message) {
        if (username != null) {
            // TODO: Implement getting user's game history from database
            // List<GameHistory> history = dbManager.getUserGameHistory(username);
            // sendMessage(new Message(Message.Type.HISTORY_DATA, history, username));
            
            // For now, send empty list
            sendMessage(new Message(Message.Type.HISTORY_DATA, List.of(), username));
        }
    }
    
    private void handleUserStats(Message message) {
        if (username != null) {
            // TODO: Get user statistics from database
            // Map<String, Integer> stats = dbManager.getUserStats(username);
            // sendMessage(new Message(Message.Type.USER_STATS, stats, username));
            
            // For now, send dummy stats
            Map<String, Integer> stats = Map.of("wins", 0, "losses", 0);
            sendMessage(new Message(Message.Type.USER_STATS, stats, username));
        }
    }
    
    private void handleLogout(Message message) {
        Logger.logSync("User logged out: " + username);
        disconnect();
    }
    
    public void sendMessage(Message message) {
        try {
            if (output != null && isConnected) {
                output.writeObject(message);
                output.flush();
            }
        } catch (IOException e) {
            Logger.logSync("Error sending message to client: " + e.getMessage());
            disconnect();
        }
    }
    
    public void disconnect() {
        isConnected = false;
        
        // Leave current room if any
        if (currentRoomId != null && username != null) {
            gameEngine.leaveRoom(currentRoomId, username);
        }
        
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            Logger.logSync("Error closing client connection: " + e.getMessage());
        }
        
        Logger.logSync("Client disconnected: " + (username != null ? username : "Unknown"));
    }
    
    public String getUsername() {
        return username;
    }
    
    public boolean isConnected() {
        return isConnected;
    }
}