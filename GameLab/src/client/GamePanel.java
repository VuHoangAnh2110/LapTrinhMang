package client;

import shared.models.GameState;
import shared.models.Player;
import shared.utils.Constants;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class GamePanel extends JPanel implements KeyListener, ActionListener {
    private GameState gameState;
    private GameClient client;
    private Timer gameTimer;
    private Set<Integer> pressedKeys;
    private int playerX, playerY;
    private boolean canAttack;
    private long lastAttackTime;
    private boolean gameStateReceived = false;
    
    public GamePanel(GameClient client) {
        this.client = client;
        this.pressedKeys = new HashSet<>();
        this.canAttack = true;
        this.lastAttackTime = 0;
        
        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        setBackground(Color.LIGHT_GRAY);
        setFocusable(true);
        addKeyListener(this);
        
        // Initialize player position
        playerX = 50;
        playerY = Constants.WINDOW_HEIGHT - 100;
        
        // Game loop timer
        gameTimer = new Timer(16, this); // ~60 FPS
        gameTimer.start();
        
        String username = (client != null && client.getUsername() != null) ? client.getUsername() : "unknown";
        System.out.println("GamePanel created for user: " + username);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        try {
            // Draw background
            g.setColor(Color.CYAN);
            g.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw ground
            g.setColor(Color.GREEN);
            g.fillRect(0, getHeight() - 50, getWidth(), 50);
            
            if (gameState != null && gameStateReceived && gameState.getPlayers() != null) {
                drawPlayers(g);
                drawUI(g);
            } else {
                drawWaitingMessage(g);
            }
            
            // Debug info
            drawDebugInfo(g);
            
        } catch (Exception e) {
            System.err.println("Error in paintComponent: " + e.getMessage());
            e.printStackTrace();
            
            // Draw error message
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Rendering Error: " + e.getMessage(), 10, 100);
        }
    }
    
    private void drawDebugInfo(Graphics g) {
        try {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            g.drawString("Debug Info:", 10, getHeight() - 100);
            
            String clientConnected = (client != null) ? String.valueOf(client.isConnected()) : "client is null";
            g.drawString("Client connected: " + clientConnected, 10, getHeight() - 85);
            
            g.drawString("GameState received: " + gameStateReceived, 10, getHeight() - 70);
            
            String username = (client != null && client.getUsername() != null) ? client.getUsername() : "null";
            g.drawString("Username: " + username, 10, getHeight() - 55);
            
            if (gameState != null && gameState.getPlayers() != null) {
                g.drawString("Players in game: " + gameState.getPlayers().size(), 10, getHeight() - 40);
                g.drawString("Game started: " + gameState.isGameStarted(), 10, getHeight() - 25);
            } else {
                g.drawString("GameState: " + (gameState == null ? "null" : "players is null"), 10, getHeight() - 40);
            }
            
        } catch (Exception e) {
            System.err.println("Error in drawDebugInfo: " + e.getMessage());
        }
    }
    
    private void drawPlayers(Graphics g) {
        try {
            if (gameState == null || gameState.getPlayers() == null) {
                return;
            }
            
            Map<String, Player> players = gameState.getPlayers();
            
            if (players.isEmpty()) {
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 16));
                g.drawString("No players in game", getWidth()/2 - 70, getHeight()/2);
                return;
            }
            
            for (Player player : players.values()) {
                if (player == null || player.getUsername() == null) {
                    continue; // Skip null players
                }
                
                // Player body
                String clientUsername = (client != null) ? client.getUsername() : null;
                if (clientUsername != null && player.getUsername().equals(clientUsername)) {
                    g.setColor(Color.BLUE); // Own player
                } else {
                    g.setColor(Color.RED); // Other players
                }
                
                g.fillRect(player.getX(), player.getY(), Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);
                
                // Player outline
                g.setColor(Color.BLACK);
                g.drawRect(player.getX(), player.getY(), Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);
                
                // Username
                g.setColor(Color.BLACK);
                g.drawString(player.getUsername(), player.getX(), player.getY() - 5);
                
                // Health bar
                drawHealthBar(g, player);
                
                // Attack indicator
                if (player.isAttacking()) {
                    g.setColor(Color.YELLOW);
                    String direction = player.getDirection();
                    if (direction == null) direction = "right"; // Default direction
                    
                    int attackX = direction.equals("right") ? 
                        player.getX() + Constants.PLAYER_WIDTH : 
                        player.getX() - Constants.ATTACK_RANGE;
                    g.fillRect(attackX, player.getY() + 10, Constants.ATTACK_RANGE, 10);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in drawPlayers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void drawHealthBar(Graphics g, Player player) {
        try {
            if (player == null) return;
            
            int barWidth = 50;
            int barHeight = 5;
            int barX = player.getX() - 5;
            int barY = player.getY() - 15;
            
            // Background
            g.setColor(Color.BLACK);
            g.fillRect(barX, barY, barWidth, barHeight);
            
            // Health
            g.setColor(Color.GREEN);
            int healthWidth = (int) ((double) player.getHealth() / Constants.MAX_HEALTH * barWidth);
            g.fillRect(barX, barY, healthWidth, barHeight);
        } catch (Exception e) {
            System.err.println("Error in drawHealthBar: " + e.getMessage());
        }
    }
    
    private void drawUI(Graphics g) {
        try {
            if (gameState == null) return;
            
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            
            if (gameState.isGameStarted()) {
                String gameMode = gameState.getGameMode();
                if (gameMode == null) gameMode = "UNKNOWN";
                g.drawString("Game Mode: " + gameMode, 10, 25);
                
                int playerCount = (gameState.getPlayers() != null) ? gameState.getPlayers().size() : 0;
                g.drawString("Players: " + playerCount, 10, 45);
            } else {
                g.drawString("Waiting for players...", 10, 25);
                g.drawString("Need at least 2 players to start", 10, 45);
            }
            
            // Show own player info
            String clientUsername = (client != null) ? client.getUsername() : null;
            if (clientUsername != null && gameState.getPlayers() != null && 
                gameState.getPlayers().containsKey(clientUsername)) {
                
                Player ownPlayer = gameState.getPlayers().get(clientUsername);
                if (ownPlayer != null) {
                    g.drawString("Health: " + ownPlayer.getHealth(), 10, 65);
                    g.drawString("Position: (" + ownPlayer.getX() + ", " + ownPlayer.getY() + ")", 10, 85);
                }
            }
            
            // Controls help
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("Controls: Arrow keys to move, Space to attack", 10, getHeight() - 120);
        } catch (Exception e) {
            System.err.println("Error in drawUI: " + e.getMessage());
        }
    }
    
    private void drawWaitingMessage(Graphics g) {
        try {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 24));
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
            
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(message)) / 2;
            int y = getHeight() / 2;
            g.drawString(message, x, y);
        } catch (Exception e) {
            System.err.println("Error in drawWaitingMessage: " + e.getMessage());
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            handleInput();
            repaint();
        } catch (Exception ex) {
            System.err.println("Error in actionPerformed: " + ex.getMessage());
        }
    }
    
    private void handleInput() {
        try {
            if (client == null || !client.isConnected() || !gameStateReceived) return;
            
            boolean moved = false;
            int newX = playerX;
            int newY = playerY;
            int direction = 0;
            
            if (pressedKeys.contains(KeyEvent.VK_LEFT)) {
                newX -= Constants.PLAYER_SPEED;
                direction = -1;
                moved = true;
            }
            if (pressedKeys.contains(KeyEvent.VK_RIGHT)) {
                newX += Constants.PLAYER_SPEED;
                direction = 1;
                moved = true;
            }
            if (pressedKeys.contains(KeyEvent.VK_UP)) {
                newY -= Constants.PLAYER_SPEED;
                moved = true;
            }
            if (pressedKeys.contains(KeyEvent.VK_DOWN)) {
                newY += Constants.PLAYER_SPEED;
                moved = true;
            }
            
            // Boundary checking
            newX = Math.max(0, Math.min(Constants.WINDOW_WIDTH - Constants.PLAYER_WIDTH, newX));
            newY = Math.max(0, Math.min(Constants.WINDOW_HEIGHT - Constants.PLAYER_HEIGHT, newY));
            
            if (moved) {
                playerX = newX;
                playerY = newY;
                client.sendMove(playerX, playerY, direction);
            }
            
            // Attack
            if (pressedKeys.contains(KeyEvent.VK_SPACE) && canAttack) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastAttackTime > Constants.ATTACK_COOLDOWN) {
                    client.sendAttack();
                    lastAttackTime = currentTime;
                }
            }
        } catch (Exception e) {
            System.err.println("Error in handleInput: " + e.getMessage());
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (pressedKeys != null) {
            pressedKeys.add(e.getKeyCode());
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        if (pressedKeys != null) {
            pressedKeys.remove(e.getKeyCode());
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    public void updateGameState(GameState gameState) {
        try {
            System.out.println("GamePanel received GameState update");
            
            if (gameState == null) {
                System.err.println("Received null GameState!");
                return;
            }
            
            this.gameState = gameState;
            this.gameStateReceived = true;
            
            if (gameState.getPlayers() != null) {
                System.out.println("Players in gameState: " + gameState.getPlayers().size());
                
                // Update own player position from server
                String clientUsername = (client != null) ? client.getUsername() : null;
                if (clientUsername != null && gameState.getPlayers().containsKey(clientUsername)) {
                    Player ownPlayer = gameState.getPlayers().get(clientUsername);
                    if (ownPlayer != null) {
                        playerX = ownPlayer.getX();
                        playerY = ownPlayer.getY();
                        System.out.println("Updated own player position: (" + playerX + ", " + playerY + ")");
                    }
                }
            } else {
                System.err.println("GameState players is null!");
            }
            
            // Repaint on EDT
            SwingUtilities.invokeLater(this::repaint);
            
        } catch (Exception e) {
            System.err.println("Error in updateGameState: " + e.getMessage());
            e.printStackTrace();
        }
    }
}