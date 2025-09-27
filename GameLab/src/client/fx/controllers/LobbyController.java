package client.fx.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import client.network.ClientConnection;
import client.network.MessageListener;
import client.fx.utils.SceneManager;
import shared.network.Message;
import shared.models.RoomState;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;

public class LobbyController implements Initializable, MessageListener {
    
    @FXML private TextField roomIdField;
    @FXML private Button joinSpecificRoomButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private ListView<RoomListItem> roomListView;
    @FXML private Label statusLabel;
    
    private ClientConnection gameClient;
    private ObservableList<RoomListItem> roomList;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roomList = FXCollections.observableArrayList();
        roomListView.setItems(roomList);
        
        // Custom cell factory for room display
        roomListView.setCellFactory(listView -> new ListCell<RoomListItem>() {
            @Override
            protected void updateItem(RoomListItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    if (item.canJoin()) {
                        setStyle("-fx-text-fill: black; -fx-background-color: #e8f5e8;");
                    } else {
                        setStyle("-fx-text-fill: gray; -fx-background-color: #f0f0f0;");
                    }
                }
            }
        });
        
        // Double-click to join room
        roomListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                RoomListItem selected = roomListView.getSelectionModel().getSelectedItem();
                if (selected != null && selected.canJoin()) {
                    joinRoom(selected.getRoomId());
                }
            }
        });
        
        statusLabel.setText("Loading...");
        
        // Get game client from SceneManager
        gameClient = (ClientConnection) SceneManager.getInstance().getGameClient();
        if (gameClient != null) {
            gameClient.setMessageListener(this);
            requestRoomList();
        }
    }
    
    public void setGameClient(ClientConnection client) {
        this.gameClient = client;
        if (client != null) {
            client.setMessageListener(this);
            requestRoomList();
        }
    }
    
    @FXML
    private void handleJoinSpecificRoom() {
        String roomId = roomIdField.getText().trim();
        if (!roomId.isEmpty()) {
            joinRoom(roomId);
        } else {
            showAlert("Please enter a room ID", Alert.AlertType.WARNING);
        }
    }
    
    @FXML
    private void handleRefresh() {
        requestRoomList();
    }
    
    @FXML
    private void handleBack() {
        try {
            SceneManager.getInstance().showMainMenuScene();
        } catch (Exception e) {
            showAlert("Error returning to main menu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void joinRoom(String roomId) {
        if (gameClient != null && gameClient.isConnected()) {
            joinSpecificRoomButton.setDisable(true);
            joinSpecificRoomButton.setText("Joining...");
            statusLabel.setText("Joining room " + roomId + "...");
            gameClient.joinRoom(roomId);
        } else {
            showAlert("Not connected to server", Alert.AlertType.ERROR);
        }
    }
    
    private void requestRoomList() {
        if (gameClient != null && gameClient.isConnected()) {
            refreshButton.setDisable(true);
            refreshButton.setText("Refreshing...");
            statusLabel.setText("Refreshing room list...");
            gameClient.requestRooms();
        } else {
            statusLabel.setText("Not connected to server");
        }
    }
    
    @Override
    public void onMessageReceived(Message message) {
        Platform.runLater(() -> {
            switch (message.getType()) {
                case ROOMS_LIST:
                    @SuppressWarnings("unchecked")
                    List<RoomState> rooms = (List<RoomState>) message.getData();
                    updateRoomList(rooms);
                    refreshButton.setDisable(false);
                    refreshButton.setText("Refresh");
                    break;
                    
                case ROOM_JOINED:
                    // Scene change will be handled by ClientConnection
                    statusLabel.setText("Successfully joined room!");
                    break;
                    
                case ROOM_FULL:
                    joinSpecificRoomButton.setDisable(false);
                    joinSpecificRoomButton.setText("Join Room");
                    statusLabel.setText("Ready to join rooms");
                    showAlert("Failed to join room: " + message.getMessage(), Alert.AlertType.ERROR);
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
            statusLabel.setText("Connection lost");
            showAlert("Connection to server lost", Alert.AlertType.ERROR);
            try {
                SceneManager.getInstance().showLoginScene();
            } catch (Exception e) {
                System.exit(0);
            }
        });
    }
    
    public void updateRoomList(List<RoomState> rooms) {
        Platform.runLater(() -> {
            roomList.clear();
            for (RoomState room : rooms) {
                roomList.add(new RoomListItem(room));
            }
            statusLabel.setText("Found " + rooms.size() + " rooms");
        });
    }
    
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Lobby");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Inner class for room list items
    public static class RoomListItem {
        private final String roomId;
        private final String hostUsername;
        private final int currentPlayers;
        private final int maxPlayers;
        private final RoomState.Status status;
        
        public RoomListItem(RoomState room) {
            this.roomId = room.getRoomId();
            this.hostUsername = room.getHostUsername();
            this.currentPlayers = room.getCurrentPlayers();
            this.maxPlayers = room.getMaxPlayers();
            this.status = room.getStatus();
        }
        
        public String getRoomId() { return roomId; }
        public boolean canJoin() { return status == RoomState.Status.WAITING && currentPlayers < maxPlayers; }
        
        @Override
        public String toString() {
            String statusText = canJoin() ? "WAITING" : status.toString();
            String joinable = canJoin() ? " ✓" : " ✗";
            return String.format("Room %s | Host: %s | Players: %d/%d | Status: %s%s", 
                roomId, hostUsername, currentPlayers, maxPlayers, statusText, joinable);
        }
    }
}