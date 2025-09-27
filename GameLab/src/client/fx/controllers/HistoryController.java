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
import shared.models.GameHistory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HistoryController implements Initializable, MessageListener {
    
    @FXML private TableView<GameHistoryItem> historyTable;
    @FXML private TableColumn<GameHistoryItem, Integer> gameIdColumn;
    @FXML private TableColumn<GameHistoryItem, String> roomIdColumn;
    @FXML private TableColumn<GameHistoryItem, String> playersColumn;
    @FXML private TableColumn<GameHistoryItem, String> winnerColumn;
    @FXML private TableColumn<GameHistoryItem, String> durationColumn;
    @FXML private TableColumn<GameHistoryItem, String> dateColumn;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Label statusLabel;
    
    private ClientConnection gameClient;
    private ObservableList<GameHistoryItem> historyList;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        historyList = FXCollections.observableArrayList();
        historyTable.setItems(historyList);
        
        // Setup table columns
        gameIdColumn.setCellValueFactory(new PropertyValueFactory<>("gameId"));
        roomIdColumn.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        playersColumn.setCellValueFactory(new PropertyValueFactory<>("players"));
        winnerColumn.setCellValueFactory(new PropertyValueFactory<>("winner"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        // Style winner column
        winnerColumn.setCellFactory(column -> new TableCell<GameHistoryItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Highlight if current user is winner
                    if (gameClient != null && item.equals(gameClient.getUsername())) {
                        setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        statusLabel.setText("Loading...");
        
        // Get game client from SceneManager
        gameClient = (ClientConnection) SceneManager.getInstance().getGameClient();
        if (gameClient != null) {
            gameClient.setMessageListener(this);
            requestHistory();
        }
    }
    
    public void setGameClient(ClientConnection client) {
        this.gameClient = client;
        if (client != null) {
            client.setMessageListener(this);
            requestHistory();
        }
    }
    
    @FXML
    private void handleRefresh() {
        requestHistory();
    }
    
    @FXML
    private void handleBack() {
        try {
            SceneManager.getInstance().showMainMenuScene();
        } catch (Exception e) {
            showAlert("Error returning to main menu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void requestHistory() {
        if (gameClient != null && gameClient.isConnected()) {
            refreshButton.setDisable(true);
            refreshButton.setText("Refreshing...");
            statusLabel.setText("Loading game history...");
            gameClient.requestHistory();
        } else {
            statusLabel.setText("Not connected to server");
        }
    }
    
    @Override
    public void onMessageReceived(Message message) {
        Platform.runLater(() -> {
            switch (message.getType()) {
                case HISTORY_DATA:
                    @SuppressWarnings("unchecked")
                    List<GameHistory> historyData = (List<GameHistory>) message.getData();
                    updateHistory(historyData);
                    refreshButton.setDisable(false);
                    refreshButton.setText("Refresh");
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
    
    public void updateHistory(List<GameHistory> historyData) {
        Platform.runLater(() -> {
            historyList.clear();
            for (GameHistory history : historyData) {
                historyList.add(new GameHistoryItem(history));
            }
            statusLabel.setText("Loaded " + historyData.size() + " game records");
            
            if (historyData.isEmpty()) {
                statusLabel.setText("No game history found");
            }
        });
    }
    
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Game History");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Inner class for table items
    public static class GameHistoryItem {
        private final Integer gameId;
        private final String roomId;
        private final String players;
        private final String winner;
        private final String duration;
        private final String date;
        
        public GameHistoryItem(GameHistory history) {
            this.gameId = history.getGameId();
            this.roomId = history.getRoomId();
            this.players = String.join(", ", history.getPlayers());
            this.winner = history.getWinner();
            this.duration = formatDuration(history.getDuration());
            
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm");
            this.date = sdf.format(new Date(history.getTimestamp()));
        }
        
        private String formatDuration(int seconds) {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return String.format("%d:%02d", minutes, remainingSeconds);
        }
        
        // Getters for table columns
        public Integer getGameId() { return gameId; }
        public String getRoomId() { return roomId; }
        public String getPlayers() { return players; }
        public String getWinner() { return winner; }
        public String getDuration() { return duration; }
        public String getDate() { return date; }
    }
}