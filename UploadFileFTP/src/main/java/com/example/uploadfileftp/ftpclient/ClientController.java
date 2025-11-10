package com.example.uploadfileftp.ftpclient;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

/**
 * Controller cho giao diện FTP Client
 * Quản lý logic kết nối, đăng nhập và upload file
 */
public class ClientController {
    
    @FXML private TextField txtHost;
    @FXML private TextField txtPort;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnConnect;
    @FXML private Button btnDisconnect;
    @FXML private Button btnChooseFile;
    @FXML private Button btnUpload;
    @FXML private Label lblStatus;
    @FXML private Label lblSelectedFile;
    @FXML private TextArea txtLog;
    @FXML private ProgressBar progressBar;
    
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean isConnected = false;
    private File selectedFile;

    /**
     * Khởi tạo controller, thiết lập trạng thái ban đầu
     */
    @FXML
    public void initialize() {
        // Ban đầu vô hiệu hóa nút chọn file và upload
        btnChooseFile.setDisable(true);
        btnUpload.setDisable(true);
        btnDisconnect.setDisable(true); 
        progressBar.setVisible(false);
        
        // Thiết lập giá trị mặc định
        txtHost.setText("localhost");
        txtPort.setText("2121");
        txtUsername.setText("");
        txtPassword.setText("");
        
        addLog("Sẵn sàng kết nối đến FTP Server...");
    }

    /**
     * Xử lý sự kiện nhấn nút "Kết nối"
     */
    @FXML
    private void handleConnect() {
        String host = txtHost.getText().trim();
        String portStr = txtPort.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        // Kiểm tra thông tin đầu vào
        if (host.isEmpty() || portStr.isEmpty() || username.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin kết nối!");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Port phải là số nguyên!");
            return;
        }

        // Thực hiện kết nối trong thread riêng để không block UI
        new Thread(() -> {
            try {
                Platform.runLater(() -> {
                    lblStatus.setText("Đang kết nối...");
                    addLog("Đang kết nối đến " + host + ":" + port);
                });

                // Bước 1: Kết nối đến server
                socket = new Socket(host, port);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);

                // Đọc thông báo chào mừng từ server (220)
                String welcomeMsg = reader.readLine();
                addLog("Server: " + welcomeMsg);

                // Bước 2: Gửi username
                sendCommand("USER " + username);
                String userResponse = reader.readLine();
                addLog("Server: " + userResponse);

                // Bước 3: Gửi password
                sendCommand("PASS " + password);
                String passResponse = reader.readLine();
                addLog("Server: " + passResponse);

                // Kiểm tra đăng nhập thành công (mã 230)
                if (passResponse.startsWith("230")) {
                    isConnected = true;
                    Platform.runLater(() -> {
                        lblStatus.setText("✓ Đã kết nối và đăng nhập thành công");
                        lblStatus.setStyle("-fx-text-fill: green;");
                        btnConnect.setDisable(true);
                        btnDisconnect.setDisable(false); // Enable nút ngắt kết nối
                        btnChooseFile.setDisable(false);
                        // Disable các field kết nối
                        txtHost.setDisable(true);
                        txtPort.setDisable(true);
                        txtUsername.setDisable(true);
                        txtPassword.setDisable(true);
                        addLog("========== Đăng nhập thành công ==========");
                    });
                } else {
                    // Đăng nhập thất bại
                    disconnect();
                    Platform.runLater(() -> {
                        lblStatus.setText("✗ Đăng nhập thất bại");
                        lblStatus.setStyle("-fx-text-fill: red;");
                        showAlert("Lỗi đăng nhập", passResponse);
                    });
                }

            } catch (IOException e) {
                Platform.runLater(() -> {
                    lblStatus.setText("✗ Không thể kết nối");
                    lblStatus.setStyle("-fx-text-fill: red;");
                    showAlert("Lỗi kết nối", "Không thể kết nối đến server.\n" + 
                             "Vui lòng kiểm tra server đã chạy chưa.\n\n" + e.getMessage());
                    addLog("Lỗi: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Xử lý sự kiện nhấn nút "Chọn file"
     */
    @FXML
    private void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file để upload");
        
        // Lấy stage hiện tại
        Stage stage = (Stage) btnChooseFile.getScene().getWindow();
        
        // Hiển thị dialog chọn file
        selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            lblSelectedFile.setText("File: " + selectedFile.getName() + 
                                   " (" + formatFileSize(selectedFile.length()) + ")");
            btnUpload.setDisable(false);
            addLog("Đã chọn file: " + selectedFile.getAbsolutePath());
        }
    }

    /**
     * Xử lý sự kiện nhấn nút "Upload"
     */
    @FXML
    private void handleUpload() {
        if (selectedFile == null || !selectedFile.exists()) {
            showAlert("Lỗi", "File không tồn tại!");
            return;
        }

        if (!isConnected) {
            showAlert("Lỗi", "Chưa kết nối đến server!");
            return;
        }

        // Thực hiện upload trong thread riêng
        new Thread(() -> {
            try {
                Platform.runLater(() -> {
                    progressBar.setVisible(true);
                    progressBar.setProgress(0);
                    btnUpload.setDisable(true);
                    lblStatus.setText("Đang upload...");
                });

                // Bước 1: Gửi lệnh STOR
                String filename = selectedFile.getName();
                sendCommand("STOR " + filename);
                String storResponse = reader.readLine();
                addLog("Server: " + storResponse);

                if (!storResponse.startsWith("150")) {
                    Platform.runLater(() -> {
                        showAlert("Lỗi", "Server từ chối nhận file: " + storResponse);
                        btnUpload.setDisable(false);
                        progressBar.setVisible(false);
                    });
                    return;
                }

                // Bước 2: Gửi kích thước file
                long fileSize = selectedFile.length();
                writer.println(fileSize);
                addLog("Gửi kích thước file: " + fileSize + " bytes");

                // Bước 3: Gửi dữ liệu file
                addLog("Bắt đầu upload file...");
                
                // KHÔNG đóng OutputStream của socket (sẽ đóng cả socket)
                OutputStream outputStream = socket.getOutputStream();
                
                try (FileInputStream fis = new FileInputStream(selectedFile)) {
                    
                    byte[] buffer = new byte[4096];
                    long totalBytesSent = 0;
                    int bytesRead;
                    
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        totalBytesSent += bytesRead;
                        
                        // Cập nhật progress bar
                        final double progress = (double) totalBytesSent / fileSize;
                        Platform.runLater(() -> progressBar.setProgress(progress));
                        
                        // Log tiến trình
                        int percent = (int) (progress * 100);
                        if (percent % 10 == 0) {
                            addLog("Đã gửi: " + percent + "%");
                        }
                    }
                    
                    outputStream.flush();
                }
                // FileInputStream được đóng tự động, OutputStream KHÔNG đóng → Socket vẫn mở!

                // Bước 4: Đọc phản hồi hoàn tất (226)
                String completeResponse = reader.readLine();
                
                // Kiểm tra nếu server đã đóng kết nối
                if (completeResponse == null) {
                    Platform.runLater(() -> {
                        lblStatus.setText("✗ Mất kết nối với server");
                        lblStatus.setStyle("-fx-text-fill: red;");
                        showAlert("Lỗi", "Mất kết nối với server sau khi upload.\n" +
                                "Tuy nhiên file có thể đã được lưu thành công.\n" +
                                "Vui lòng kiểm tra thư mục data/");
                        addLog("Cảnh báo: Mất kết nối, kiểm tra file trong data/");
                    });
                    return;
                }
                
                addLog("Server: " + completeResponse);

                if (completeResponse.startsWith("226")) {
                    Platform.runLater(() -> {
                        lblStatus.setText("✓ Upload thành công!");
                        lblStatus.setStyle("-fx-text-fill: green;");
                        showAlert("Thành công", "File đã được upload thành công!\n" +
                                 "File: " + filename + "\n" +
                                 "Kích thước: " + formatFileSize(fileSize));
                        addLog("========== Upload thành công ==========");
                        progressBar.setProgress(1.0);
                    });
                } else {
                    Platform.runLater(() -> {
                        lblStatus.setText("✗ Upload thất bại");
                        lblStatus.setStyle("-fx-text-fill: red;");
                        showAlert("Lỗi", "Upload thất bại: " + completeResponse);
                    });
                }

            } catch (IOException e) {
                // Kiểm tra nếu lỗi chỉ là socket closed sau khi upload xong
                if (e.getMessage() != null && e.getMessage().contains("Socket closed")) {
                    Platform.runLater(() -> {
                        lblStatus.setText("⚠ Upload có thể đã hoàn tất");
                        lblStatus.setStyle("-fx-text-fill: orange;");
                        addLog("Cảnh báo: Socket đã đóng. Kiểm tra file trong data/");
                        showAlert("Cảnh báo", "Kết nối bị đóng sau khi gửi file.\n" +
                                "File có thể đã được upload thành công.\n" +
                                "Vui lòng kiểm tra thư mục data/");
                    });
                } else {
                    Platform.runLater(() -> {
                        lblStatus.setText("✗ Lỗi khi upload");
                        lblStatus.setStyle("-fx-text-fill: red;");
                        showAlert("Lỗi", "Lỗi khi upload file:\n" + e.getMessage());
                        addLog("Lỗi upload: " + e.getMessage());
                    });
                }
            } finally {
                Platform.runLater(() -> {
                    btnUpload.setDisable(false);
                });
            }
        }).start();
    }

    /**
     * Gửi lệnh đến server
     * @param command Lệnh cần gửi
     */
    private void sendCommand(String command) {
        writer.println(command);
        addLog("Client: " + command);
    }

    /**
     * Thêm dòng log vào TextArea
     * @param message Nội dung log
     */
    private void addLog(String message) {
        Platform.runLater(() -> {
            txtLog.appendText(message + "\n");
        });
    }

    /**
     * Xử lý sự kiện nhấn nút "Ngắt kết nối"
     */
    @FXML
    private void handleDisconnect() {
        if (!isConnected) {
            showAlert("Thông báo", "Chưa có kết nối nào!");
            return;
        }

        // Thực hiện ngắt kết nối trong thread riêng
        new Thread(() -> {
            try {
                addLog("Đang ngắt kết nối...");
                
                // Gửi lệnh QUIT đến server
                if (writer != null && socket != null && !socket.isClosed()) {
                    sendCommand("QUIT");
                    
                    // Đọc phản hồi từ server
                    if (reader != null) {
                        String response = reader.readLine();
                        if (response != null) {
                            addLog("Server: " + response);
                        }
                    }
                }
                
                // Đóng kết nối
                disconnect();
                
                Platform.runLater(() -> {
                    lblStatus.setText("Đã ngắt kết nối");
                    lblStatus.setStyle("-fx-text-fill: gray;");
                    btnConnect.setDisable(false);
                    btnDisconnect.setDisable(true);
                    btnChooseFile.setDisable(true);
                    btnUpload.setDisable(true);
                    // Enable lại các field kết nối
                    txtHost.setDisable(false);
                    txtPort.setDisable(false);
                    txtUsername.setDisable(false);
                    txtPassword.setDisable(false);
                    // Reset file selection
                    selectedFile = null;
                    lblSelectedFile.setText("Chưa chọn file");
                    progressBar.setVisible(false);
                    progressBar.setProgress(0);
                    addLog("========== Đã ngắt kết nối ==========");
                });
                
            } catch (IOException e) {
                addLog("Lỗi khi ngắt kết nối: " + e.getMessage());
                Platform.runLater(() -> {
                    showAlert("Lỗi", "Lỗi khi ngắt kết nối:\n" + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Hiển thị dialog thông báo
     * @param title Tiêu đề
     * @param message Nội dung
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Format kích thước file sang dạng dễ đọc
     * @param size Kích thước byte
     * @return Chuỗi format (KB, MB, GB)
     */
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", size / Math.pow(1024, exp), pre);
    }

    /**
     * Ngắt kết nối với server (không gửi QUIT, chỉ đóng socket)
     */
    private void disconnect() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            isConnected = false;
        } catch (IOException e) {
            addLog("Lỗi khi đóng kết nối: " + e.getMessage());
        }
    }
}
