package client.fx;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import shared.models.GameState;
import shared.models.Player;
import shared.utils.Constants;
import client.GameClient;

import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public class GameController implements Initializable {
    
    @FXML private Canvas gameCanvas;
    @FXML private Label statusLabel;
    @FXML private Label playersLabel;
    @FXML private Label healthLabel;
    @FXML private Label controlsLabel;
    
    private GameState gameState;
    private Set<KeyCode> pressedKeys;
    private AnimationTimer gameLoop;
    private GraphicsContext gc;
    private GameClient client;
    private boolean canAttack = true;
    private long lastAttackTime = 0;
    private boolean gameStateReceived = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = gameCanvas.getGraphicsContext2D();
        pressedKeys = new HashSet<>();
        
        // Make canvas focusable for key events
        gameCanvas.setFocusTraversable(true);
        gameCanvas.setOnKeyPressed(this::handleKeyPressed);
        gameCanvas.setOnKeyReleased(this::handleKeyReleased);
        
        controlsLabel.setText("Controls: Arrow keys to move, Space to attack");
        
        // Request focus when clicked
        gameCanvas.setOnMouseClicked(e -> gameCanvas.requestFocus());
        
        startGameLoop();
        
        // Request focus initially
        Platform.runLater(() -> gameCanvas.requestFocus());
    }
    
    public void setGameClient(GameClient client) {
        this.client = client;
        if (client != null) {
            client.setGamePanel(this); // Set this controller as the game panel
            
            // Request current game state
            Platform.runLater(() -> {
                if (client.getUsername() != null) {
                    client.sendMessage(new shared.models.Message(
                        shared.models.Message.Type.JOIN_ROOM, null, client.getUsername()));
                }
            });
        }
    }
    
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                handleInput();
                render();
            }
        };
        gameLoop.start();
    }
    
    private void handleKeyPressed(KeyEvent event) {
        pressedKeys.add(event.getCode());
        event.consume();
    }
    
    private void handleKeyReleased(KeyEvent event) {
        pressedKeys.remove(event.getCode());
        event.consume();
    }
    
    private void handleInput() {
        if (client == null || !client.isConnected()) {
            return;
        }
        
        boolean moved = false;
        int newX = 50; // Default position
        int newY = Constants.WINDOW_HEIGHT - 100;
        int direction = 0;
        
        // Get current player position
        String username = client.getUsername();
        if (gameState != null && gameState.getPlayers() != null && username != null) {
            Player currentPlayer = gameState.getPlayers().get(username);
            if (currentPlayer != null) {
                newX = currentPlayer.getX();
                newY = currentPlayer.getY();
            }
        }
        
        // Handle movement with reduced speed for smoother network play
        int moveSpeed = Constants.PLAYER_SPEED / 2; // Reduce speed for network play
        
        if (pressedKeys.contains(KeyCode.LEFT)) {
            newX -= moveSpeed;
            direction = -1;
            moved = true;
        }
        if (pressedKeys.contains(KeyCode.RIGHT)) {
            newX += moveSpeed;
            direction = 1;
            moved = true;
        }
        if (pressedKeys.contains(KeyCode.UP)) {
            newY -= moveSpeed;
            moved = true;
        }
        if (pressedKeys.contains(KeyCode.DOWN)) {
            newY += moveSpeed;
            moved = true;
        }
        
        // Boundary checking
        newX = Math.max(0, Math.min(Constants.WINDOW_WIDTH - Constants.PLAYER_WIDTH, newX));
        newY = Math.max(0, Math.min(Constants.WINDOW_HEIGHT - Constants.PLAYER_HEIGHT - 50, newY));
        
        if (moved) {
            client.sendMove(newX, newY, direction);
        }
        
        // Handle attack with cooldown
        if (pressedKeys.contains(KeyCode.SPACE) && canAttack) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAttackTime > Constants.ATTACK_COOLDOWN) {
                client.sendAttack();
                lastAttackTime = currentTime;
                canAttack = false;
                
                // Re-enable attack after cooldown
                Platform.runLater(() -> {
                    new Thread(() -> {
                        try {
                            Thread.sleep(Constants.ATTACK_COOLDOWN);
                            canAttack = true;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                });
            }
        }
    }
    
    private void render() {
        // Clear canvas
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        
        // Draw background
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        
        // Draw ground
        gc.setFill(Color.LIGHTGREEN);
        gc.fillRect(0, gameCanvas.getHeight() - 50, gameCanvas.getWidth(), 50);
        
        if (gameState != null && gameState.getPlayers() != null && gameStateReceived) {
            drawPlayers();
            updateUI();
        } else {
            drawWaitingMessage();
        }
        
        // Draw debug info
        drawDebugInfo();
    }
    
    private void drawPlayers() {
        Map<String, Player> players = gameState.getPlayers();
        
        if (players.isEmpty()) {
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            gc.fillText("No players in game", gameCanvas.getWidth()/2 - 70, gameCanvas.getHeight()/2);
            return;
        }
        
        for (Player player : players.values()) {
            if (player == null || player.getUsername() == null) {
                continue;
            }
            
            // Player body
            String clientUsername = (client != null) ? client.getUsername() : null;
            if (clientUsername != null && player.getUsername().equals(clientUsername)) {
                gc.setFill(Color.BLUE); // Own player
            } else {
                gc.setFill(Color.RED); // Other players
            }
            
            gc.fillRect(player.getX(), player.getY(), Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);
            
            // Player outline
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.strokeRect(player.getX(), player.getY(), Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);
            
            // Username
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            gc.fillText(player.getUsername(), player.getX(), player.getY() - 5);
            
            // Health bar
            drawHealthBar(player);
            
            // Attack indicator
            if (player.isAttacking()) {
                gc.setFill(Color.YELLOW);
                String direction = player.getDirection();
                if (direction == null) direction = "right";
                
                int attackX = direction.equals("right") ? 
                    player.getX() + Constants.PLAYER_WIDTH : 
                    player.getX() - Constants.ATTACK_RANGE;
                gc.fillRect(attackX, player.getY() + 10, Constants.ATTACK_RANGE, 10);
            }
        }
    }
    
    private void drawHealthBar(Player player) {
        int barWidth = 50;
        int barHeight = 5;
        int barX = player.getX() - 5;
        int barY = player.getY() - 15;
        
        // Background
        gc.setFill(Color.BLACK);
        gc.fillRect(barX, barY, barWidth, barHeight);
        
        // Health
        gc.setFill(Color.LIME);
        int healthWidth = (int) ((double) player.getHealth() / Constants.MAX_HEALTH * barWidth);
        gc.fillRect(barX, barY, healthWidth, barHeight);
    }
    
    private void updateUI() {
        Platform.runLater(() -> {
            if (gameState.isGameStarted()) {
                statusLabel.setText("Game Mode: " + gameState.getGameMode());
                playersLabel.setText("Players: " + gameState.getPlayers().size());
            } else {
                statusLabel.setText("Waiting for players...");
                playersLabel.setText("Need at least 2 players to start");
            }
            
            // Show own player health
            String clientUsername = (client != null) ? client.getUsername() : null;
            if (clientUsername != null && gameState.getPlayers() != null && 
                gameState.getPlayers().containsKey(clientUsername)) {
                
                Player ownPlayer = gameState.getPlayers().get(clientUsername);
                if (ownPlayer != null) {
                    healthLabel.setText("Health: " + ownPlayer.getHealth());
                }
            } else {
                healthLabel.setText("Health: --");
            }
        });
    }
    
    private void drawWaitingMessage() {
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        String message;
        if (client == null) {
            message = "Client not initialized";
        } else if (!client.isConnected()) {
            message = "Disconnected from server";
        } else if (!gameStateReceived) {
            message = "Waiting for game data...";
        } else {
            message = "Connecting to server...";
        }
        
        double textWidth = message.length() * 12; // Approximate
        gc.fillText(message, (gameCanvas.getWidth() - textWidth) / 2, gameCanvas.getHeight() / 2);
    }
    
    private void drawDebugInfo() {
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        
        String clientConnected = (client != null) ? String.valueOf(client.isConnected()) : "client is null";
        gc.fillText("Client connected: " + clientConnected, 10, gameCanvas.getHeight() - 100);
        
        gc.fillText("GameState received: " + gameStateReceived, 10, gameCanvas.getHeight() - 85);
        
        String username = (client != null && client.getUsername() != null) ? client.getUsername() : "null";
        gc.fillText("Username: " + username, 10, gameCanvas.getHeight() - 70);
        
        if (gameState != null && gameState.getPlayers() != null) {
            gc.fillText("Players in game: " + gameState.getPlayers().size(), 10, gameCanvas.getHeight() - 55);
            gc.fillText("Game started: " + gameState.isGameStarted(), 10, gameCanvas.getHeight() - 40);
        } else {
            gc.fillText("GameState: " + (gameState == null ? "null" : "players is null"), 10, gameCanvas.getHeight() - 55);
        }
        
        gc.fillText("Focus: " + gameCanvas.isFocused(), 10, gameCanvas.getHeight() - 25);
    }
    
    // This method will be called by GameClient (similar to GamePanel.updateGameState)
    public void updateGameState(GameState gameState) {
        Platform.runLater(() -> {
            System.out.println("GameController received GameState update");
            
            if (gameState == null) {
                System.err.println("Received null GameState!");
                return;
            }
            
            this.gameState = gameState;
            this.gameStateReceived = true;
            
            if (gameState.getPlayers() != null) {
                System.out.println("Players in gameState: " + gameState.getPlayers().size());
            } else {
                System.err.println("GameState players is null!");
            }
        });
    }
    
    public void onClose() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
}