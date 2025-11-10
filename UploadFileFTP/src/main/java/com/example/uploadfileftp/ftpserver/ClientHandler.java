package com.example.uploadfileftp.ftpserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Xử lý kết nối và yêu cầu từ một FTP client
 * Mỗi client sẽ được xử lý bởi một instance riêng trong một thread riêng
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean isAuthenticated = false;
    
    // Thông tin đăng nhập cố định 
    private static final String VALID_USER = "admin";
    private static final String VALID_PASS = "123";
    
    // Thư mục lưu file
    private static final String DATA_DIR = "data";

    /**
     * Constructor nhận socket của client
     * @param socket Socket kết nối với client
     */
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        
        // Tạo thư mục data nếu chưa tồn tại
        try {
            Path dataPath = Paths.get(DATA_DIR);
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
                System.out.println("Đã tạo thư mục: " + DATA_DIR);
            }
        } catch (IOException e) {
            System.err.println("Không thể tạo thư mục data: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            // Khởi tạo input/output stream
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);
            
            // Gửi thông báo chào mừng (mã 220)
            sendResponse("220 FTP Server sẵn sàng");
            
            // Vòng lặp xử lý lệnh từ client
            String command;
            while ((command = reader.readLine()) != null) {
                System.out.println("Nhận lệnh: " + command);
                processCommand(command);
                
                // Nếu client gửi lệnh QUIT thì thoát
                if (command.toUpperCase().startsWith("QUIT")) {
                    break;
                }
            }
            
        } catch (IOException e) {
            System.err.println("Lỗi khi xử lý client: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    /**
     * Xử lý các lệnh FTP từ client
     * @param command Lệnh nhận được từ client
     */
    private void processCommand(String command) {
        String[] parts = command.split(" ", 2);
        String cmd = parts[0].toUpperCase();
        String argument = parts.length > 1 ? parts[1] : "";

        switch (cmd) {
            case "USER":
                handleUser(argument);
                break;
                
            case "PASS":
                handlePassword(argument);
                break;
                
            case "STOR":
                handleStore(argument);
                break;
                
            case "QUIT":
                handleQuit();
                break;
                
            default:
                sendResponse("502 Lệnh không được hỗ trợ");
        }
    }

    /**
     * Xử lý lệnh USER (tên đăng nhập)
     * @param username Tên người dùng
     */
    private void handleUser(String username) {
        if (username.equals(VALID_USER)) {
            sendResponse("331 Yêu cầu mật khẩu cho " + username);
        } else {
            sendResponse("530 Tên đăng nhập không đúng");
        }
    }

    /**
     * Xử lý lệnh PASS (mật khẩu)
     * @param password Mật khẩu
     */
    private void handlePassword(String password) {
        if (password.equals(VALID_PASS)) {
            isAuthenticated = true;
            sendResponse("230 Đăng nhập thành công");
        } else {
            isAuthenticated = false;
            sendResponse("530 Mật khẩu không đúng");
        }
    }

    /**
     * Xử lý lệnh STOR (upload file)
     * @param filename Tên file cần upload
     */
    private void handleStore(String filename) {
        if (!isAuthenticated) {
            sendResponse("530 Vui lòng đăng nhập trước");
            return;
        }

        try {
            // Thông báo sẵn sàng nhận file (mã 150)
            sendResponse("150 Sẵn sàng nhận file " + filename);

            // Đọc kích thước file
            String fileSizeStr = reader.readLine();
            long fileSize = Long.parseLong(fileSizeStr);
            System.out.println("Kích thước file: " + fileSize + " bytes");

            // Nhận dữ liệu file và lưu vào thư mục data
            Path filePath = Paths.get(DATA_DIR, filename);
            
            // Không đóng InputStream vì nó là socket stream, sẽ làm đóng kết nối
            InputStream inputStream = clientSocket.getInputStream();
            
            try (FileOutputStream fileOutputStream = new FileOutputStream(filePath.toFile())) {
                
                byte[] buffer = new byte[4096];
                long totalBytesRead = 0;
                int bytesRead;
                
                // Đọc dữ liệu cho đến khi đủ kích thước file
                while (totalBytesRead < fileSize && 
                       (bytesRead = inputStream.read(buffer, 0, 
                           (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    
                    // Hiển thị tiến trình
                    int progress = (int) ((totalBytesRead * 100) / fileSize);
                    if (progress % 10 == 0) {
                        System.out.println("Đã nhận: " + progress + "%");
                    }
                }
                
                System.out.println("File đã được lưu: " + filePath.toAbsolutePath());
                
            } catch (IOException e) {
                System.err.println("Lỗi khi lưu file: " + e.getMessage());
                sendResponse("550 Lỗi khi lưu file");
                return; // Không throw, chỉ return để tiếp tục lắng nghe
            }
            
            // Gửi phản hồi thành công sau khi đóng file
            sendResponse("226 Upload hoàn tất");
            System.out.println("Upload hoàn tất, sẵn sàng nhận lệnh tiếp theo");
            
        } catch (IOException e) {
            System.err.println("Lỗi khi xử lý upload: " + e.getMessage());
            sendResponse("550 Lỗi khi xử lý upload");
            // Không throw exception, để server tiếp tục lắng nghe
        }
    }

    /**
     * Xử lý lệnh QUIT (ngắt kết nối)
     */
    private void handleQuit() {
        sendResponse("221 Tạm biệt");
        System.out.println("Client đã ngắt kết nối");
    }

    /**
     * Gửi phản hồi về client
     * @param response Chuỗi phản hồi
     */
    private void sendResponse(String response) {
        writer.println(response);
        System.out.println("Gửi: " + response);
    }

    /**
     * Đóng kết nối và giải phóng tài nguyên
     */
    private void closeConnection() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            System.out.println("Đã đóng kết nối với client");
        } catch (IOException e) {
            System.err.println("Lỗi khi đóng kết nối: " + e.getMessage());
        }
    }
}
