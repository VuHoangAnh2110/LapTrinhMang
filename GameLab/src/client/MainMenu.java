package client;

import shared.models.Message;
import shared.utils.Constants;
import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {
    private GameClient client;
    private LoginPanel loginPanel;
    private String currentUsername;
    
    public MainMenu() {
        initializeUI();
        client = new GameClient();
    }
    
    private void initializeUI() {
        setTitle("Fighting Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        loginPanel = new LoginPanel(this);
        add(loginPanel);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    public boolean attemptLogin(String username, String password) {
        try {
            System.out.println("Attempting login for: " + username);
            
            if (!client.connect(Constants.SERVER_HOST, Constants.SERVER_PORT)) {
                System.err.println("Failed to connect to server");
                return false;
            }
            
            // Set username BEFORE sending login message
            currentUsername = username;
            client.setUsername(username);
            
            System.out.println("Connected to server, sending login message");
            String[] credentials = {username, password};
            Message loginMessage = new Message(Message.Type.LOGIN, credentials, username);
            client.sendMessage(loginMessage);
            
            System.out.println("Waiting for login response...");
            // Wait for response
            Message response = client.waitForResponse(10000); // 10 second timeout
            
            if (response != null) {
                System.out.println("Login response: " + response.getType());
                if (response.getType() == Message.Type.LOGIN_SUCCESS) {
                    System.out.println("Login successful for: " + username);
                    return true;
                } else {
                    System.out.println("Login failed: " + response.getMessage());
                    client.disconnect();
                    return false;
                }
            } else {
                System.err.println("No response from server");
                client.disconnect();
                return false;
            }
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            client.disconnect();
            return false;
        }
    }
    
    public boolean attemptRegister(String username, String password) {
        try {
            System.out.println("Attempting registration for: " + username);
            
            if (!client.connect(Constants.SERVER_HOST, Constants.SERVER_PORT)) {
                System.err.println("Failed to connect to server for registration");
                return false;
            }
            
            String[] credentials = {username, password};
            Message registerMessage = new Message(Message.Type.REGISTER, credentials, username);
            client.sendMessage(registerMessage);
            
            // Wait for response
            Message response = client.waitForResponse(10000); // 10 second timeout
            client.disconnect(); // Disconnect after registration
            
            if (response != null) {
                System.out.println("Registration response: " + response.getType());
                return response.getType() == Message.Type.REGISTER_SUCCESS;
            } else {
                System.err.println("No response from server for registration");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            client.disconnect();
            return false;
        }
    }
    
    public void openGameWindow() {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Opening game window for: " + currentUsername);
            
            // Create game panel FIRST and set it to client
            GamePanel gamePanel = new GamePanel(client);
            client.setGamePanel(gamePanel);
            
            // Request current game state from server
            requestCurrentGameState();
            
            JFrame gameFrame = new JFrame("Fighting Game - " + currentUsername);
            gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gameFrame.setResizable(false);
            
            gameFrame.add(gamePanel);
            gameFrame.pack();
            gameFrame.setLocationRelativeTo(null);
            gameFrame.setVisible(true);
            
            // Add window closing handler
            gameFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    client.disconnect();
                    System.exit(0);
                }
            });
            
            // Hide login window
            this.setVisible(false);
        });
    }
    
    private void requestCurrentGameState() {
        // Request current game state
        Message requestStateMessage = new Message(Message.Type.JOIN_ROOM, null, currentUsername);
        client.sendMessage(requestStateMessage);
        System.out.println("Requested current game state");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new MainMenu().setVisible(true);
        });
    }
}