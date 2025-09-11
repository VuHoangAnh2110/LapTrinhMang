import java.io.*;
import java.net.*;
import java.util.Scanner;

public class SocketReader {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        System.out.print("Nhap host:port: ");
        String input = sc.nextLine().trim();
        
        String[] parts = input.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);
        
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            System.out.println("Ket noi thanh cong. Gui 'hello' va doc du lieu...\n");
            
            // Gửi "hello" vào socket
            out.println("32,10,-");
            
            // Đọc phản hồi
            String response = in.readLine();
            if (response != null) {
                System.out.println("Phan hoi: " + response);
            } else {
                System.out.println("Khong co phan hoi");
            }
            
        } catch (Exception e) {
            System.out.println("Loi: " + e.getMessage());
        }
        
        sc.close();
    }
}