package com.example.uploadfileftp;

import com.example.uploadfileftp.ftpclient.ClientApp;
import com.example.uploadfileftp.ftpserver.FTPServer;

/**
 * Main class để khởi động cả Server và Client
 * Bạn có thể chạy riêng từng phần hoặc chạy cả hai
 */
public class Main {
    
    public static void main(String[] args) {
        if (args.length > 0) {
            String mode = args[0].toLowerCase();
            
            switch (mode) {
                case "server":
                    // Chạy chỉ server
                    System.out.println("Khởi động FTP Server...");
                    startServer(args);
                    break;
                    
                case "client":
                    // Chạy chỉ client
                    System.out.println("Khởi động FTP Client...");
                    ClientApp.main(new String[]{});
                    break;
                    
                default:
                    printUsage();
            }
        } else {
            // Mặc định: chạy cả server và client
            System.out.println("=".repeat(50));
            System.out.println("KHỞI ĐỘNG FTP SERVER VÀ CLIENT");
            System.out.println("=".repeat(50));
            
            // Khởi động server trong thread riêng
            Thread serverThread = new Thread(() -> {
                startServer(new String[]{"2121"});
            });
            serverThread.setDaemon(true); // Cho phép JVM thoát khi chỉ còn daemon thread
            serverThread.start();
            
            // Đợi server khởi động
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Khởi động client GUI
            System.out.println("\nKhởi động Client GUI...\n");
            ClientApp.main(new String[]{});
        }
    }
    
    /**
     * Khởi động FTP Server
     * @param args Tham số dòng lệnh (có thể chứa port)
     */
    private static void startServer(String[] args) {
        int port = 2121; // Port mặc định
        
        // Lấy port từ args nếu có
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Port không hợp lệ, sử dụng port mặc định: 2121");
            }
        }
        
        FTPServer server = new FTPServer(port);
        
        // Thêm shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nĐang dừng server...");
            server.stop();
        }));
        
        server.start();
    }
    
    /**
     * In hướng dẫn sử dụng
     */
    private static void printUsage() {
        System.out.println("Cách sử dụng:");
        System.out.println("  java Main                 - Chạy cả server và client");
        System.out.println("  java Main server [port]   - Chỉ chạy server");
        System.out.println("  java Main client          - Chỉ chạy client");
        System.out.println("\nVí dụ:");
        System.out.println("  java Main server 2121");
    }
}
