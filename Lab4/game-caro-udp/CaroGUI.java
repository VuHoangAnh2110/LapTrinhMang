import javax.swing.*; 
import java.awt.*; 
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.DatagramPacket; 
import java.net.DatagramSocket; 
import java.net.InetAddress; 
 
public class CaroGUI extends JFrame { 
    private JButton[][] boardButtons; 
    private JLabel statusLabel; 
    private JLabel playerInfoLabel;
    private final int boardSize = 8; 
    private final String serverIP; 
    private final int serverPort; 
    private DatagramSocket clientSocket; 
    private InetAddress serverAddress; 
    private boolean isMyTurn = false; 
    private final String mySymbol; 
    private final String opponentSymbol; 
    private final String playerName; 
 
    public CaroGUI(String serverIP, int serverPort, String playerName, String mySymbol, String opponentSymbol, DatagramSocket clientSocket) { 
        this.serverIP = serverIP; 
        this.serverPort = serverPort; 
        this.playerName = playerName; 
        this.mySymbol = mySymbol; 
        this.opponentSymbol = opponentSymbol; 
        this.clientSocket = clientSocket;
         
        try { 
            serverAddress = InetAddress.getByName(this.serverIP); 
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Không thể kết nối đến server: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); 
            System.exit(1); 
        } 
 
        initializeGUI();
    } 
    
    private void initializeGUI() {
        // Thiết lập cửa sổ chính
        setTitle("🎮 Game Cờ Caro - " + playerName + " (" + mySymbol + ")"); 
        setSize(600, 700); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        setLayout(new BorderLayout(10, 10));
        setResizable(false);
        
        // Đặt màu nền
        getContentPane().setBackground(new Color(240, 248, 255)); // Alice Blue
        
        // Panel thông tin người chơi
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(240, 248, 255));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        playerInfoLabel = new JLabel("👤 " + playerName + " - Ký hiệu: " + mySymbol, SwingConstants.LEFT);
        playerInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        playerInfoLabel.setForeground(mySymbol.equals("X") ? new Color(220, 20, 60) : new Color(30, 144, 255)); // Deep Pink hoặc Dodger Blue
        
        // Bảng trạng thái với viền đẹp
        statusLabel = new JLabel("⏳ Đang chờ người chơi khác...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(new Color(75, 0, 130)); // Indigo
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        statusLabel.setBackground(new Color(248, 248, 255)); // Ghost White
        statusLabel.setOpaque(true);
        
        topPanel.add(playerInfoLabel, BorderLayout.WEST);
        topPanel.add(statusLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);
        
        // Bàn cờ với viền đẹp
        JPanel boardContainer = new JPanel(new BorderLayout());
        boardContainer.setBackground(new Color(240, 248, 255));
        boardContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 20, 20, 20),
            BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            )
        ));
        
        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(boardSize, boardSize, 2, 2));
        boardPanel.setBackground(new Color(105, 105, 105)); // Dim Gray cho đường kẻ
        boardButtons = new JButton[boardSize][boardSize];
        
        for (int i = 0; i < boardSize; i++) { 
            for (int j = 0; j < boardSize; j++) { 
                JButton button = createStyledButton();
                boardButtons[i][j] = button; 
                 
                final int row = i; 
                final int col = j; 
                
                button.addActionListener(e -> handleButtonClick(row, col, button));
                boardPanel.add(button); 
            } 
        }
        
        boardContainer.add(boardPanel, BorderLayout.CENTER);
        add(boardContainer, BorderLayout.CENTER);
        
        // Panel thông tin bổ sung
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(240, 248, 255));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 10, 15));
        
        JLabel legendLabel = new JLabel("🔴 X (Người chơi 1)    🔵 O (Người chơi 2)    🎯 Cần 5 quân liên tiếp để thắng");
        legendLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        legendLabel.setForeground(new Color(105, 105, 105));
        legendLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        bottomPanel.add(legendLabel);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Căn giữa cửa sổ
        setLocationRelativeTo(null);
    }
    
    private JButton createStyledButton() {
        JButton button = new JButton();
        button.setFont(new Font("Segoe UI", Font.BOLD, 32));
        button.setPreferredSize(new Dimension(60, 60));
        
        // Màu nền mặc định
        button.setBackground(new Color(255, 255, 255)); // White
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hiệu ứng hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled() && button.getText().isEmpty()) {
                    button.setBackground(new Color(230, 230, 250)); // Lavender
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled() && button.getText().isEmpty()) {
                    button.setBackground(Color.WHITE);
                }
            }
        });
        
        return button;
    }
    
    private void handleButtonClick(int row, int col, JButton button) {
        // Kiểm tra button có enabled không và có phải lượt của mình không
        if (!button.isEnabled()) {
            return;
        }
        
        if (!isMyTurn) { 
            showStyledMessage("⚠️ Không phải lượt của bạn!", "Chờ lượt", JOptionPane.WARNING_MESSAGE);
            return;
        } 
        
        if (!button.getText().isEmpty()) { 
            showStyledMessage("❌ Ô này đã được đánh rồi!", "Nước đi không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return;
        } 
        
        // Gửi nước đi
        String move = row + "," + col; 
        try { 
            byte[] sendData = move.getBytes(); 
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort); 
            clientSocket.send(sendPacket); 
            System.out.println("Sent move: " + move + " from port: " + clientSocket.getLocalPort());
            setStatus("📤 Đã gửi nước đi, chờ đối thủ..."); 
            isMyTurn = false; 
        } catch (Exception ex) { 
            ex.printStackTrace(); 
            showStyledMessage("❌ Lỗi khi gửi nước đi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showStyledMessage(String message, String title, int messageType) {
        JOptionPane optionPane = new JOptionPane(message, messageType);
        optionPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JDialog dialog = optionPane.createDialog(this, title);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
     
    public void setSymbol(int row, int col, String symbol) { 
        SwingUtilities.invokeLater(() -> {
            JButton button = boardButtons[row][col];
            button.setText(symbol); 
            button.setEnabled(false);
            
            // Thiết lập màu sắc và hiệu ứng đẹp
            if (symbol.equals("X")) {
                button.setForeground(new Color(220, 20, 60)); // Deep Pink
                button.setBackground(new Color(255, 240, 245)); // Lavender Blush
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 20, 60), 2),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
                ));
            } else {
                button.setForeground(new Color(30, 144, 255)); // Dodger Blue
                button.setBackground(new Color(240, 248, 255)); // Alice Blue
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(30, 144, 255), 2),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
                ));
            }
            
            // Hiệu ứng nhấp nháy khi đặt quân
            Timer timer = new Timer(100, null);
            timer.addActionListener(e -> {
                if (timer.getDelay() == 100) {
                    button.setFont(new Font("Segoe UI", Font.BOLD, 36));
                    timer.setDelay(100);
                } else {
                    button.setFont(new Font("Segoe UI", Font.BOLD, 32));
                    timer.stop();
                }
            });
            timer.start();
        });
    } 
     
    public void setMyTurn(boolean myTurn) { 
        SwingUtilities.invokeLater(() -> {
            isMyTurn = myTurn; 
            System.out.println("My turn set to: " + myTurn + " for player: " + mySymbol);
            
            // Thay đổi màu viền khi đến lượt
            if (myTurn) {
                playerInfoLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GREEN, 3),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)
                ));
                playerInfoLabel.setBackground(new Color(240, 255, 240)); // Honeydew
                playerInfoLabel.setOpaque(true);
            } else {
                playerInfoLabel.setBorder(null);
                playerInfoLabel.setOpaque(false);
            }
        });
    } 
     
    public void setStatus(String status) { 
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
            
            // Thay đổi màu status dựa trên nội dung
            if (status.contains("thắng")) {
                statusLabel.setBackground(new Color(144, 238, 144)); // Light Green
                statusLabel.setForeground(new Color(0, 100, 0)); // Dark Green
            } else if (status.contains("thua")) {
                statusLabel.setBackground(new Color(255, 160, 160)); // Light Red
                statusLabel.setForeground(new Color(139, 0, 0)); // Dark Red
            } else if (status.contains("hòa")) {
                statusLabel.setBackground(new Color(255, 255, 160)); // Light Yellow
                statusLabel.setForeground(new Color(184, 134, 11)); // Dark Goldenrod
            } else if (status.contains("lượt của bạn") || status.contains("Đến lượt")) {
                statusLabel.setBackground(new Color(173, 216, 230)); // Light Blue
                statusLabel.setForeground(new Color(25, 25, 112)); // Midnight Blue
            } else {
                statusLabel.setBackground(new Color(248, 248, 255)); // Ghost White
                statusLabel.setForeground(new Color(75, 0, 130)); // Indigo
            }
        });
    } 
     
    public void setEnabledButtons(boolean enabled) { 
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < boardSize; i++) { 
                for (int j = 0; j < boardSize; j++) { 
                    JButton button = boardButtons[i][j];
                    // Chỉ enable button nếu chưa được đánh (text rỗng)
                    if (enabled && button.getText().isEmpty()) {
                        button.setEnabled(true);
                        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    } else if (!enabled) {
                        button.setEnabled(false);
                        if (button.getText().isEmpty()) {
                            button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        }
                    }
                } 
            } 
        });
    }
    
    // Phương thức để hiển thị thông báo kết thúc game với style đẹp
    public void showGameOverDialog(String message, String title) {
        SwingUtilities.invokeLater(() -> {
            // Tạo dialog tùy chỉnh
            JDialog dialog = new JDialog(this, title, true);
            dialog.setLayout(new BorderLayout());
            dialog.setSize(350, 200);
            dialog.setLocationRelativeTo(this);
            
            // Panel nội dung
            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.setBackground(Color.WHITE);
            contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            // Icon và message
            JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
            messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            
            if (title.contains("thắng")) {
                messageLabel.setForeground(new Color(0, 128, 0));
                messageLabel.setIcon(new ImageIcon("🏆"));
            } else if (title.contains("thua")) {
                messageLabel.setForeground(new Color(128, 0, 0));
                messageLabel.setIcon(new ImageIcon("😢"));
            } else {
                messageLabel.setForeground(new Color(0, 0, 128));
                messageLabel.setIcon(new ImageIcon("🤝"));
            }
            
            // Button OK
            JButton okButton = new JButton("OK");
            okButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            okButton.setPreferredSize(new Dimension(100, 35));
            okButton.addActionListener(e -> dialog.dispose());
            
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.WHITE);
            buttonPanel.add(okButton);
            
            contentPanel.add(messageLabel, BorderLayout.CENTER);
            contentPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            dialog.add(contentPanel);
            dialog.setVisible(true);
        });
    }
     
    public String getMySymbol() { 
        return mySymbol; 
    } 
}