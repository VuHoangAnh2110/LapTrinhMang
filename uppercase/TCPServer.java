import java.io.*;
import java.net.*;

public class TCPServer {
    public static void main(String[] args) {
        int port = 8888;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server running on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket.getInetAddress());

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                String received;
                while ((received = in.readLine()) != null) {
                    if (received.equalsIgnoreCase("exit")) break;
                    String response = received.toUpperCase();
                    out.println(response);
                    System.out.println("Received: " + received + " -> Sent: " + response);
                }

                socket.close();
                System.out.println("Client disconnected");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
