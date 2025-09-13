import java.net.*;
import java.io.*;

public class SumUDPServer1 {
    private static final int PORT = 9001;
    private DatagramSocket socket;
    private boolean running = false;
    
    public SumUDPServer1() throws SocketException {
        socket = new DatagramSocket(PORT);
        System.out.println("Server 1 (a+b) da khoi dong tren port " + PORT);
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
            // Parse a và b từ request (format: "a,b")
            String[] parts = request.trim().split(",");
            if (parts.length != 2) {
                return "ERROR: Dinh dang khong hop le. Can: a,b";
            }
            
            double a = Double.parseDouble(parts[0]);
            double b = Double.parseDouble(parts[1]);
            
            // Tính a + b
            double result = a + b;
            
            return String.valueOf(result);
            
        } catch (NumberFormatException e) {
            return "ERROR: Du lieu khong hop le";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
    
    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
    
    public static void main(String[] args) {
        try {
            SumUDPServer1 server = new SumUDPServer1();
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            server.start();
        } catch (Exception e) {
            System.err.println("Loi SumUDPServer1: " + e.getMessage());
            e.printStackTrace();
        }
    }
}