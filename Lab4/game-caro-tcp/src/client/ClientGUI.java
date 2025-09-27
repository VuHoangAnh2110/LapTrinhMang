package client;

import shared.CaroBoard;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUI extends JFrame {
    private ClientGame game;
    private JButton[][] buttons;
    private JLabel statusLabel;
    private JLabel playerLabel;
    private String playerSymbol;
    private boolean canMove;
    private boolean gameStarted;
    
    // Colors for better UI
    private static final Color BOARD_COLOR = new Color(139, 69, 19); // Brown
    private static final Color EMPTY_CELL_COLOR = new Color(245, 222, 179); // Wheat
    private static final Color EMPTY_CELL_HOVER = new Color(255, 248, 220); // Cornsilk
    private static final Color DISABLED_CELL_COLOR = new Color(211, 211, 211); // Light Gray
    private static final Color PLAYER_X_COLOR = new Color(220, 20, 60); // Crimson Red
    private static final Color PLAYER_O_COLOR = new Color(30, 144, 255); // Dodger Blue
    private static final Color OCCUPIED_CELL_COLOR = new Color(255, 255, 255); // White
    private static final Color WIN_HIGHLIGHT_COLOR = new Color(255, 215, 0); // Gold
    
    public ClientGUI(ClientGame game) {
        this.game = game;
        this.canMove = false;
        this.gameStarted = false;
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("🎯 Caro Game - TCP Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Set main background color
        getContentPane().setBackground(new Color(250, 250, 250));
        
        // Create header panel with game info
        JPanel headerPanel = createHeaderPanel();
        
        // Create board panel with improved styling
        JPanel boardPanel = createBoardPanel();
        
        // Create control panel
        JPanel controlPanel = createControlPanel();
        
        // Create status panel
        JPanel statusPanel = createStatusPanel();
        
        add(headerPanel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        add(controlPanel, BorderLayout.EAST);
        
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
        
        // Set icon (optional)
        try {
            setIconImage(createGameIcon());
        } catch (Exception e) {
            // If icon creation fails, continue without icon
        }
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180)); // Steel Blue
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("🎯 CARO GAME", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        playerLabel = new JLabel("Connecting...", JLabel.CENTER);
        playerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        playerLabel.setForeground(Color.YELLOW);
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(playerLabel, BorderLayout.SOUTH);
        
        return headerPanel;
    }
    
    private JPanel createBoardPanel() {
        JPanel mainBoardPanel = new JPanel(new BorderLayout());
        mainBoardPanel.setBackground(BOARD_COLOR);
        mainBoardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JPanel boardPanel = new JPanel(new GridLayout(CaroBoard.BOARD_SIZE, CaroBoard.BOARD_SIZE, 2, 2));
        boardPanel.setBackground(BOARD_COLOR);
        buttons = new JButton[CaroBoard.BOARD_SIZE][CaroBoard.BOARD_SIZE];
        
        for (int i = 0; i < CaroBoard.BOARD_SIZE; i++) {
            for (int j = 0; j < CaroBoard.BOARD_SIZE; j++) {
                buttons[i][j] = createStyledButton();
                
                final int row = i;
                final int col = j;
                buttons[i][j].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("Button clicked: (" + row + "," + col + ") - canMove: " + canMove + ", gameStarted: " + gameStarted);
                        if (canMove && gameStarted && buttons[row][col].getText().isEmpty()) {
                            // Add visual feedback
                            buttons[row][col].setBackground(new Color(255, 255, 0, 100));
                            game.makeMove(row, col);
                        }
                    }
                });
                
                boardPanel.add(buttons[i][j]);
            }
        }
        
        mainBoardPanel.add(boardPanel, BorderLayout.CENTER);
        return mainBoardPanel;
    }
    
    private JButton createStyledButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(40, 40));
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setEnabled(false);
        button.setBackground(DISABLED_CELL_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled() && button.getText().isEmpty()) {
                    button.setBackground(EMPTY_CELL_HOVER);
                }
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.isEnabled() && button.getText().isEmpty()) {
                    button.setBackground(EMPTY_CELL_COLOR);
                }
            }
        });
        
        return button;
    }
    
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(new Color(250, 250, 250));
        controlPanel.setBorder(new EmptyBorder(20, 15, 20, 15));
        
        JButton reconnectButton = createStyledControlButton("🔄 Reconnect", new Color(34, 139, 34));
        JButton quitButton = createStyledControlButton("❌ Quit", new Color(220, 20, 60));
        
        reconnectButton.addActionListener(e -> {
            game.disconnect();
            game.connectToServer();
        });
        
        quitButton.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to quit?",
                "Confirm Quit",
                JOptionPane.YES_NO_OPTION
            );
            if (option == JOptionPane.YES_OPTION) {
                game.disconnect();
                System.exit(0);
            }
        });
        
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(reconnectButton);
        controlPanel.add(Box.createVerticalStrut(15));
        controlPanel.add(quitButton);
        controlPanel.add(Box.createVerticalGlue());
        
        return controlPanel;
    }
    
    private JButton createStyledControlButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(120, 40));
        button.setMaximumSize(new Dimension(120, 40));
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(47, 79, 79)); // Dark Slate Gray
        statusPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        statusLabel = new JLabel("Connecting to server...", JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(Color.WHITE);
        
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        
        return statusPanel;
    }
    
    private Image createGameIcon() {
        // Create a simple game icon
        int size = 32;
        java.awt.image.BufferedImage icon = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw background
        g2d.setColor(new Color(70, 130, 180));
        g2d.fillOval(2, 2, size-4, size-4);
        
        // Draw X and O with colors
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(PLAYER_X_COLOR);
        g2d.drawString("X", 6, 18);
        g2d.setColor(PLAYER_O_COLOR);
        g2d.drawString("O", 18, 26);
        
        g2d.dispose();
        return icon;
    }
    
    public void setStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("📊 " + status);
            System.out.println("Status updated: " + status);
        });
    }
    
    public void setPlayerSymbol(String symbol) {
        this.playerSymbol = symbol;
        SwingUtilities.invokeLater(() -> {
            setTitle("🎯 Caro Game - You are " + symbol);
            Color symbolColor = symbol.equals("X") ? PLAYER_X_COLOR : PLAYER_O_COLOR;
            playerLabel.setText("<html>👤 You are: <font color='" + 
                              (symbol.equals("X") ? "#DC143C" : "#1E90FF") + "'><b>" + symbol + "</b></font></html>");
            System.out.println("Player symbol set to: " + symbol);
        });
    }
    
    public void setGameStarted(boolean started) {
        this.gameStarted = started;
        System.out.println("Game started flag set to: " + started);
        
        if (started) {
            SwingUtilities.invokeLater(() -> {
                playerLabel.setText("<html>👤 You are: <font color='" + 
                                  (playerSymbol.equals("X") ? "#DC143C" : "#1E90FF") + 
                                  "'><b>" + playerSymbol + "</b></font> | 🎮 Game Active!</html>");
            });
        }
    }
    
    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
        System.out.println("Can move flag set to: " + canMove);
        
        SwingUtilities.invokeLater(() -> {
            updateButtonStates();
        });
    }
    
    private void updateButtonStates() {
        for (int i = 0; i < CaroBoard.BOARD_SIZE; i++) {
            for (int j = 0; j < CaroBoard.BOARD_SIZE; j++) {
                if (buttons[i][j].getText().isEmpty()) {
                    boolean shouldEnable = canMove && gameStarted;
                    buttons[i][j].setEnabled(shouldEnable);
                    buttons[i][j].setBackground(shouldEnable ? EMPTY_CELL_COLOR : DISABLED_CELL_COLOR);
                    buttons[i][j].setCursor(shouldEnable ? 
                        new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        }
    }
    
    public void updateBoard(String[] boardData) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Updating board with " + boardData.length + " cells");
            
            if (boardData.length != CaroBoard.BOARD_SIZE * CaroBoard.BOARD_SIZE) {
                System.err.println("Invalid board data length: " + boardData.length);
                return;
            }
            
            int index = 0;
            for (int i = 0; i < CaroBoard.BOARD_SIZE; i++) {
                for (int j = 0; j < CaroBoard.BOARD_SIZE; j++) {
                    try {
                        int cellValue = Integer.parseInt(boardData[index]);
                        String currentText = buttons[i][j].getText();
                        
                        if (cellValue == CaroBoard.PLAYER_X) {
                            if (!currentText.equals("X")) {
                                buttons[i][j].setText("X");
                                buttons[i][j].setFont(new Font("Arial", Font.BOLD, 28));
                                buttons[i][j].setForeground(PLAYER_X_COLOR);
                                buttons[i][j].setEnabled(false);
                                buttons[i][j].setBackground(OCCUPIED_CELL_COLOR);
                                buttons[i][j].setBorder(BorderFactory.createLoweredBevelBorder());
                                buttons[i][j].setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                System.out.println("Set X at (" + i + "," + j + ")");
                                
                                // Add animation effect
                                animateCell(buttons[i][j], PLAYER_X_COLOR);
                            }
                        } else if (cellValue == CaroBoard.PLAYER_O) {
                            if (!currentText.equals("O")) {
                                buttons[i][j].setText("O");
                                buttons[i][j].setFont(new Font("Arial", Font.BOLD, 28));
                                buttons[i][j].setForeground(PLAYER_O_COLOR);
                                buttons[i][j].setEnabled(false);
                                buttons[i][j].setBackground(OCCUPIED_CELL_COLOR);
                                buttons[i][j].setBorder(BorderFactory.createLoweredBevelBorder());
                                buttons[i][j].setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                System.out.println("Set O at (" + i + "," + j + ")");
                                
                                // Add animation effect
                                animateCell(buttons[i][j], PLAYER_O_COLOR);
                            }
                        } else {
                            // Empty cell
                            if (!currentText.isEmpty()) {
                                buttons[i][j].setText("");
                                buttons[i][j].setFont(new Font("Arial", Font.BOLD, 20));
                                buttons[i][j].setForeground(Color.BLACK);
                                buttons[i][j].setBorder(BorderFactory.createRaisedBevelBorder());
                            }
                            boolean shouldEnable = canMove && gameStarted;
                            buttons[i][j].setEnabled(shouldEnable);
                            buttons[i][j].setBackground(shouldEnable ? EMPTY_CELL_COLOR : DISABLED_CELL_COLOR);
                            buttons[i][j].setCursor(shouldEnable ? 
                                new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
                        }
                        
                        index++;
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid cell value at index " + index + ": " + boardData[index]);
                        index++;
                    }
                }
            }
            System.out.println("Board update completed");
        });
    }
    
    private void animateCell(JButton button, Color playerColor) {
        // Enhanced animation with pulse effect
        Timer timer = new Timer(150, null);
        timer.addActionListener(e -> {
            Color animColor = new Color(playerColor.getRed(), playerColor.getGreen(), playerColor.getBlue(), 100);
            button.setBackground(animColor);
            
            Timer resetTimer = new Timer(300, evt -> {
                button.setBackground(OCCUPIED_CELL_COLOR);
                ((Timer)evt.getSource()).stop();
            });
            resetTimer.setRepeats(false);
            resetTimer.start();
            timer.stop();
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    public void showGameResult(String result) {
        SwingUtilities.invokeLater(() -> {
            String message;
            String title;
            int messageType;
            
            switch (result) {
                case "WIN":
                    message = "🎉 Congratulations! You WIN! 🎉\n\nWould you like to play another game?";
                    title = "Victory!";
                    messageType = JOptionPane.INFORMATION_MESSAGE;
                    // Highlight winning animation
                    highlightBoard(WIN_HIGHLIGHT_COLOR);
                    break;
                case "LOSE":
                    message = "😞 Game Over! You LOSE! 😞\n\nBetter luck next time!\nWould you like to play again?";
                    title = "Defeat";
                    messageType = JOptionPane.INFORMATION_MESSAGE;
                    break;
                case "DRAW":
                    message = "🤝 It's a DRAW! 🤝\n\nGreat game! Would you like to play again?";
                    title = "Draw Game";
                    messageType = JOptionPane.INFORMATION_MESSAGE;
                    break;
                default:
                    return;
            }
            
            // Show result with sound effect (if available)
            UIManager.getLookAndFeel().provideErrorFeedback(this);
            
            int option = JOptionPane.showConfirmDialog(
                this,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                messageType
            );
            
            if (option == JOptionPane.YES_OPTION) {
                // Player wants to play again
                game.disconnect();
                resetBoard();
                game.connectToServer();
            } else {
                // Player doesn't want to play again
                game.disconnect();
                System.exit(0);
            }
        });
    }
    
    private void highlightBoard(Color highlightColor) {
        Timer highlightTimer = new Timer(200, null);
        highlightTimer.addActionListener(e -> {
            for (int i = 0; i < CaroBoard.BOARD_SIZE; i++) {
                for (int j = 0; j < CaroBoard.BOARD_SIZE; j++) {
                    if (!buttons[i][j].getText().isEmpty()) {
                        buttons[i][j].setBackground(highlightColor);
                    }
                }
            }
            
            Timer resetTimer = new Timer(500, evt -> {
                for (int i = 0; i < CaroBoard.BOARD_SIZE; i++) {
                    for (int j = 0; j < CaroBoard.BOARD_SIZE; j++) {
                        if (!buttons[i][j].getText().isEmpty()) {
                            buttons[i][j].setBackground(OCCUPIED_CELL_COLOR);
                        }
                    }
                }
                ((Timer)evt.getSource()).stop();
            });
            resetTimer.setRepeats(false);
            resetTimer.start();
            highlightTimer.stop();
        });
        highlightTimer.setRepeats(false);
        highlightTimer.start();
    }
    
    public void resetBoard() {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < CaroBoard.BOARD_SIZE; i++) {
                for (int j = 0; j < CaroBoard.BOARD_SIZE; j++) {
                    buttons[i][j].setText("");
                    buttons[i][j].setFont(new Font("Arial", Font.BOLD, 20));
                    buttons[i][j].setEnabled(false);
                    buttons[i][j].setBackground(DISABLED_CELL_COLOR);
                    buttons[i][j].setForeground(Color.BLACK);
                    buttons[i][j].setBorder(BorderFactory.createRaisedBevelBorder());
                    buttons[i][j].setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
            gameStarted = false;
            canMove = false;
            playerLabel.setText("Waiting for connection...");
            System.out.println("Board reset completed");
        });
    }
}