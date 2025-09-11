import java.io.*;
import java.net.*;

public class MayTinhClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected = false;
    
    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
            return true;
        } catch (IOException e) {
            connected = false;
            return false;
        }
    }
    
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            connected = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String calculate(double num1, double num2, String operation) throws IOException {
        if (!connected || socket == null || socket.isClosed()) {
            throw new IOException("Chua ket noi den server");
        }
        
        String request = num1 + "," + num2 + "," + operation;
        out.println(request);
        
        String response = in.readLine();
        if (response == null) {
            connected = false;
            throw new IOException("Server da ngat ket noi");
        }
        
        return response;
    }
    
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
}