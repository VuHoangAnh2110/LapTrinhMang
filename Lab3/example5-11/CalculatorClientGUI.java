import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.io.File;

public class CalculatorClientGUI extends JFrame {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER1_PORT = 9001;
    private static final int SERVER2_PORT = 9002;
    
    private DatagramSocket socket;
    private InetAddress serverAddress;
    
    // GUI Components
    private JTextField inputField;
    private JTextArea resultArea;
    private JButton singleButton, multipleButton, fileButton, clearButton;
    private volatile boolean isCalculating = false;
    
    public CalculatorClientGUI() {
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
        setTitle("UDP Calculator Client - S = 25*(a+b) - 6*(3c-2d)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Title
        JLabel titleLabel = new JLabel("Tinh S = 25*(a+b) - 6*(3c-2d)", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Input instruction
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel instructionLabel = new JLabel("Nhap gia tri (a,b,c,d) - Vi du: 1,2,3,4");
        instructionLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        inputPanel.add(instructionLabel, gbc);
        
        // Input field
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        inputField = new JTextField(20);
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(inputField, gbc);
        
        // Buttons
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        
        singleButton = new JButton("1. Tinh toan 1 lan");
        singleButton.addActionListener(new SingleCalculationListener());
        inputPanel.add(singleButton, gbc);
        
        gbc.gridx = 1;
        multipleButton = new JButton("2. Tinh toan nhieu lan");
        multipleButton.addActionListener(new MultipleCalculationListener());
        inputPanel.add(multipleButton, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 1;
        fileButton = new JButton("3. Doc tu file input.txt");
        fileButton.addActionListener(new FileInputListener());
        inputPanel.add(fileButton, gbc);
        
        gbc.gridx = 1;
        clearButton = new JButton("Xoa ket qua");
        clearButton.addActionListener(e -> resultArea.setText(""));
        inputPanel.add(clearButton, gbc);
        
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        
        // Result area
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Ket qua"));
        
        resultArea = new JTextArea(15, 50);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(resultPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Allow Enter key in input field for single calculation
        inputField.addActionListener(new SingleCalculationListener());
    }
    
    private class SingleCalculationListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String input = inputField.getText().trim();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(CalculatorClientGUI.this, 
                    "Vui long nhap gia tri!", "Thong bao", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            double[] values = parseInput(input);
            if (values != null) {
                calculateFormula(values[0], values[1], values[2], values[3], "Tinh toan 1 lan");
            }
        }
    }
    
    private class MultipleCalculationListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (isCalculating) {
                JOptionPane.showMessageDialog(CalculatorClientGUI.this, 
                    "Dang tinh toan! Vui long doi...", "Thong bao", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Tạo dialog để nhập liên tục
            JDialog dialog = new JDialog(CalculatorClientGUI.this, "Tinh toan nhieu lan", true);
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(CalculatorClientGUI.this);
            
            JPanel dialogPanel = new JPanel(new BorderLayout(10, 10));
            dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            // Instruction
            JLabel instruction = new JLabel("<html>Nhap gia tri (a,b,c,d) - Vi du: 1,2,3,4<br>Nhap 'stop' de dung</html>");
            instruction.setHorizontalAlignment(JLabel.CENTER);
            dialogPanel.add(instruction, BorderLayout.NORTH);
            
            // Input area
            JTextArea inputArea = new JTextArea(10, 30);
            inputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JScrollPane inputScroll = new JScrollPane(inputArea);
            inputScroll.setBorder(BorderFactory.createTitledBorder("Nhap du lieu (moi dong 1 bo gia tri)"));
            dialogPanel.add(inputScroll, BorderLayout.CENTER);
            
            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton startButton = new JButton("Bat dau tinh");
            JButton cancelButton = new JButton("Huy");
            
            startButton.addActionListener(evt -> {
                String text = inputArea.getText().trim();
                if (!text.isEmpty()) {
                    dialog.dispose();
                    processMultipleCalculations(text);
                }
            });
            
            cancelButton.addActionListener(evt -> dialog.dispose());
            
            buttonPanel.add(startButton);
            buttonPanel.add(cancelButton);
            dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            dialog.add(dialogPanel);
            dialog.setVisible(true);
        }
    }
    
    private void processMultipleCalculations(String input) {
        isCalculating = true;
        multipleButton.setText("Dang tinh toan...");
        multipleButton.setEnabled(false);
        
        // Chạy trong thread riêng để không block GUI
        new Thread(() -> {
            String[] lines = input.split("\n");
            int count = 1;
            
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                if (line.equalsIgnoreCase("stop")) {
                    SwingUtilities.invokeLater(() -> {
                        resultArea.append("=== Gap lenh 'stop' - Dung tinh toan ===\n\n");
                        resultArea.setCaretPosition(resultArea.getDocument().getLength());
                    });
                    break;
                }
                
                double[] values = parseInput(line);
                if (values != null) {
                    final int currentCount = count;
                    SwingUtilities.invokeLater(() -> {
                        calculateFormula(values[0], values[1], values[2], values[3], 
                                    "Tinh toan lan " + currentCount);
                    });
                    
                    try {
                        Thread.sleep(500); // Tạm dừng để user có thể theo dõi
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    final int currentCount = count;
                    final String currentLine = line; // Tạo biến final
                    SwingUtilities.invokeLater(() -> {
                        resultArea.append("Lan " + currentCount + ": Du lieu khong hop le - " + currentLine + "\n");
                        resultArea.setCaretPosition(resultArea.getDocument().getLength());
                    });
                }
                count++;
            }
            
            SwingUtilities.invokeLater(() -> {
                isCalculating = false;
                multipleButton.setText("2. Tinh toan nhieu lan");
                multipleButton.setEnabled(true);
            });
        }).start();
    }
    
    private class FileInputListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                File file = new File("input.txt");
                if (!file.exists()) {
                    JOptionPane.showMessageDialog(CalculatorClientGUI.this, 
                        "File input.txt khong ton tai!", "Loi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                Scanner scanner = new Scanner(file);
                int lineNumber = 1;
                
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    
                    if (line.equalsIgnoreCase("stop")) {
                        resultArea.append("=== Gap lenh 'stop' trong file - Dung doc ===\n\n");
                        break;
                    }
                    
                    if (line.isEmpty()) {
                        lineNumber++;
                        continue;
                    }
                    
                    double[] values = parseInputFromFile(line);
                    if (values != null) {
                        calculateFormula(values[0], values[1], values[2], values[3], 
                                       "File dong " + lineNumber);
                        Thread.sleep(300); // Tạm dừng để user theo dõi
                    } else {
                        resultArea.append("Dong " + lineNumber + ": Du lieu khong hop le - " + line + "\n");
                    }
                    
                    lineNumber++;
                }
                
                scanner.close();
                
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(CalculatorClientGUI.this, 
                    "Loi doc file: " + ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private double[] parseInput(String input) {
        try {
            String[] parts = input.split(",");
            if (parts.length != 4) {
                JOptionPane.showMessageDialog(this, 
                    "Dinh dang khong hop le! Can nhap 4 so cach nhau boi dau phay.\nVi du: 1,2,3,4", 
                    "Loi", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            
            double[] values = new double[4];
            for (int i = 0; i < 4; i++) {
                values[i] = Double.parseDouble(parts[i].trim());
            }
            
            return values;
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Du lieu khong hop le! Vui long nhap so.\nVi du: 1,2,3,4", 
                "Loi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    private double[] parseInputFromFile(String line) {
        try {
            // File có thể dùng space hoặc comma
            String[] parts = line.split("[,\\s]+");
            if (parts.length != 4) {
                return null;
            }
            
            double[] values = new double[4];
            for (int i = 0; i < 4; i++) {
                values[i] = Double.parseDouble(parts[i].trim());
            }
            
            return values;
            
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private void calculateFormula(double a, double b, double c, double d, String method) {
        try {
            // Gửi yêu cầu đến Server 1 (a+b)
            String request1 = a + "," + b;
            double result1 = sendRequest(request1, SERVER1_PORT);
            
            // Gửi yêu cầu đến Server 2 (3c-2d)
            String request2 = c + "," + d;
            double result2 = sendRequest(request2, SERVER2_PORT);
            
            // Tính S = 25*(a+b) - 6*(3c-2d)
            double S = 25 * result1 - 6 * result2;
            
            // Hiển thị kết quả
            String displayText = String.format(
                "=== %s ===\n" +
                "Input: a=%.1f, b=%.1f, c=%.1f, d=%.1f\n" +
                "Server 1 (a+b): %.1f + %.1f = %.1f\n" +
                "Server 2 (3c-2d): 3*%.1f - 2*%.1f = %.1f\n" +
                "S = 25*(%.1f) - 6*(%.1f) = %.1f\n" +
                "Thoi gian: %s\n" +
                "----------------------------------------\n\n",
                method, a, b, c, d,
                a, b, result1,
                c, d, result2,
                result1, result2, S,
                java.time.LocalTime.now().toString()
            );
            
            resultArea.append(displayText);
            resultArea.setCaretPosition(resultArea.getDocument().getLength());
            
        } catch (IOException ex) {
            String errorText = String.format("=== %s - LOI ===\n" +
                "Loi ket noi server: %s\n\n", method, ex.getMessage());
            resultArea.append(errorText);
            resultArea.setCaretPosition(resultArea.getDocument().getLength());
        }
    }
    
    private double sendRequest(String request, int port) throws IOException {
        // Gửi yêu cầu
        byte[] buffer = request.getBytes();
        DatagramPacket packet = new DatagramPacket(
            buffer, buffer.length, serverAddress, port);
        socket.send(packet);
        
        // Nhận kết quả
        byte[] responseBuffer = new byte[1024];
        DatagramPacket responsePacket = new DatagramPacket(
            responseBuffer, responseBuffer.length);
        socket.receive(responsePacket);
        
        String response = new String(responsePacket.getData(), 
            0, responsePacket.getLength());
        
        if (response.startsWith("ERROR")) {
            throw new IOException("Server error: " + response);
        }
        
        return Double.parseDouble(response);
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
            
            new CalculatorClientGUI().setVisible(true);
        });
    }
}