package client.network;

import shared.network.Message;
import shared.utils.Constants;
import client.fx.utils.SceneManager;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class ClientConnection implements Runnable {
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String username;
    private boolean isConnected;
    private MessageListener messageListener;
    private String currentRoomId;
    
    public ClientConnection() {
        this.isConnected = false;
    }
    
    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            isConnected = true;
            
            // Start listening for messages
            new Thread(this).start();
            
            System.out.println("Connected to server: " + host + ":" + port);
            return true;
            
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public void run() {
        try {
            while (isConnected && socket != null && !socket.isClosed()) {
                Message message = (Message) input.readObject();
                handleMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (isConnected) {
                System.err.println("Connection lost: " + e.getMessage());
                disconnect();
            }
        }
    }
    
    private void handleMessage(Message message) {
        Platform.runLater(() -> {
            try {
                switch (message.getType()) {
                    case LOGIN_SUCCESS:
                        handleLoginSuccess(message);
                        break;
                        
                    case LOGIN_FAILED:
                        handleLoginFailed(message);
                        break;
                        
                    case REGISTER_SUCCESS:
                        handleRegisterSuccess(message);
                        break;
                        
                    case REGISTER_FAILED:
                        handleRegisterFailed(message);
                        break;
                        
                    case ROOM_CREATED:
                        handleRoomCreated(message);
                        break;
                        
                    case ROOM_JOINED:
                        handleRoomJoined(message);
                        break;
                        
                    case ROOM_FULL:
                        handleRoomFull(message);
                        break;
                        
                    case ROOMS_LIST:
                        handleRoomsList(message);
                        break;
                        
                    case ROOM_UPDATE:
                        handleRoomUpdate(message);
                        break;
                        
                    case PLAYER_JOINED:
                        handlePlayerJoined(message);
                        break;
                        
                    case PLAYER_LEFT:
                        handlePlayerLeft(message);
                        break;
                        
                    case START_GAME:
                        handleGameStart(message);
                        break;
                        
                    case GAME_STATE:
                        handleGameState(message);
                        break;
                        
                    case GAME_END:
                        handleGameEnd(message);
                        break;
                        
                    case GAME_TIMER:
                        handleGameTimer(message);
                        break;
                        
                    case MOVE:
                        handlePlayerMove(message);
                        break;
                        
                    case ATTACK:
                        handlePlayerAttack(message);
                        break;
                        
                    case PLAYER_RESPAWN:
                        handlePlayerRespawn(message);
                        break;
                        
                    case SCORE_UPDATE:
                        handleScoreUpdate(message);
                        break;
                        
                    case HISTORY_DATA:
                        handleHistoryData(message);
                        break;
                        
                    case USER_STATS:
                        handleUserStats(message);
                        break;
                        
                    default:
                        System.out.println("Unhandled message type: " + message.getType());
                }
                
                // Notify message listener if set
                if (messageListener != null) {
                    messageListener.onMessageReceived(message);
                }
                
            } catch (Exception e) {
                System.err.println("Error handling message: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private void handleLoginSuccess(Message message) {
        this.username = message.getUsername();
        try {
            SceneManager.getInstance().setGameClient(this);
            SceneManager.getInstance().showMainMenuScene();
        } catch (Exception e) {
            System.err.println("Error switching to main menu: " + e.getMessage());
        }
    }
    
    private void handleLoginFailed(Message message) {
        System.out.println("Login failed: " + message.getMessage());
        // LoginController will handle this through MessageListener
    }
    
    private void handleRegisterSuccess(Message message) {
        System.out.println("Registration successful: " + message.getMessage());
    }
    
    private void handleRegisterFailed(Message message) {
        System.out.println("Registration failed: " + message.getMessage());
    }
    
    private void handleRoomCreated(Message message) {
        currentRoomId = (String) message.getData();
        System.out.println("Room created: " + currentRoomId);
        
        try {
            SceneManager.getInstance().showGameScene();
        } catch (Exception e) {
            System.err.println("Error switching to game scene: " + e.getMessage());
        }
    }
    
    private void handleRoomJoined(Message message) {
        currentRoomId = (String) message.getData();
        System.out.println("Joined room: " + currentRoomId);
        
        try {
            SceneManager.getInstance().showGameScene();
        } catch (Exception e) {
            System.err.println("Error switching to game scene: " + e.getMessage());
        }
    }
    
    private void handleRoomFull(Message message) {
        System.out.println("Room operation failed: " + message.getMessage());
    }
    
    private void handleRoomsList(Message message) {
        @SuppressWarnings("unchecked")
        List<shared.models.RoomState> rooms = (List<shared.models.RoomState>) message.getData();
        System.out.println("Received rooms list: " + rooms.size() + " rooms");
    }
    
    private void handleRoomUpdate(Message message) {
        shared.models.RoomState room = (shared.models.RoomState) message.getData();
        System.out.println("Room updated: " + room.getRoomId());
    }
    
    private void handlePlayerJoined(Message message) {
        shared.models.PlayerState player = (shared.models.PlayerState) message.getData();
        System.out.println("Player joined: " + player.getUsername());
    }
    
    private void handlePlayerLeft(Message message) {
        shared.models.PlayerState player = (shared.models.PlayerState) message.getData();
        System.out.println("Player left: " + player.getUsername());
    }
    
    private void handleGameStart(Message message) {
        shared.models.RoomState room = (shared.models.RoomState) message.getData();
        System.out.println("Game started in room: " + room.getRoomId());
    }
    
    private void handleGameState(Message message) {
        shared.models.RoomState room = (shared.models.RoomState) message.getData();
        // GameController will handle this through MessageListener
    }
    
    private void handleGameEnd(Message message) {
        @SuppressWarnings("unchecked")
        Map<String, Object> gameResult = (Map<String, Object>) message.getData();
        System.out.println("Game ended. Winner: " + gameResult.get("winner"));
    }
    
    private void handleGameTimer(Message message) {
        Long remainingTime = (Long) message.getData();
        // GameController will handle this through MessageListener
    }
    
    private void handlePlayerMove(Message message) {
        shared.models.PlayerState player = (shared.models.PlayerState) message.getData();
        // GameController will handle this through MessageListener
    }
    
    private void handlePlayerAttack(Message message) {
        @SuppressWarnings("unchecked")
        Map<String, Object> attackData = (Map<String, Object>) message.getData();
        // GameController will handle this through MessageListener
    }
    
    private void handlePlayerRespawn(Message message) {
        shared.models.PlayerState player = (shared.models.PlayerState) message.getData();
        System.out.println("Player respawned: " + player.getUsername());
    }
    
    private void handleScoreUpdate(Message message) {
        @SuppressWarnings("unchecked")
        Map<String, shared.models.PlayerState> players = 
            (Map<String, shared.models.PlayerState>) message.getData();
        // GameController will handle this through MessageListener
    }
    
    private void handleHistoryData(Message message) {
        @SuppressWarnings("unchecked")
        List<shared.models.GameHistory> history = 
            (List<shared.models.GameHistory>) message.getData();
        // HistoryController will handle this through MessageListener
    }
    
    private void handleUserStats(Message message) {
        @SuppressWarnings("unchecked")
        Map<String, Integer> stats = (Map<String, Integer>) message.getData();
        // MainMenuController will handle this through MessageListener
    }
    
    public void sendMessage(Message message) {
        if (isConnected && output != null) {
            try {
                output.writeObject(message);
                output.flush();
            } catch (IOException e) {
                System.err.println("Error sending message: " + e.getMessage());
                disconnect();
            }
        }
    }
    
    public boolean login(String username, String password) {
        Map<String, String> credentials = Map.of(
            "username", username,
            "password", password
        );
        Message loginMessage = new Message(Message.Type.LOGIN, credentials, username);
        sendMessage(loginMessage);
        return true;
    }
    
    public boolean register(String username, String password, String email) {
        Map<String, String> userData = Map.of(
            "username", username,
            "password", password,
            "email", email != null ? email : ""
        );
        Message registerMessage = new Message(Message.Type.REGISTER, userData, username);
        sendMessage(registerMessage);
        return true;
    }
    
    public void createRoom() {
        if (username != null) {
            Message createRoomMsg = new Message(Message.Type.CREATE_ROOM, null, username);
            sendMessage(createRoomMsg);
        }
    }
    
    public void joinRoom(String roomId) {
        if (username != null) {
            Message joinRoomMsg = new Message(Message.Type.JOIN_ROOM, roomId, username);
            sendMessage(joinRoomMsg);
        }
    }
    
    public void leaveRoom() {
        if (username != null && currentRoomId != null) {
            Message leaveRoomMsg = new Message(Message.Type.LEAVE_ROOM, currentRoomId, username);
            sendMessage(leaveRoomMsg);
            currentRoomId = null;
        }
    }
    
    public void setPlayerReady(boolean ready) {
        if (username != null && currentRoomId != null) {
            Message readyMsg = new Message(Message.Type.PLAYER_READY, ready, username);
            sendMessage(readyMsg);
        }
    }
    
    public void startGame() {
        if (username != null && currentRoomId != null) {
            Message startMsg = new Message(Message.Type.START_GAME, null, username);
            sendMessage(startMsg);
        }
    }
    
    public void sendMove(int x, int y) {
        if (username != null && currentRoomId != null) {
            Map<String, Integer> moveData = Map.of("x", x, "y", y);
            Message moveMsg = new Message(Message.Type.MOVE, moveData, username);
            sendMessage(moveMsg);
        }
    }
    
    public void sendAttack(String targetUsername) {
        if (username != null && currentRoomId != null) {
            Message attackMsg = new Message(Message.Type.ATTACK, targetUsername, username);
            sendMessage(attackMsg);
        }
    }
    
    public void requestRooms() {
        if (username != null) {
            Message getRoomsMsg = new Message(Message.Type.GET_ROOMS, null, username);
            sendMessage(getRoomsMsg);
        }
    }
    
    public void requestHistory() {
        if (username != null) {
            Message historyMsg = new Message(Message.Type.GET_HISTORY, null, username);
            sendMessage(historyMsg);
        }
    }
    
    public void requestUserStats() {
        if (username != null) {
            Message statsMsg = new Message(Message.Type.USER_STATS, null, username);
            sendMessage(statsMsg);
        }
    }
    
    public void disconnect() {
        isConnected = false;
        
        if (username != null) {
            Message logoutMsg = new Message(Message.Type.LOGOUT, null, username);
            sendMessage(logoutMsg);
        }
        
        try {
            if (output != null) output.close();
            if (input != null) input.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
        
        System.out.println("Disconnected from server");
    }
    
    // Getters and setters
    public String getUsername() { return username; }
    public boolean isConnected() { return isConnected; }
    public String getCurrentRoomId() { return currentRoomId; }
    
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }
    
    public MessageListener getMessageListener() {
        return messageListener;
    }
}