import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ClientGUI extends JFrame {
    private JTextField hostField;
    private JTextField portField;
    private JTextField num1Field;
    private JTextField num2Field;
    private JTextField resultField;
    private JComboBox<String> operationCombo;
    private JButton connectButton;
    private JButton disconnectButton;
    private JButton calculateButton;
    
    // Thêm các biến cho labels để dễ quản lý layout
    private JLabel titleLabel;
    private JLabel hostLabel;
    private JLabel portLabel;
    private JLabel num1Label;
    private JLabel num2Label;
    private JLabel resultLabel;
    private JLabel equalsLabel;
    
    private MayTinhClient client;
    
    public ClientGUI() {
        client = new MayTinhClient();
        
        setTitle("MÁY TÍNH TCP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null); 
        
        createComponents();
        setupLayout();
        setupEventHandlers();
        
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Set màu nền
        getContentPane().setBackground(new Color(240, 240, 240));
    }
    
    private void createComponents() {
        // Tiêu đề
        titleLabel = new JLabel("MÁY TÍNH TCP", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(255, 20, 147)); // Màu hồng
        
        // Các thành phần kết nối
        hostLabel = new JLabel("Máy chủ (IP)");
        hostField = new JTextField("localhost");
        portLabel = new JLabel("Cổng");
        portField = new JTextField("3210");
        
        connectButton = new JButton("Kết nối");
        connectButton.setBackground(new Color(144, 238, 144)); // Màu xanh lá nhạt
        
        disconnectButton = new JButton("Ngắt kết nối");
        disconnectButton.setBackground(new Color(255, 182, 193)); // Màu hồng nhạt
        disconnectButton.setEnabled(false);
        
        // Các thành phần tính toán
        num1Label = new JLabel("Số thứ nhất");
        num1Field = new JTextField();
        
        num2Label = new JLabel("Số thứ hai");
        num2Field = new JTextField();
        
        resultLabel = new JLabel("Kết quả");
        resultField = new JTextField();
        resultField.setEditable(false);
        resultField.setBackground(Color.WHITE);
        
        equalsLabel = new JLabel("=");
        
        // ComboBox cho phép toán
        String[] operations = {"-", "+", "*", "/"};
        operationCombo = new JComboBox<>(operations);
        operationCombo.setSelectedIndex(0); // Mặc định là trừ
        
        calculateButton = new JButton("Tính toán");
        calculateButton.setBackground(new Color(144, 238, 144)); // Màu xanh lá
        calculateButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Vô hiệu hóa các thành phần tính toán ban đầu
        setCalculationEnabled(false);
        
        // Thêm các component vào frame
        add(titleLabel);
        add(hostLabel);
        add(hostField);
        add(portLabel);
        add(portField);
        add(connectButton);
        add(disconnectButton);
        add(num1Label);
        add(num1Field);
        add(operationCombo);
        add(num2Label);
        add(num2Field);
        add(equalsLabel);
        add(resultLabel);
        add(resultField);
        add(calculateButton);
    }
    
    private void setupLayout() {
        // Tiêu đề
        titleLabel.setBounds(100, 10, 200, 30);
        
        // Hàng kết nối
        hostLabel.setBounds(20, 50, 80, 25); // "Máy chủ (IP)"
        hostField.setBounds(105, 50, 130, 25); // hostField
        portLabel.setBounds(250, 50, 40, 25); // "Cổng"
        portField.setBounds(295, 50, 60, 25); // portField
        
        // Nút kết nối và ngắt kết nối
        connectButton.setBounds(50, 85, 100, 30); // connectButton
        disconnectButton.setBounds(200, 85, 100, 30); // disconnectButton
        
        // Hàng nhập liệu
        num1Label.setBounds(20, 130, 80, 25); // "Số thứ nhất"
        num1Field.setBounds(20, 155, 80, 25); // num1Field
        
        operationCombo.setBounds(110, 155, 50, 25); // operationCombo
        
        num2Label.setBounds(170, 130, 80, 25); // "Số thứ hai"
        num2Field.setBounds(170, 155, 80, 25); // num2Field
        
        equalsLabel.setBounds(260, 155, 20, 25); // "="
        
        resultLabel.setBounds(290, 130, 80, 25); // "Kết quả"
        resultField.setBounds(290, 155, 80, 25); // resultField
        
        // Nút tính toán
        calculateButton.setBounds(150, 200, 100, 40); // calculateButton
    }
    
    private void setupEventHandlers() {
        connectButton.addActionListener(e -> connectToServer());
        disconnectButton.addActionListener(e -> disconnectFromServer());
        calculateButton.addActionListener(e -> performCalculation());
        
        // Xử lý phím Enter cho các text field
        ActionListener calculateAction = e -> {
            if (calculateButton.isEnabled()) {
                performCalculation();
            }
        };
        
        num1Field.addActionListener(calculateAction);
        num2Field.addActionListener(calculateAction);
        
        // Xử lý sự kiện đóng cửa sổ
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client.isConnected()) {
                    client.disconnect();
                }
                System.exit(0);
            }
        });
    }
    
    private void connectToServer() {
        try {
            String host = hostField.getText().trim();
            String portText = portField.getText().trim();
            
            if (host.isEmpty() || portText.isEmpty()) {
                showError("Vui lòng nhập đầy đủ thông tin kết nối!");
                return;
            }
            
            int port = Integer.parseInt(portText);
            
            if (client.connect(host, port)) {
                connectButton.setEnabled(false);
                disconnectButton.setEnabled(true);
                setCalculationEnabled(true);
                hostField.setEnabled(false);
                portField.setEnabled(false);
                
                showInfo("Kết nối thành công đến " + host + ":" + port);
            } else {
                showError("Không thể kết nối đến server!\nVui lòng kiểm tra server đã chạy chưa.");
            }
            
        } catch (NumberFormatException e) {
            showError("Cổng phải là một số nguyên hợp lệ!");
        }
    }
    
    private void disconnectFromServer() {
        client.disconnect();
        
        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        setCalculationEnabled(false);
        hostField.setEnabled(true);
        portField.setEnabled(true);
        
        resultField.setText("");
        
        showInfo("Đã ngắt kết nối khỏi server!");
    }
    
    private void setCalculationEnabled(boolean enabled) {
        num1Field.setEnabled(enabled);
        num2Field.setEnabled(enabled);
        operationCombo.setEnabled(enabled);
        calculateButton.setEnabled(enabled);
        
        if (!enabled) {
            num1Field.setText("");
            num2Field.setText("");
            resultField.setText("");
        }
    }
    
    private void performCalculation() {
        try {
            String num1Text = num1Field.getText().trim();
            String num2Text = num2Field.getText().trim();
            
            if (num1Text.isEmpty() || num2Text.isEmpty()) {
                showError("Vui lòng nhập đầy đủ hai số!");
                return;
            }
            
            double num1 = Double.parseDouble(num1Text);
            double num2 = Double.parseDouble(num2Text);
            String operation = (String) operationCombo.getSelectedItem();
            
            String result = client.calculate(num1, num2, operation);
            
            if (result.startsWith("Lỗi:")) {
                showError(result);
                resultField.setText("");
            } else {
                resultField.setText(result);
            }
            
        } catch (NumberFormatException e) {
            showError("Vui lòng nhập số hợp lệ!");
        } catch (Exception e) {
            showError("Lỗi khi tính toán: " + e.getMessage());
            
            // Nếu mất kết nối, reset giao diện
            if (!client.isConnected()) {
                disconnectFromServer();
            }
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new ClientGUI().setVisible(true);
        });
    }
}