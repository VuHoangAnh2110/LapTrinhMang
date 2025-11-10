package com.example.uploadfileftp.ftpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * FTP Server đơn giản chạy trên localhost
 * Lắng nghe kết nối từ client và tạo thread riêng cho mỗi client
 */
public class FTPServer {
    private static final int DEFAULT_PORT = 2121;
    private ServerSocket serverSocket;
    private boolean isRunning;

    /**
     * Khởi tạo FTP Server với port mặc định
     */
    public FTPServer() {
        this(DEFAULT_PORT);
    }

    /**
     * Khởi tạo FTP Server với port tùy chọn
     * @param port Cổng lắng nghe
     */
    public FTPServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            System.out.println("FTP Server đã khởi động trên cổng " + port);
        } catch (IOException e) {
            System.err.println("Không thể khởi động server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Bắt đầu lắng nghe và chấp nhận kết nối từ client
     */
    public void start() {
        System.out.println("Server đang chờ kết nối từ client...");
        
        while (isRunning) {
            try {
                // Chấp nhận kết nối từ client
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client đã kết nối: " + clientSocket.getInetAddress());
                
                // Tạo thread riêng để xử lý client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread thread = new Thread(clientHandler);
                thread.start();
                
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Lỗi khi chấp nhận kết nối: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Dừng server
     */
    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Server đã dừng");
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi đóng server: " + e.getMessage());
        }
    }

    /**
     * Main method để chạy server độc lập
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        // Cho phép chỉ định port từ command line
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Port không hợp lệ, sử dụng port mặc định: " + DEFAULT_PORT);
            }
        }
        
        FTPServer server = new FTPServer(port);
        
        // Thêm shutdown hook để đóng server khi thoát
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nĐang dừng server...");
            server.stop();
        }));
        
        server.start();
    }
}
