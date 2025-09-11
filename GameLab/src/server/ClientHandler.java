package server;

import shared.models.Message;
import shared.models.Player;
import java.net.Socket;
import java.io.*;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private GameServer server;
    private String username;
    private boolean isConnected;
    
    public ClientHandler(Socket socket, GameServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.isConnected = true;
        
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error setting up client streams: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        try {
            while (isConnected) {
                Message message = (Message) input.readObject();
                processMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Client disconnected: " + e.getMessage());
        } finally {
            disconnect();
        }
    }
    
    private void processMessage(Message message) {
        switch (message.getType()) {
            case LOGIN:
                handleLogin(message);
                break;
            case MOVE:
                handleMove(message);
                break;
            case ATTACK:
                handleAttack(message);
                break;
            case LOGOUT:
                disconnect();
                break;
        }
    }
    
    private void handleLogin(Message message) {
        this.username = message.getUsername();
        server.addClient(username, this);
        
        // Send current game state to new player
        sendMessage(server.getGameEngine().getGameState());
    }
    
    private void handleMove(Message message) {
        server.getGameEngine().updatePlayerPosition(username, message.getData());
        server.broadcastMessage(message, username);
    }
    
    private void handleAttack(Message message) {
        server.getGameEngine().processAttack(username);
        server.broadcastMessage(message, username);
    }
    
    public void sendMessage(Object message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            System.err.println("Error sending message to client: " + e.getMessage());
        }
    }
    
    private void disconnect() {
        isConnected = false;
        if (username != null) {
            server.removeClient(username);
        }
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
    }
}