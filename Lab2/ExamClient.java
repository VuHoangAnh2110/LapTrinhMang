import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ExamClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 9999;
    
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private Scanner scanner;
    
    public void connect() {
        try {
            // Kết nối đến server
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Da ket noi den server " + SERVER_ADDRESS + ":" + SERVER_PORT);
            
            // Thiết lập input/output streams
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            scanner = new Scanner(System.in);
            
            // Đọc thông báo chào mừng từ server
            String welcomeMessage = input.readLine();
            System.out.println(welcomeMessage);
            
            // Bắt đầu giao tiếp
            startCommunication();
            
        } catch (IOException e) {
            System.err.println("Loi ket noi: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    private void startCommunication() {
        try {
            System.out.println("Nhap tin nhan (go 'quit' de thoat):");
            
            String userInput;
            while (true) {
                System.out.print(">> ");
                userInput = scanner.nextLine();
                
                // Gửi tin nhắn đến server
                output.println(userInput);
                
                // Đọc phản hồi từ server
                String serverResponse = input.readLine();
                if (serverResponse != null) {
                    System.out.println("Server: " + serverResponse);
                }
                
                // Thoát nếu người dùng gõ "quit"
                if ("quit".equalsIgnoreCase(userInput)) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Loi giao tiep: " + e.getMessage());
        }
    }
    
    private void cleanup() {
        try {
            if (scanner != null) scanner.close();
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
            System.out.println("Da ngat ket noi khoi server");
        } catch (IOException e) {
            System.err.println("LLoi khi dong ket noi: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        ExamClient client = new ExamClient();
        client.connect();
    }
}