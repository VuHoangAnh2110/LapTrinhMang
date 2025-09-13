import java.net.*;
import java.io.*;
import java.util.Scanner;

public class ChatUDPServer {
    private static final int PORT = 9876;
    private DatagramSocket socket;
    private InetAddress clientAddress;
    private int clientPort;
    private volatile boolean running = false;
    
    public ChatUDPServer() throws SocketException {
        socket = new DatagramSocket(PORT);
        System.out.println("Chat UDP Server da khoi dong tren port " + PORT);
        System.out.println("Dang cho ket noi tu client...");
    }
    
    public void start() throws IOException {
        running = true;
        
        // Thread để nhận tin nhắn
        Thread receiveThread = new Thread(() -> {
            try {
                receiveMessages();
            } catch (IOException e) {
                if (running) {
                    System.err.println("Loi khi nhan tin nhan: " + e.getMessage());
                }
            }
        });
        receiveThread.start();
        
        // Thread để gửi tin nhắn
        Thread sendThread = new Thread(() -> {
            try {
                sendMessages();
            } catch (IOException e) {
                if (running) {
                    System.err.println("Loi khi gui tin nhan: " + e.getMessage());
                }
            }
        });
        sendThread.start();
        
        try {
            receiveThread.join();
            sendThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        stop();
    }
    
    private void receiveMessages() throws IOException {
        byte[] buffer = new byte[1024];
        
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                String message = new String(packet.getData(), 0, packet.getLength());
                
                // Lưu thông tin client để gửi lại
                if (clientAddress == null) {
                    clientAddress = packet.getAddress();
                    clientPort = packet.getPort();
                    System.out.println("Client da ket noi tu: " + clientAddress + ":" + clientPort);
                }
                
                if (message.trim().equalsIgnoreCase("quit")) {
                    System.out.println("Client: " + message);
                    System.out.println("Client da thoat chat!");
                    running = false;
                    socket.close();
                    break;
                }
                
                System.out.println("Client: " + message);
                
            } catch (SocketException e) {
                if (running) {
                    throw e;
                }
                break;
            }
        }
    }
    
    private void sendMessages() throws IOException {
        Scanner scanner = new Scanner(System.in);
        
        while (running) {
            if (clientAddress != null) {
                System.out.print(">> ");
                String message = scanner.nextLine();
                
                // Kiểm tra quit ngay khi nhập
                if (message.trim().equalsIgnoreCase("quit")) {
                    sendMessage(message);
                    System.out.println("Da thoat chat!");
                    running = false;
                    scanner.close();
                    socket.close();
                    System.exit(0);
                }
                
                sendMessage(message);
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        scanner.close();
    }
    
    private void sendMessage(String message) throws IOException {
        if (clientAddress != null && !socket.isClosed()) {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length, clientAddress, clientPort);
            socket.send(packet);
        }
    }
    
    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        System.exit(0);
    }
    
    public static void main(String[] args) {
        try {
            ChatUDPServer server = new ChatUDPServer();
            
            // Thêm shutdown hook để đóng socket khi thoát
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            
            server.start();
        } catch (Exception e) {
            System.err.println("Loi server: " + e.getMessage());
        }
    }
}