package server;

import shared.models.Message;
import shared.models.Player;
import server.database.UserDAO;
import java.net.Socket;
import java.io.*;
import java.sql.SQLException;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private GameServer server;
    private String username;
    private boolean isConnected;
    private boolean isAuthenticated;
    private UserDAO userDAO;
    
    public ClientHandler(Socket socket, GameServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.isConnected = true;
        this.isAuthenticated = false;
        
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            userDAO = new UserDAO();
            System.out.println("Client handler created for: " + socket.getInetAddress());
        } catch (IOException | SQLException e) {
            System.err.println("Error setting up client streams: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        try {
            while (isConnected) {
                Message message = (Message) input.readObject();
                processMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Client disconnected: " + e.getMessage());
        } finally {
            disconnect();
        }
    }
    
    private void processMessage(Message message) {
        System.out.println("Processing message: " + message.getType() + " from " + message.getUsername());
        
        switch (message.getType()) {
            case LOGIN:
                handleLogin(message);
                break;
            case REGISTER:
                handleRegister(message);
                break;
            case JOIN_ROOM:
                if (isAuthenticated) handleJoinRoom(message);
                break;
            case MOVE:
                if (isAuthenticated) handleMove(message);
                break;
            case ATTACK:
                if (isAuthenticated) handleAttack(message);
                break;
            case LOGOUT:
                disconnect();
                break;
        }
    }
    
    private void handleLogin(Message message) {
        String[] credentials = (String[]) message.getData();
        String username = credentials[0];
        String password = credentials[1];
        
        System.out.println("Login attempt for user: " + username);
        
        if (userDAO.authenticateUser(username, password)) {
            this.username = username;
            this.isAuthenticated = true;
            
            System.out.println("Login successful for: " + username);
            
            // Send success response
            sendMessage(new Message(Message.Type.LOGIN_SUCCESS, null, username));
            
            // Add to game
            server.addClient(username, this);
            
            // Send user stats
            int[] stats = userDAO.getUserStats(username);
            sendMessage(new Message(Message.Type.USER_STATS, stats, username));
            
        } else {
            System.out.println("Login failed for: " + username);
            sendMessage(new Message(Message.Type.LOGIN_FAILED, null, username, "Invalid credentials"));
        }
    }
    
    private void handleJoinRoom(Message message) {
        System.out.println("Handling JOIN_ROOM for: " + username);
        // Send current game state
        sendMessage(server.getGameEngine().getGameState());
        System.out.println("Sent current game state to: " + username);
    }
    
    private void handleRegister(Message message) {
        String[] credentials = (String[]) message.getData();
        String username = credentials[0];
        String password = credentials[1];
        
        System.out.println("Registration attempt for user: " + username);
        
        if (userDAO.userExists(username)) {
            sendMessage(new Message(Message.Type.REGISTER_FAILED, null, username, "Username already exists"));
        } else if (userDAO.createUser(username, password)) {
            System.out.println("Registration successful for: " + username);
            sendMessage(new Message(Message.Type.REGISTER_SUCCESS, null, username, "Account created successfully"));
        } else {
            sendMessage(new Message(Message.Type.REGISTER_FAILED, null, username, "Failed to create account"));
        }
    }
    
    private void handleMove(Message message) {
        // Use server's centralized method for movement
        server.handlePlayerMove(username, message.getData());
    }
    
    private void handleAttack(Message message) {
        // Use server's centralized method for attacks
        server.handlePlayerAttack(username);
    }
    
    public void sendMessage(Object message) {
        try {
            if (output != null) {
                output.writeObject(message);
                output.flush();
                System.out.println("Sent message to " + username + ": " + 
                    (message instanceof Message ? ((Message)message).getType() : message.getClass().getSimpleName()));
            }
        } catch (IOException e) {
            System.err.println("Error sending message to client " + username + ": " + e.getMessage());
        }
    }
    
    public void notifyGameEnd(String winner) {
        if (isAuthenticated) {
            boolean isWin = username.equals(winner);
            userDAO.updateUserStats(username, isWin);
            
            Message gameEndMessage = new Message(Message.Type.GAME_END, winner, username, 
                isWin ? "You won!" : "You lost!");
            sendMessage(gameEndMessage);
        }
    }
    
    private void disconnect() {
        isConnected = false;
        if (username != null && isAuthenticated) {
            System.out.println("Player " + username + " disconnecting...");
            server.removeClient(username);
        }
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
    }
    
    public String getUsername() {
        return username;
    }
    
    public boolean isAuthenticated() {
        return isAuthenticated;
    }
}