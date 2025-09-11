import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ExamServer {
    private static final int PORT = 9999;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    
    public ExamServer() {
        // Để tạo thread pool, cho phép xử lý nhiều client đồng thời
        threadPool = Executors.newCachedThreadPool();
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server dang nghe tren cong " + PORT);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client moi ket noi: " + clientSocket.getInetAddress());
                
                // Tạo thread mới để xử lý client
                threadPool.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Loi server: " + e.getMessage());
        }
    }
    
    public void stop() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (threadPool != null) {
                threadPool.shutdown();
            }
        } catch (IOException e) {
            System.err.println("Loi khi dong server: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        ExamServer server = new ExamServer();
        server.start();
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }
    
    @Override
    public void run() {
        try {
            // Thiết lập input/output streams
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);
            
            // Gửi thông báo chào mừng
            output.println("Chao mung ban den voi server!");

            String inputLine;
            while ((inputLine = input.readLine()) != null) {
                System.out.println("Nhan tu client " + clientSocket.getInetAddress() + ": " + inputLine);

                // Kiểm tra nếu client gửi "quit" thì ngắt kết nối
                if ("quit".equalsIgnoreCase(inputLine)) {
                    output.println("Tam biet!");
                    System.out.println("Client " + clientSocket.getInetAddress() + " yeu cau ngat ket noi");
                    break;
                }
                
                // Đảo chuỗi
                String reversedString = reverseString(inputLine);
                
                // Gửi chuỗi đã được đảo cho client
                output.println("Chuoi dao nguoc: " + reversedString);
            }
            
        } catch (IOException e) {
            System.err.println("Loi xu ly client: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    // Phương thức đảo chuỗi
    private String reverseString(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return new StringBuilder(str).reverse().toString();
    }
    
    private void cleanup() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (clientSocket != null) clientSocket.close();
            System.out.println("Client " + clientSocket.getInetAddress() + " da ngat ket noi");
        } catch (IOException e) {
            System.err.println("Loi khi dong ket noi: " + e.getMessage());
        }
    }
}