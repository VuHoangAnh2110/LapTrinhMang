package client.fx.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import client.network.ClientConnection;
import client.network.MessageListener;
import client.fx.utils.SceneManager;
import shared.network.Message;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Map;

public class MainMenuController implements Initializable, MessageListener {
    
    @FXML private Label welcomeLabel;
    @FXML private Button profileButton;
    @FXML private Button historyButton;
    @FXML private Button joinRoomButton;
    @FXML private Button createRoomButton;
    @FXML private Button logoutButton;
    @FXML private Label statsLabel;
    
    private ClientConnection client;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        client = (ClientConnection) SceneManager.getInstance().getGameClient();
        if (client != null) {
            client.setMessageListener(this);
            if (client.getUsername() != null) {
                welcomeLabel.setText("Welcome, " + client.getUsername() + "!");
                requestUserStats();
            }
        }
    }
    
    @FXML
    private void handleProfile() {
        // Show profile dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Player Profile");
        alert.setHeaderText("Player Information");
        
        String profileInfo = "Username: " + (client != null ? client.getUsername() : "Unknown") + "\n" + 
                           statsLabel.getText() + "\n" +
                           "Status: " + (client != null && client.isConnected() ? "Online" : "Offline");
        
        alert.setContentText(profileInfo);
        alert.showAndWait();
    }
    
    @FXML
    private void handleHistory() {
        try {
            SceneManager.getInstance().showHistoryScene();
        } catch (Exception e) {
            showError("Error opening history: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleJoinRoom() {
        try {
            SceneManager.getInstance().showLobbyScene();
        } catch (Exception e) {
            showError("Error opening lobby: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCreateRoom() {
        if (client != null && client.isConnected()) {
            createRoomButton.setDisable(true);
            createRoomButton.setText("Creating...");
            client.createRoom();
        } else {
            showError("Not connected to server");
        }
    }
    
    @FXML
    private void handleLogout() {
        if (client != null) {
            client.disconnect();
        }
        
        try {
            SceneManager.getInstance().showLoginScene();
        } catch (Exception e) {
            System.err.println("Error returning to login: " + e.getMessage());
            System.exit(0);
        }
    }
    
    private void requestUserStats() {
        if (client != null && client.isConnected()) {
            client.requestUserStats();
        }
    }
    
    @Override
    public void onMessageReceived(Message message) {
        Platform.runLater(() -> {
            switch (message.getType()) {
                case USER_STATS:
                    @SuppressWarnings("unchecked")
                    Map<String, Integer> stats = (Map<String, Integer>) message.getData();
                    updateStats(stats.getOrDefault("wins", 0), stats.getOrDefault("losses", 0));
                    break;
                    
                case ROOM_CREATED:
                    // Room created successfully, GameController will handle the scene change
                    createRoomButton.setDisable(false);
                    createRoomButton.setText("Create Room");
                    break;
                    
                case ROOM_FULL:
                    createRoomButton.setDisable(false);
                    createRoomButton.setText("Create Room");
                    showError("Failed to create room: " + message.getMessage());
                    break;
                    
                default:
                    // Ignore other message types
                    break;
            }
        });
    }
    
    @Override
    public void onConnectionLost() {
        Platform.runLater(() -> {
            showError("Connection to server lost");
            try {
                SceneManager.getInstance().showLoginScene();
            } catch (Exception e) {
                System.exit(0);
            }
        });
    }
    
    public void updateStats(int wins, int losses) {
        Platform.runLater(() -> {
            statsLabel.setText(String.format("Wins: %d | Losses: %d | Total: %d", 
                wins, losses, wins + losses));
        });
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }
}