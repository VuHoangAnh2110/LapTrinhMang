import java.net.*;
import java.io.*;
import java.util.Scanner;

public class ChatUDPClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9876;
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private volatile boolean running = false;
    
    public ChatUDPClient() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        serverAddress = InetAddress.getByName(SERVER_HOST);
        System.out.println("Chat UDP Client da khoi dong");
        System.out.println("Ket noi den server: " + SERVER_HOST + ":" + SERVER_PORT);
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
                
                if (message.trim().equalsIgnoreCase("quit")) {
                    System.out.println("Server: " + message);
                    System.out.println("Server da thoat chat!");
                    running = false;
                    // Đóng socket để interrupt thread khác
                    socket.close();
                    break;
                }
                
                System.out.println("Server: " + message);
                
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
            System.out.print(">> ");
            String message = scanner.nextLine();
            
            // Kiểm tra quit ngay khi nhập
            if (message.trim().equalsIgnoreCase("quit")) {
                sendMessage(message);
                System.out.println("Da thoat chat!");
                running = false;
                scanner.close();
                socket.close(); // Đóng socket ngay lập tức
                System.exit(0); // Thoát ngay
            }
            
            sendMessage(message);
        }
        scanner.close();
    }
    
    private void sendMessage(String message) throws IOException {
        if (!socket.isClosed()) {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length, serverAddress, SERVER_PORT);
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
            ChatUDPClient client = new ChatUDPClient();
            
            // Thêm shutdown hook để đóng socket khi thoát
            Runtime.getRuntime().addShutdownHook(new Thread(client::stop));
            
            client.start();
        } catch (Exception e) {
            System.err.println("Loi client: " + e.getMessage());
        }
    }
}