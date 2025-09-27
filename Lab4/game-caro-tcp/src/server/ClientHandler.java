package server;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private MainServer server;
    private GameRoom gameRoom;
    private boolean running;
    
    public ClientHandler(Socket socket, MainServer server) {
        this.socket = socket;
        this.server = server;
        this.running = true;
        
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("ClientHandler created for " + socket.getInetAddress());
        } catch (IOException e) {
            System.err.println("Error creating client handler: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        try {
            String message;
            while (running && (message = reader.readLine()) != null) {
                System.out.println("Received from client: " + message);
                handleMessage(message);
            }
        } catch (IOException e) {
            System.err.println("Client disconnected: " + e.getMessage());
        } finally {
            disconnect();
        }
    }
    
    private void handleMessage(String message) {
        String[] parts = message.split(":");
        String command = parts[0];
        
        switch (command) {
            case "MOVE":
                if (gameRoom != null && parts.length == 3) {
                    try {
                        int row = Integer.parseInt(parts[1]);
                        int col = Integer.parseInt(parts[2]);
                        System.out.println("Processing move: (" + row + "," + col + ")");
                        gameRoom.makeMove(this, row, col);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid move format: " + message);
                        sendMessage("INVALID_MOVE");
                    }
                } else {
                    System.out.println("Move rejected: No game room or invalid format");
                }
                break;
            case "QUIT":
                System.out.println("Client requested quit");
                disconnect();
                break;
            default:
                System.out.println("Unknown command: " + command);
        }
    }
    
    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
            System.out.println("Sent to client: " + message);
        }
    }
    
    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
        System.out.println("Client assigned to game room");
    }
    
    public void disconnect() {
        running = false;
        System.out.println("Disconnecting client");
        
        if (gameRoom != null) {
            gameRoom.playerDisconnected(this);
        } else {
            server.removeWaitingClient(this);
        }
        
        try {
            if (socket != null) socket.close();
            if (reader != null) reader.close();
            if (writer != null) writer.close();
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }
    }
}