package client;

import shared.models.Message;
import shared.models.GameState;
import shared.utils.Constants;
import javafx.application.Platform;
import client.fx.GameController;
import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameClient {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private String username;
    private GameController gameController;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private BlockingQueue<Message> responseQueue;
    private Thread listenThread;
    private final Object sendLock = new Object();
    private volatile boolean shouldReconnect = false;
    private long lastMoveTime = 0;
    private static final long MOVE_THROTTLE = 50; // 50ms between moves
    
    public GameClient() {
        this.responseQueue = new LinkedBlockingQueue<>();
    }
    
    public boolean connect(String host, int port) {
        try {
            System.out.println("Attempting to connect to " + host + ":" + port);
            
            // Close existing connection if any
            disconnect();
            
            socket = new Socket();
            socket.setSoTimeout(5000); // 5 second timeout for reads
            socket.setTcpNoDelay(true); // Disable Nagle's algorithm for low latency
            socket.connect(new InetSocketAddress(host, port), 5000);
            System.out.println("Socket connected successfully");
            
            // Create streams with buffering
            output = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            output.flush();
            System.out.println("Output stream created");
            
            input = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            System.out.println("Input stream created");
            
            isConnected.set(true);
            
            // Start listening thread
            listenThread = new Thread(this::listenForMessages);
            listenThread.setDaemon(true);
            listenThread.setName("GameClient-Listen-" + username);
            listenThread.start();
            System.out.println("Started listening thread");
            
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            e.printStackTrace();
            cleanup();
            return false;
        }
    }
    
    private void listenForMessages() {
        System.out.println("Listening for messages from server...");
        while (isConnected.get() && !Thread.currentThread().isInterrupted()) {
            try {
                // Add timeout to prevent hanging
                if (input.available() > 0 || socket.getInputStream().available() > 0) {
                    Object received = input.readObject();
                    
                    if (received == null) {
                        System.err.println("Received null object from server");
                        continue;
                    }
                    
                    System.out.println("Received object: " + received.getClass().getSimpleName());
                    
                    if (received instanceof GameState) {
                        handleGameState((GameState) received);
                    } else if (received instanceof Message) {
                        handleMessage((Message) received);
                    } else {
                        System.err.println("Received unknown object type: " + received.getClass());
                    }
                } else {
                    // Sleep briefly to prevent busy waiting
                    Thread.sleep(10);
                }
            } catch (SocketTimeoutException e) {
                // Timeout is normal, continue listening
                continue;
            } catch (EOFException e) {
                System.err.println("Server closed connection (EOF)");
                break;
            } catch (SocketException e) {
                if (isConnected.get()) {
                    System.err.println("Socket error: " + e.getMessage());
                }
                break;
            } catch (StreamCorruptedException e) {
                System.err.println("Stream corrupted: " + e.getMessage());
                e.printStackTrace();
                // Try to reset streams
                try {
                    input.reset();
                } catch (IOException resetEx) {
                    System.err.println("Failed to reset input stream: " + resetEx.getMessage());
                    break;
                }
            } catch (ClassNotFoundException e) {
                System.err.println("Unknown class received: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                if (isConnected.get()) {
                    System.err.println("Connection lost: " + e.getMessage());
                    e.printStackTrace();
                }
                break;
            } catch (InterruptedException e) {
                System.out.println("Listen thread interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Unexpected error in listen thread: " + e.getMessage());
                e.printStackTrace();
                break;
            }
        }
        
        System.out.println("Listen thread ending");
        if (isConnected.get()) {
            handleConnectionLost();
        }
    }
    
    private void handleGameState(GameState gs) {
        try {
            System.out.println("Received GameState with " + 
                (gs.getPlayers() != null ? gs.getPlayers().size() : "null") + " players");
            
            // Update JavaFX controller on FX thread
            if (gameController != null) {
                Platform.runLater(() -> {
                    try {
                        gameController.updateGameState(gs);
                    } catch (Exception e) {
                        System.err.println("Error updating GameController: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } else {
                System.err.println("No GameController set to update game state");
            }
        } catch (Exception e) {
            System.err.println("Error handling GameState: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleMessage(Message message) {
        try {
            System.out.println("Handling message: " + message.getType());
            switch (message.getType()) {
                case LOGIN_SUCCESS:
                case LOGIN_FAILED:
                case REGISTER_SUCCESS:
                case REGISTER_FAILED:
                    System.out.println("Adding response to queue: " + message.getType());
                    responseQueue.offer(message);
                    break;
                    
                case PLAYER_JOINED:
                    System.out.println("Player " + message.getUsername() + " joined the game");
                    break;
                    
                case PLAYER_LEFT:
                    System.out.println("Player " + message.getUsername() + " left the game");
                    break;
                    
                case GAME_END:
                    showGameEndAlert(message.getMessage());
                    break;
                    
                case USER_STATS:
                    if (message.getData() instanceof int[]) {
                        int[] stats = (int[]) message.getData();
                        System.out.println("Your stats - Wins: " + stats[0] + ", Losses: " + stats[1]);
                    }
                    break;
                    
                default:
                    System.out.println("Unknown message type: " + message.getType());
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error handling message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showGameEndAlert(String message) {
        if (gameController != null) {
            Platform.runLater(() -> {
                try {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("Game Over");
                    alert.setHeaderText("Game Finished!");
                    alert.setContentText(message);
                    alert.showAndWait();
                } catch (Exception alertException) {
                    System.err.println("Error showing game end alert: " + alertException.getMessage());
                }
            });
        }
    }
    
    private void handleConnectionLost() {
        Platform.runLater(() -> {
            if (gameController != null) {
                try {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Connection Error");
                    alert.setHeaderText("Connection to server lost!");
                    alert.setContentText("The connection was interrupted. Please restart the game.");
                    alert.showAndWait();
                } catch (Exception alertException) {
                    System.err.println("Error showing connection lost alert: " + alertException.getMessage());
                }
            }
        });
        disconnect();
    }
    
    public Message waitForResponse(long timeoutMs) throws InterruptedException {
        System.out.println("Waiting for response, timeout: " + timeoutMs + "ms");
        Message response = responseQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
        if (response != null) {
            System.out.println("Got response: " + response.getType());
        } else {
            System.out.println("Timeout waiting for response");
        }
        return response;
    }
    
    public void sendMessage(Message message) {
        if (!isConnected.get() || output == null) {
            System.err.println("Cannot send message - not connected or output is null");
            return;
        }
        
        synchronized (sendLock) {
            try {
                System.out.println("Sending message: " + message.getType() + " from " + message.getUsername());
                output.writeObject(message);
                output.flush();
                System.out.println("Message sent successfully");
            } catch (IOException e) {
                System.err.println("Failed to send message: " + e.getMessage());
                e.printStackTrace();
                // Mark as disconnected on send failure
                if (isConnected.get()) {
                    handleConnectionLost();
                }
            }
        }
    }
    
    public void sendMove(int x, int y, int direction) {
        // Throttle movement messages to prevent spam
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMoveTime < MOVE_THROTTLE) {
            return; // Skip this move to prevent flooding
        }
        lastMoveTime = currentTime;
        
        if (username != null) {
            int[] positionData = {x, y, direction};
            sendMessage(new Message(Message.Type.MOVE, positionData, username));
        }
    }
    
    public void sendAttack() {
        if (username != null) {
            sendMessage(new Message(Message.Type.ATTACK, null, username));
        }
    }
    
    public void disconnect() {
        System.out.println("Disconnecting client: " + username);
        isConnected.set(false);
        
        // Send logout message if still connected
        if (socket != null && !socket.isClosed() && username != null) {
            try {
                sendMessage(new Message(Message.Type.LOGOUT, null, username));
                Thread.sleep(100); // Give time for message to send
            } catch (Exception e) {
                System.err.println("Error sending logout message: " + e.getMessage());
            }
        }
        
        cleanup();
    }
    
    private void cleanup() {
        // Interrupt listen thread
        if (listenThread != null && listenThread.isAlive()) {
            listenThread.interrupt();
            try {
                listenThread.join(1000); // Wait up to 1 second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Close streams and socket
        try {
            if (output != null) {
                output.close();
                output = null;
            }
        } catch (IOException e) {
            System.err.println("Error closing output stream: " + e.getMessage());
        }
        
        try {
            if (input != null) {
                input.close();
                input = null;
            }
        } catch (IOException e) {
            System.err.println("Error closing input stream: " + e.getMessage());
        }
        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
        
        System.out.println("Client cleanup completed");
    }
    
    public void setGamePanel(GameController gameController) {
        this.gameController = gameController;
        System.out.println("JavaFX GameController set for client: " + (gameController != null ? "not null" : "null"));
    }
    
    public void setUsername(String username) {
        this.username = username;
        System.out.println("Username set to: " + username);
    }
    
    public String getUsername() {
        return username;
    }
    
    public boolean isConnected() {
        return isConnected.get() && socket != null && !socket.isClosed();
    }
}