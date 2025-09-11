package shared.utils;

import java.net.*;
import java.io.*;

public class NetworkUtils {
    
    public static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public static String getLocalIPAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }
    
    public static boolean isValidIPAddress(String ip) {
        try {
            InetAddress.getByName(ip);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }
    
    public static boolean canConnectToServer(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    public static void closeQuietly(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    public static void closeQuietly(ServerSocket serverSocket) {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    public static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    public static class ConnectionInfo {
        private final String host;
        private final int port;
        private final long connectTime;
        private final boolean isConnected;
        
        public ConnectionInfo(String host, int port, long connectTime, boolean isConnected) {
            this.host = host;
            this.port = port;
            this.connectTime = connectTime;
            this.isConnected = isConnected;
        }
        
        public String getHost() { return host; }
        public int getPort() { return port; }
        public long getConnectTime() { return connectTime; }
        public boolean isConnected() { return isConnected; }
        
        @Override
        public String toString() {
            return String.format("Connection{host='%s', port=%d, connected=%s, time=%dms}", 
                host, port, isConnected, connectTime);
        }
    }
    
    public static ConnectionInfo testConnection(String host, int port) {
        long startTime = System.currentTimeMillis();
        boolean connected = canConnectToServer(host, port, 5000);
        long connectTime = System.currentTimeMillis() - startTime;
        
        return new ConnectionInfo(host, port, connectTime, connected);
    }
}