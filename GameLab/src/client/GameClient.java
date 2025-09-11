package client;

import shared.models.Message;
import shared.models.GameState;
import shared.utils.Constants;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class GameClient {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private String username;
    private GamePanel gamePanel;
    private boolean isConnected;
    private BlockingQueue<Message> responseQueue;
    
    public GameClient() {
        this.isConnected = false;
        this.responseQueue = new LinkedBlockingQueue<>();
    }
    
    public boolean connect(String host, int port) {
        try {
            System.out.println("Attempting to connect to " + host + ":" + port);
            
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 5000);
            System.out.println("Socket connected successfully");
            
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            System.out.println("Output stream created");
            
            input = new ObjectInputStream(socket.getInputStream());
            System.out.println("Input stream created");
            
            isConnected = true;
            
            // Start listening for server messages
            new Thread(this::listenForMessages).start();
            System.out.println("Started listening thread");
            
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void listenForMessages() {
        try {
            System.out.println("Listening for messages from server...");
            while (isConnected) {
                Object received = input.readObject();
                System.out.println("Received object: " + received.getClass().getSimpleName());
                
                if (received instanceof GameState) {
                    GameState gs = (GameState) received;
                    System.out.println("Received GameState with " + 
                        (gs.getPlayers() != null ? gs.getPlayers().size() : "null") + " players");
                    
                    if (gamePanel != null) {
                        System.out.println("Updating GamePanel with new GameState");
                        SwingUtilities.invokeLater(() -> gamePanel.updateGameState(gs));
                    } else {
                        System.err.println("GamePanel is null, cannot update game state");
                    }
                } else if (received instanceof Message) {
                    Message message = (Message) received;
                    System.out.println("Received message type: " + message.getType());
                    handleMessage(message);
                } else {
                    System.err.println("Received unknown object type: " + received.getClass());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (isConnected) {
                System.err.println("Connection lost: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, 
                        "Connection to server lost!\n" + e.getMessage(), 
                        "Connection Error", 
                        JOptionPane.ERROR_MESSAGE);
                });
                disconnect();
            }
        }
    }
    
    private void handleMessage(Message message) {
        try {
            System.out.println("Handling message: " + message.getType());
            switch (message.getType()) {
                case LOGIN_SUCCESS:
                case LOGIN_FAILED:
                case REGISTER_SUCCESS:
                case REGISTER_FAILED:
                    System.out.println("Adding response to queue: " + message.getType());
                    responseQueue.offer(message);
                    break;
                    
                case PLAYER_JOINED:
                    System.out.println("Player " + message.getUsername() + " joined the game");
                    break;
                    
                case PLAYER_LEFT:
                    System.out.println("Player " + message.getUsername() + " left the game");
                    break;
                    
                case GAME_END:
                    SwingUtilities.invokeLater(() -> {
                        if (gamePanel != null) {
                            JOptionPane.showMessageDialog(gamePanel, 
                                message.getMessage(), 
                                "Game Over", 
                                JOptionPane.INFORMATION_MESSAGE);
                        }
                    });
                    break;
                    
                case USER_STATS:
                    if (message.getData() instanceof int[]) {
                        int[] stats = (int[]) message.getData();
                        System.out.println("Your stats - Wins: " + stats[0] + ", Losses: " + stats[1]);
                    }
                    break;
                    
                default:
                    System.out.println("Unknown message type: " + message.getType());
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error handling message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public Message waitForResponse(long timeoutMs) throws InterruptedException {
        System.out.println("Waiting for response, timeout: " + timeoutMs + "ms");
        Message response = responseQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
        if (response != null) {
            System.out.println("Got response: " + response.getType());
        } else {
            System.out.println("Timeout waiting for response");
        }
        return response;
    }
    
    public void sendMessage(Message message) {
        if (isConnected && output != null) {
            try {
                System.out.println("Sending message: " + message.getType());
                output.writeObject(message);
                output.flush();
                System.out.println("Message sent successfully");
            } catch (IOException e) {
                System.err.println("Failed to send message: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Cannot send message - not connected or output is null");
        }
    }
    
    public void sendMove(int x, int y, int direction) {
        if (username != null) {
            int[] positionData = {x, y, direction};
            sendMessage(new Message(Message.Type.MOVE, positionData, username));
        }
    }
    
    public void sendAttack() {
        if (username != null) {
            sendMessage(new Message(Message.Type.ATTACK, null, username));
        }
    }
    
    public void disconnect() {
        System.out.println("Disconnecting...");
        isConnected = false;
        if (socket != null && !socket.isClosed()) {
            try {
                if (username != null) {
                    sendMessage(new Message(Message.Type.LOGOUT, null, username));
                }
                socket.close();
                System.out.println("Socket closed");
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
    
    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        System.out.println("GamePanel set for client: " + (gamePanel != null ? "not null" : "null"));
    }
    
    public void setUsername(String username) {
        this.username = username;
        System.out.println("Username set to: " + username);
    }
    
    public String getUsername() {
        return username;
    }
    
    public boolean isConnected() {
        return isConnected && socket != null && !socket.isClosed();
    }
}