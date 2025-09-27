package shared.utils;

import shared.network.Message;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Network utility functions for connection management, validation, and serialization
 * Provides common networking operations used by both client and server
 */
public class NetworkUtils {
    
    // Network validation patterns
    private static final Pattern IP_PATTERN = Pattern.compile(
        "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );
    
    private static final Pattern HOSTNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?(\\.([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?))*$"
    );
    
    // Connection timeouts
    public static final int DEFAULT_CONNECTION_TIMEOUT = 5000; // 5 seconds
    public static final int DEFAULT_READ_TIMEOUT = 10000; // 10 seconds
    public static final int DEFAULT_KEEP_ALIVE_INTERVAL = 30000; // 30 seconds
    
    // Message size limits
    public static final int MAX_MESSAGE_SIZE = 1024 * 1024; // 1MB
    public static final int MAX_USERNAME_LENGTH = 50;
    public static final int MAX_ROOM_ID_LENGTH = 20;
    
    /**
     * Validates if a string is a valid IP address
     */
    public static boolean isValidIPAddress(String ip) {
        return ip != null && IP_PATTERN.matcher(ip.trim()).matches();
    }
    
    /**
     * Validates if a string is a valid hostname
     */
    public static boolean isValidHostname(String hostname) {
        return hostname != null && HOSTNAME_PATTERN.matcher(hostname.trim()).matches();
    }
    
    /**
     * Validates if a string is a valid host (IP or hostname)
     */
    public static boolean isValidHost(String host) {
        return isValidIPAddress(host) || isValidHostname(host);
    }
    
    /**
     * Validates if a port number is valid
     */
    public static boolean isValidPort(int port) {
        return port >= 1 && port <= 65535;
    }
    
    /**
     * Validates if a username meets requirements
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = username.trim();
        return trimmed.length() >= 3 && 
               trimmed.length() <= MAX_USERNAME_LENGTH &&
               trimmed.matches("^[a-zA-Z0-9_]+$"); // Alphanumeric and underscore only
    }
    
    /**
     * Validates if a room ID meets requirements
     */
    public static boolean isValidRoomId(String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = roomId.trim();
        return trimmed.length() <= MAX_ROOM_ID_LENGTH &&
               trimmed.matches("^[A-Z0-9]+$"); // Uppercase alphanumeric only
    }
    
    /**
     * Tests connection to a host and port
     */
    public static boolean testConnection(String host, int port) {
        return testConnection(host, port, DEFAULT_CONNECTION_TIMEOUT);
    }
    
    /**
     * Tests connection to a host and port with custom timeout
     */
    public static boolean testConnection(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Gets the local IP address
     */
    public static String getLocalIPAddress() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            return localhost.getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }
    
    /**
     * Gets all local IP addresses
     */
    public static List<String> getAllLocalIPAddresses() {
        List<String> addresses = new ArrayList<>();
        
        try {
            List<NetworkInterface> interfaces = java.util.Collections.list(
                NetworkInterface.getNetworkInterfaces());
            
            for (NetworkInterface networkInterface : interfaces) {
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                List<InetAddress> inetAddresses = java.util.Collections.list(
                    networkInterface.getInetAddresses());
                
                for (InetAddress address : inetAddresses) {
                    if (address instanceof Inet4Address) {
                        addresses.add(address.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            addresses.add("127.0.0.1");
        }
        
        return addresses;
    }
    
    /**
     * Safely sends a message through an ObjectOutputStream
     */
    public static boolean sendMessage(ObjectOutputStream output, Message message) {
        try {
            if (output != null && message != null) {
                // Check message size
                byte[] serialized = serializeMessage(message);
                if (serialized.length > MAX_MESSAGE_SIZE) {
                    throw new IOException("Message too large: " + serialized.length + " bytes");
                }
                
                output.writeObject(message);
                output.flush();
                return true;
            }
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Safely receives a message from an ObjectInputStream
     */
    public static Message receiveMessage(ObjectInputStream input) {
        try {
            if (input != null) {
                Object obj = input.readObject();
                if (obj instanceof Message) {
                    return (Message) obj;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error receiving message: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Serializes a message to byte array
     */
    public static byte[] serializeMessage(Message message) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(message);
            oos.flush();
            return baos.toByteArray();
        }
    }
    
    /**
     * Deserializes a message from byte array
     */
    public static Message deserializeMessage(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            Object obj = ois.readObject();
            if (obj instanceof Message) {
                return (Message) obj;
            }
            throw new ClassNotFoundException("Object is not a Message");
        }
    }
    
    /**
     * Creates a socket with default timeouts
     */
    public static Socket createSocket(String host, int port) throws IOException {
        return createSocket(host, port, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }
    
    /**
     * Creates a socket with custom timeouts
     */
    public static Socket createSocket(String host, int port, int connectionTimeout, int readTimeout) 
            throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(readTimeout);
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true);
        socket.connect(new InetSocketAddress(host, port), connectionTimeout);
        return socket;
    }
    
    /**
     * Safely closes a socket
     */
    public static void closeSocket(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
    
    /**
     * Safely closes an InputStream
     */
    public static void closeInputStream(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                System.err.println("Error closing input stream: " + e.getMessage());
            }
        }
    }
    
    /**
     * Safely closes an OutputStream
     */
    public static void closeOutputStream(OutputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                System.err.println("Error closing output stream: " + e.getMessage());
            }
        }
    }
    
    /**
     * Gets a free port on the local machine
     */
    public static int getFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            return -1;
        }
    }
    
    /**
     * Checks if a port is available on the local machine
     */
    public static boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Formats a socket address as string
     */
    public static String formatSocketAddress(Socket socket) {
        if (socket == null) return "null";
        
        InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
        InetSocketAddress localAddress = (InetSocketAddress) socket.getLocalSocketAddress();
        
        return String.format("%s:%d -> %s:%d",
            remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort(),
            localAddress.getAddress().getHostAddress(), localAddress.getPort());
    }
    
    /**
     * Calculates network latency by measuring round-trip time
     */
    public static long measureLatency(String host, int port) {
        long startTime = System.currentTimeMillis();
        
        try (Socket socket = createSocket(host, port, 3000, 3000)) {
            return System.currentTimeMillis() - startTime;
        } catch (IOException e) {
            return -1; // Connection failed
        }
    }
    
    /**
     * Validates message content for security
     */
    public static boolean isMessageSafe(Message message) {
        if (message == null) return false;
        
        // Check username length
        if (message.getUsername() != null && 
            message.getUsername().length() > MAX_USERNAME_LENGTH) {
            return false;
        }
        
        // Check for null or invalid message types
        if (message.getType() == null) {
            return false;
        }
        
        // Additional security checks can be added here
        return true;
    }
    
    /**
     * Sanitizes a string for network transmission
     */
    public static String sanitizeString(String input) {
        if (input == null) return null;
        
        return input.trim()
            .replaceAll("[\\r\\n\\t]", " ") // Replace line breaks and tabs with spaces
            .replaceAll("\\s+", " ") // Replace multiple spaces with single space
            .substring(0, Math.min(input.length(), 1000)); // Limit length
    }
    
    /**
     * Generates a unique session ID
     */
    public static String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + 
               Integer.toHexString((int)(Math.random() * 0x1000000));
    }
    
    /**
     * Checks if a connection is still alive
     */
    public static boolean isConnectionAlive(Socket socket) {
        if (socket == null || socket.isClosed()) {
            return false;
        }
        
        try {
            // Try to send a byte and see if it fails
            socket.getOutputStream().write(0);
            socket.getOutputStream().flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}