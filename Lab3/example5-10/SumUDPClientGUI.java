import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.util.Random;

public class SumUDPClientGUI extends JFrame {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9876;
    
    private DatagramSocket socket;
    private InetAddress serverAddress;
    
    // GUI Components
    private JTextField inputField;
    private JTextArea resultArea;
    private JButton manualButton;
    private JButton randomButton;
    private JButton random3DigitButton;
    private JButton clearButton;
    
    public SumUDPClientGUI() {
        initializeNetwork();
        initializeGUI();
    }
    
    private void initializeNetwork() {
        try {
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName(SERVER_HOST);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Loi khoi tao ket noi: " + e.getMessage(), 
                "Loi", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    private void initializeGUI() {
        setTitle("UDP Sum Calculator Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Title
        JLabel titleLabel = new JLabel("Tinh tong S = 1 + 2 + ... + n", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Input field
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        inputPanel.add(new JLabel("Nhap n:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        inputField = new JTextField(15);
        inputPanel.add(inputField, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        
        manualButton = new JButton("1. Nhap thu cong");
        manualButton.addActionListener(new ManualInputListener());
        inputPanel.add(manualButton, gbc);
        
        gbc.gridy = 2;
        randomButton = new JButton("2. So ngau nhien (10 <= n <= 100)");
        randomButton.addActionListener(new RandomInputListener());
        inputPanel.add(randomButton, gbc);
        
        gbc.gridy = 3;
        random3DigitButton = new JButton("3. So 3 chu so ngau nhien (100 <= n < 1000)");
        random3DigitButton.addActionListener(new Random3DigitListener());
        inputPanel.add(random3DigitButton, gbc);
        
        gbc.gridy = 4;
        clearButton = new JButton("Xoa ket qua");
        clearButton.addActionListener(e -> resultArea.setText(""));
        inputPanel.add(clearButton, gbc);
        
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        
        // Result area
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Ket qua"));
        
        resultArea = new JTextArea(8, 30);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(resultPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Allow Enter key in input field
        inputField.addActionListener(new ManualInputListener());
    }
    
    private class ManualInputListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String input = inputField.getText().trim();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(SumUDPClientGUI.this, 
                    "Vui long nhap so n!", "Thong bao", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                int n = Integer.parseInt(input);
                if (n <= 0) {
                    JOptionPane.showMessageDialog(SumUDPClientGUI.this, 
                        "n phai lon hon 0!", "Loi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                calculateSum(n, "Nhap thu cong");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(SumUDPClientGUI.this, 
                    "Vui long nhap so hop le!", "Loi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private class RandomInputListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Random random = new Random();
            int n = random.nextInt(91) + 10; // 10 <= n <= 100
            inputField.setText(String.valueOf(n));
            calculateSum(n, "So ngau nhien (10-100)");
        }
    }
    
    private class Random3DigitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Random random = new Random();
            int n = random.nextInt(900) + 100; // 100 <= n <= 999
            inputField.setText(String.valueOf(n));
            calculateSum(n, "So 3 chu so ngau nhien (100-999)");
        }
    }
    
    private void calculateSum(int n, String method) {
        try {
            // Gửi yêu cầu đến server
            String request = String.valueOf(n);
            byte[] buffer = request.getBytes();
            DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length, serverAddress, SERVER_PORT);
            
            socket.send(packet);
            
            // Nhận kết quả từ server
            byte[] responseBuffer = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(
                responseBuffer, responseBuffer.length);
            
            socket.receive(responsePacket);
            
            String result = new String(responsePacket.getData(), 
                0, responsePacket.getLength());
            
            // Hiển thị kết quả
            String displayText = String.format(
                "=== %s ===\n" +
                "n = %d\n" +
                "S = 1 + 2 + ... + %d = %s\n" +
                "Thoi gian: %s\n\n",
                method, n, n, result, 
                java.time.LocalTime.now().toString()
            );
            
            resultArea.append(displayText);
            resultArea.setCaretPosition(resultArea.getDocument().getLength());
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, 
                "Loi ket noi server: " + ex.getMessage(), 
                "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    public void dispose() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        super.dispose();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Use default look and feel
            }
            
            new SumUDPClientGUI().setVisible(true);
        });
    }
}