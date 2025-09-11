import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MayTinhServer {
    private static final int PORT = 3210;
    private static AtomicInteger clientCount = new AtomicInteger(0);
    
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("=== CALCULATOR SERVER ===");
            System.out.println("Server dang chay tren cong " + PORT);
            System.out.println("Dang cho ket noi tu client...\n");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                int clientId = clientCount.incrementAndGet();
                
                System.out.println(
                    "Client #" + clientId + " da ket noi tu: " + 
                    clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                
                new Thread(new ClientHandler(clientSocket, clientId)).start();
            }
        } catch (IOException e) {
            System.err.println("Loi server: " + e.getMessage());
        }
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private int clientId;
    
    public ClientHandler(Socket socket, int clientId) {
        this.socket = socket;
        this.clientId = clientId;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Loi khoi tao client #" + clientId + ": " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        try {
            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Client #" + clientId + " gui: " + request);
                String result = processCalculation(request);
                out.println(result);
                System.out.println("Tra ve cho Client #" + clientId + ": " + result);
            }
        } catch (IOException e) {
            System.out.println("Client #" + clientId + " da ngat ket noi");
        } finally {
            try {
                socket.close();
                System.out.println("Da dong ket noi voi Client #" + clientId);
            } catch (IOException e) {
                System.err.println("Loi dong ket noi Client #" + clientId);
            }
        }
    }
    
    private String processCalculation(String request) {
        try {
            String[] parts = request.split(",");
            if (parts.length != 3) {
                return "Loi: Format du lieu khong dung";
            }
            
            double num1 = Double.parseDouble(parts[0].trim());
            double num2 = Double.parseDouble(parts[1].trim());
            String operation = parts[2].trim();
            
            double result;
            switch (operation) {
                case "+":
                    result = num1 + num2;
                    break;
                case "-":
                    result = num1 - num2;
                    break;
                case "*":
                    result = num1 * num2;
                    break;
                case "/":
                    if (num2 == 0) {
                        return "Loi: Khong the chia cho 0";
                    }
                    result = num1 / num2;
                    break;
                default:
                    return "Loi: Phep toan '" + operation + "' khong hop le";
            }
            
            return formatResult(result);
        } catch (NumberFormatException e) {
            return "Loi: So khong hop le";
        } catch (Exception e) {
            return "Loi: " + e.getMessage();
        }
    }
    
    private String formatResult(double result) {
        if (result == (long) result) {
            return String.valueOf((long) result);
        } else {
            return String.format("%.6f", result).replaceAll("0*$", "").replaceAll("\\.$", "");
        }
    }
}