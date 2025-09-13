import java.net.*;
import java.io.*;

public class SumUDPServer {
    private static final int PORT = 9876;
    private DatagramSocket socket;
    private boolean running = false;
    
    public SumUDPServer() throws SocketException {
        socket = new DatagramSocket(PORT);
        System.out.println("Sum UDP Server da khoi dong tren port " + PORT);
        System.out.println("Dang cho yeu cau tu client...");
    }
    
    public void start() throws IOException {
        running = true;
        byte[] buffer = new byte[1024];
        
        while (running) {
            // Nhận yêu cầu từ client
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            
            String receivedData = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Nhan tu client: " + receivedData);
            
            // Xử lý yêu cầu
            String response = processRequest(receivedData);
            
            // Gửi kết quả về client
            byte[] responseBuffer = response.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(
                responseBuffer, responseBuffer.length, 
                packet.getAddress(), packet.getPort());
            
            socket.send(responsePacket);
            System.out.println("Da gui ket qua: " + response);
        }
    }
    
    private String processRequest(String request) {
        try {
            // Parse số n từ request
            int n = Integer.parseInt(request.trim());
            
            if (n <= 0) {
                return "ERROR: n phai lon hon 0";
            }
            
            // Tính tổng S = 1 + 2 + ... + n
            long sum = calculateSum(n);
            
            return String.valueOf(sum);
            
        } catch (NumberFormatException e) {
            return "ERROR: Du lieu khong hop le";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
    
    private long calculateSum(int n) {
        // Sử dụng công thức S = n*(n+1)/2 để tính nhanh
        return (long)n * (n + 1) / 2;
    }
    
    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
    
    public static void main(String[] args) {
        try {
            SumUDPServer server = new SumUDPServer();
            
            // Thêm shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            
            server.start();
        } catch (Exception e) {
            System.err.println("Loi server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}