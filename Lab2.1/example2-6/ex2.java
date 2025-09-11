import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ex2 {
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
            
            System.out.println("Ket noi thanh cong. Gui du lieu va hien thi noi dung da gui...\n");
            
            // Nội dung client gửi đi
            String messageToSend = "hello";
            
            // Hiển thị nội dung client gửi đi
            System.out.println("Noi dung client gui di: " + messageToSend);
            System.out.println("Do dai: " + messageToSend.length() + " ky tu");
            
            System.out.println();
            
            // Gửi vào socket
            out.println(messageToSend);
            
            System.out.println("\nDa gui thanh cong vao socket!");
            
        } catch (Exception e) {
            System.out.println("Loi: " + e.getMessage());
        }
        
        sc.close();
    }
}