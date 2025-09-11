import java.net.*;
import java.io.*;
import java.util.*;

public class example25 {
    private static final int DEFAULT_PORT = 8080;
    private static final int TIMEOUT = 5000;
    
    public static void main(String[] args) {
        String serverHost = "localhost";
        int serverPort = DEFAULT_PORT;
        
        System.out.println("=== DOC DU LIEU SO TU MAY CHU ===");
        System.out.println();
        
        startTestServer(serverPort);
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        readNumbersFromServer(serverHost, serverPort);
    }
    
    public static void readNumbersFromServer(String host, int port) {
        System.out.println("Ket noi toi may chu: " + host + ":" + port);
        
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), TIMEOUT);
            socket.setSoTimeout(TIMEOUT);
            

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            

            PrintWriter writer = new PrintWriter(
                socket.getOutputStream(), true
            );

            System.out.println("Ket noi thanh cong!");
            System.out.println("Dang doc du lieu so tu may chu...");
            System.out.println();

            writer.println("GET_NUMBERS");
            
            String line;
            List<Double> numbers = new ArrayList<>();
            int lineCount = 0;
            
            while ((line = reader.readLine()) != null && lineCount < 10) {
                try {
                    double number = Double.parseDouble(line.trim().replace(',', '.'));
                    numbers.add(number);
                    System.out.printf("Nhan duoc so: %.2f\n", number);
                    lineCount++;
                } catch (NumberFormatException e) {
                    System.out.println("Du lieu khong phai so: " + line);
                }
            }
            
            
        } catch (IOException e) {
            System.err.println("LLoi khi doc du lieu tu may chu: " + e.getMessage());
        }
    }
    
    public static Double readSingleNumber(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            
            PrintWriter writer = new PrintWriter(
                socket.getOutputStream(), true
            );
            
            writer.println("GET_SINGLE_NUMBER");
            String response = reader.readLine();
            
            if (response != null) {
                return Double.parseDouble(response.trim());
            }
            
        } catch (IOException | NumberFormatException e) {
            System.err.println("LLoi khi doc so tu may chu: " + e.getMessage());
        }
        
        return null;
    }
    
    
    private static void startTestServer(int port) {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("May chu test dang chay tren port " + port);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> handleClient(clientSocket)).start();
                }
                
            } catch (IOException e) {
                System.err.println("Loi may chu test: " + e.getMessage());
            }
        }).start();
    }
    
    private static void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(
                clientSocket.getOutputStream(), true)) {
            
            String request = reader.readLine();
            System.out.println("Nhan yeu cau: " + request);

            if ("GET_NUMBERS".equals(request)) {
                Random random = new Random();
                for (int i = 0; i < 5; i++) {
                    double number = random.nextDouble() * 100;
                    writer.println(String.format("%.2f", number));
                    
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } else if ("GET_SINGLE_NUMBER".equals(request)) {
                writer.println(String.valueOf(new Random().nextDouble() * 1000));
            }
            
        } catch (IOException e) {
            System.err.println("Loi xu ly client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Loi dong socket: " + e.getMessage());
            }
        }
    }
}