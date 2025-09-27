package client.fx.controllers;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import client.network.ClientConnection;
import client.network.MessageListener;
import client.fx.utils.SceneManager;
import shared.network.Message;
import shared.models.RoomState;
import shared.models.PlayerState;
import shared.utils.Constants;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

public class GameController implements Initializable, MessageListener {
    
    @FXML private Canvas gameCanvas;
    @FXML private VBox playerListVBox;
    @FXML private Label timerLabel;
    @FXML private Label scoreLabel;
    @FXML private Button readyButton;
    @FXML private Button startGameButton;
    @FXML private Button leaveRoomButton;
    @FXML private Label statusLabel;
    @FXML private VBox gameUI;
    @FXML private VBox waitingUI;
    
    private ClientConnection gameClient;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;
    private Map<String, PlayerState> players;
    private PlayerState myPlayer;
    private Set<KeyCode> pressedKeys;
    private RoomState currentRoom;
    private boolean isGameRunning;
    private boolean isReady;
    private long lastMoveTime;
    
    // Game state
    private int myX, myY;
    private String myDirection = "right";
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = gameCanvas.getGraphicsContext2D();
        players = new HashMap<>();
        pressedKeys = new HashSet<>();
        isGameRunning = false;
        isReady = false;
        lastMoveTime = 0;
        
        // Initialize player position
        myX = 100;
        myY = Constants.WINDOW_HEIGHT - 150;
        
        // Setup canvas
        gameCanvas.setWidth(Constants.WINDOW_WIDTH - 250); // Leave space for UI
        gameCanvas.setHeight(Constants.WINDOW_HEIGHT - 100);
        
        // Setup key event handlers
        gameCanvas.setFocusTraversable(true);
        gameCanvas.setOnKeyPressed(this::handleKeyPressed);
        gameCanvas.setOnKeyReleased(this::handleKeyReleased);
        
        // Get game client from SceneManager
        gameClient = (ClientConnection) SceneManager.getInstance().getGameClient();
        if (gameClient != null) {
            gameClient.setMessageListener(this);
        }
        
        // Initial UI state
        showWaitingUI();
        
        // Start game loop
        startGameLoop();
        
        statusLabel.setText("Waiting for players...");
    }
    
    public void setGameClient(ClientConnection client) {
        this.gameClient = client;
        if (client != null) {
            client.setMessageListener(this);
        }
    }
    
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isGameRunning) {
                    handleInput();
                    updateGame();
                }
                renderGame();
            }
        };
        gameLoop.start();
    }
    
    private void handleInput() {
        long currentTime = System.currentTimeMillis();
        boolean moved = false;
        
        if (myPlayer != null && myPlayer.isAlive()) {
            int newX = myX;
            int newY = myY;
            
            if (pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.LEFT)) {
                newX -= Constants.MOVEMENT_SPEED;
                myDirection = "left";
                moved = true;
            }
            if (pressedKeys.contains(KeyCode.D) || pressedKeys.contains(KeyCode.RIGHT)) {
                newX += Constants.MOVEMENT_SPEED;
                myDirection = "right";
                moved = true;
            }
            if (pressedKeys.contains(KeyCode.W) || pressedKeys.contains(KeyCode.UP)) {
                newY -= Constants.MOVEMENT_SPEED;
                moved = true;
            }
            if (pressedKeys.contains(KeyCode.S) || pressedKeys.contains(KeyCode.DOWN)) {
                newY += Constants.MOVEMENT_SPEED;
                moved = true;
            }
            
            // Boundary checking
            newX = Math.max(0, Math.min(newX, (int)gameCanvas.getWidth() - 50));
            newY = Math.max(0, Math.min(newY, (int)gameCanvas.getHeight() - 50));
            
            // Send movement if position changed and enough time passed
            if (moved && (newX != myX || newY != myY) && currentTime - lastMoveTime > 50) {
                myX = newX;
                myY = newY;
                
                if (gameClient != null) {
                    gameClient.sendMove(myX, myY);
                }
                lastMoveTime = currentTime;
            }
        }
    }
    
    private void handleKeyPressed(KeyEvent event) {
        pressedKeys.add(event.getCode());
        
        // Handle attack
        if (event.getCode() == KeyCode.SPACE && isGameRunning && myPlayer != null && myPlayer.isAlive()) {
            handleAttack();
        }
        
        // Handle other actions
        if (event.getCode() == KeyCode.ESCAPE) {
            handleLeaveRoom();
        }
    }
    
    private void handleKeyReleased(KeyEvent event) {
        pressedKeys.remove(event.getCode());
    }
    
    private void handleAttack() {
        if (myPlayer == null || !myPlayer.isAlive()) return;
        
        // Find closest enemy player
        String target = findClosestEnemy();
        if (target != null) {
            gameClient.sendAttack(target);
            myPlayer.setAttacking(true);
        }
    }
    
    private String findClosestEnemy() {
        double minDistance = Double.MAX_VALUE;
        String closestEnemy = null;
        
        for (PlayerState player : players.values()) {
            if (!player.getUsername().equals(gameClient.getUsername()) && player.isAlive()) {
                double distance = Math.sqrt(Math.pow(player.getX() - myX, 2) + Math.pow(player.getY() - myY, 2));
                if (distance < 100 && distance < minDistance) { // Attack range
                    minDistance = distance;
                    closestEnemy = player.getUsername();
                }
            }
        }
        
        return closestEnemy;
    }
    
    private void updateGame() {
        // Update player states
        for (PlayerState player : players.values()) {
            if (player.getUsername().equals(gameClient.getUsername())) {
                player.setX(myX);
                player.setY(myY);
                player.setDirection(myDirection);
            }
            
            // Reset attack animation
            if (player.isAttacking()) {
                player.setAttacking(false);
            }
        }
    }
    
    private void renderGame() {
        // Clear canvas
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        
        // Draw ground
        gc.setFill(Color.GREEN);
        gc.fillRect(0, gameCanvas.getHeight() - 50, gameCanvas.getWidth(), 50);
        
        // Draw players
        for (PlayerState player : players.values()) {
            drawPlayer(player);
        }
        
        // Draw UI elements
        if (isGameRunning) {
            drawGameInfo();
        }
    }
    
    private void drawPlayer(PlayerState player) {
        double x = player.getX();
        double y = player.getY();
        
        // Player body
        if (player.isAlive()) {
            if (player.getUsername().equals(gameClient.getUsername())) {
                gc.setFill(Color.BLUE); // My player
            } else {
                gc.setFill(Color.RED); // Enemy players
            }
        } else {
            gc.setFill(Color.GRAY); // Dead player
        }
        
        gc.fillRect(x, y, 40, 60);
        
        // Health bar
        if (player.isAlive()) {
            double healthPercentage = (double) player.getHealth() / player.getMaxHealth();
            gc.setFill(Color.BLACK);
            gc.fillRect(x, y - 15, 40, 8);
            gc.setFill(healthPercentage > 0.3 ? Color.GREEN : Color.RED);
            gc.fillRect(x + 1, y - 14, (40 - 2) * healthPercentage, 6);
        }
        
        // Player name
        gc.setFill(Color.BLACK);
        gc.setFont(new Font(12));
        gc.fillText(player.getUsername(), x, y - 20);
        
        // Attack animation
        if (player.isAttacking()) {
            gc.setFill(Color.YELLOW);
            if (player.getDirection().equals("right")) {
                gc.fillOval(x + 40, y + 20, 30, 20);
            } else {
                gc.fillOval(x - 30, y + 20, 30, 20);
            }
        }
    }
    
    private void drawGameInfo() {
        gc.setFill(Color.BLACK);
        gc.setFont(new Font(14));
        gc.fillText("Use WASD/Arrow keys to move, SPACE to attack, ESC to leave", 10, 20);
        
        if (myPlayer != null) {
            gc.fillText("Health: " + myPlayer.getHealth() + "/" + myPlayer.getMaxHealth(), 10, 40);
            gc.fillText("Score: " + myPlayer.getScore(), 10, 60);
            gc.fillText("Rank: " + myPlayer.getRank(), 10, 80);
        }
    }
    
    @FXML
    private void handleReady() {
        isReady = !isReady;
        readyButton.setText(isReady ? "Not Ready" : "Ready");
        readyButton.setStyle(isReady ? "-fx-background-color: #f39c12;" : "-fx-background-color: #27ae60;");
        
        if (gameClient != null) {
            gameClient.setPlayerReady(isReady);
        }
    }
    
    @FXML
    private void handleStartGame() {
        if (gameClient != null) {
            gameClient.startGame();
            startGameButton.setDisable(true);
            startGameButton.setText("Starting...");
        }
    }
    
    @FXML
    private void handleLeaveRoom() {
        if (gameClient != null) {
            gameClient.leaveRoom();
        }
        
        try {
            SceneManager.getInstance().showMainMenuScene();
        } catch (Exception e) {
            System.err.println("Error returning to main menu: " + e.getMessage());
        }
    }
    
    @Override
    public void onMessageReceived(Message message) {
        Platform.runLater(() -> {
            switch (message.getType()) {
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
                    
                case GAME_END:
                    handleGameEnd(message);
                    break;
                    
                default:
                    // Ignore other message types
                    break;
            }
        });
    }
    
    private void handleRoomUpdate(Message message) {
        RoomState room = (RoomState) message.getData();
        this.currentRoom = room;
        updatePlayerList();
        
        if (room.getHostUsername().equals(gameClient.getUsername())) {
            startGameButton.setVisible(true);
            startGameButton.setDisable(!room.allPlayersReady() || room.getCurrentPlayers() < 2);
        } else {
            startGameButton.setVisible(false);
        }
        
        statusLabel.setText("Room: " + room.getRoomId() + " | Players: " + 
                          room.getCurrentPlayers() + "/" + room.getMaxPlayers());
    }
    
    private void handlePlayerJoined(Message message) {
        PlayerState player = (PlayerState) message.getData();
        players.put(player.getUsername(), player);
        updatePlayerList();
        statusLabel.setText(player.getUsername() + " joined the room");
    }
    
    private void handlePlayerLeft(Message message) {
        PlayerState player = (PlayerState) message.getData();
        players.remove(player.getUsername());
        updatePlayerList();
        statusLabel.setText(player.getUsername() + " left the room");
    }
    
    private void handleGameStart(Message message) {
        RoomState room = (RoomState) message.getData();
        this.currentRoom = room;
        this.players = new HashMap<>(room.getPlayers());
        this.myPlayer = players.get(gameClient.getUsername());
        
        isGameRunning = true;
        showGameUI();
        
        statusLabel.setText("Game started! Fight!");
        gameCanvas.requestFocus();
    }
    
    private void handleGameState(Message message) {
        RoomState room = (RoomState) message.getData();
        this.players = new HashMap<>(room.getPlayers());
        this.myPlayer = players.get(gameClient.getUsername());
    }
    
    private void handleGameTimer(Message message) {
        Long remainingTime = (Long) message.getData();
        int minutes = (int) (remainingTime / 60000);
        int seconds = (int) ((remainingTime % 60000) / 1000);
        timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
    }
    
    private void handlePlayerMove(Message message) {
        PlayerState player = (PlayerState) message.getData();
        if (players.containsKey(player.getUsername())) {
            players.put(player.getUsername(), player);
        }
    }
    
    private void handlePlayerAttack(Message message) {
        @SuppressWarnings("unchecked")
        Map<String, Object> attackData = (Map<String, Object>) message.getData();
        
        PlayerState attacker = (PlayerState) attackData.get("attacker");
        PlayerState target = (PlayerState) attackData.get("target");
        
        if (attacker != null) {
            players.put(attacker.getUsername(), attacker);
        }
        if (target != null) {
            players.put(target.getUsername(), target);
            if (target.getUsername().equals(gameClient.getUsername())) {
                myPlayer = target;
            }
        }
    }
    
    private void handlePlayerRespawn(Message message) {
        PlayerState player = (PlayerState) message.getData();
        players.put(player.getUsername(), player);
        if (player.getUsername().equals(gameClient.getUsername())) {
            myPlayer = player;
            myX = player.getX();
            myY = player.getY();
        }
        statusLabel.setText(player.getUsername() + " respawned!");
    }
    
    private void handleScoreUpdate(Message message) {
        @SuppressWarnings("unchecked")
        Map<String, PlayerState> updatedPlayers = (Map<String, PlayerState>) message.getData();
        this.players = new HashMap<>(updatedPlayers);
        this.myPlayer = players.get(gameClient.getUsername());
        updateScoreDisplay();
    }
    
    private void handleGameEnd(Message message) {
        @SuppressWarnings("unchecked")
        Map<String, Object> gameResult = (Map<String, Object>) message.getData();
        
        isGameRunning = false;
        showWaitingUI();
        
        String winner = (String) gameResult.get("winner");
        @SuppressWarnings("unchecked")
        List<PlayerState> finalScores = (List<PlayerState>) gameResult.get("scores");
        
        // Show game end dialog
        showGameEndDialog(winner, finalScores);
    }
    
    private void updatePlayerList() {
        playerListVBox.getChildren().clear();
        
        if (currentRoom != null) {
            for (PlayerState player : currentRoom.getPlayers().values()) {
                Label playerLabel = new Label(player.getUsername() + 
                    (player.isReady() ? " ✓" : "") +
                    (player.getUsername().equals(currentRoom.getHostUsername()) ? " (Host)" : ""));
                
                if (player.isReady()) {
                    playerLabel.setStyle("-fx-text-fill: green;");
                }
                
                playerListVBox.getChildren().add(playerLabel);
            }
        }
    }
    
    private void updateScoreDisplay() {
        if (myPlayer != null) {
            scoreLabel.setText("Score: " + myPlayer.getScore() + " | Kills: " + 
                             myPlayer.getKills() + " | Deaths: " + myPlayer.getDeaths());
        }
    }
    
    private void showWaitingUI() {
        waitingUI.setVisible(true);
        gameUI.setVisible(false);
    }
    
    private void showGameUI() {
        waitingUI.setVisible(false);
        gameUI.setVisible(true);
    }
    
    private void showGameEndDialog(String winner, List<PlayerState> finalScores) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Ended");
        alert.setHeaderText("Game Results");
        
        StringBuilder content = new StringBuilder();
        content.append("Winner: ").append(winner).append("\n\n");
        content.append("Final Scores:\n");
        
        for (int i = 0; i < finalScores.size(); i++) {
            PlayerState player = finalScores.get(i);
            content.append(String.format("%d. %s - Score: %d (K:%d D:%d)\n", 
                i + 1, player.getUsername(), player.getScore(), 
                player.getKills(), player.getDeaths()));
        }
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }
    
    @Override
    public void onConnectionLost() {
        Platform.runLater(() -> {
            if (gameLoop != null) {
                gameLoop.stop();
            }
            
            showAlert("Connection to server lost", Alert.AlertType.ERROR);
            try {
                SceneManager.getInstance().showLoginScene();
            } catch (Exception e) {
                System.exit(0);
            }
        });
    }
    
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Game");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void cleanup() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
}