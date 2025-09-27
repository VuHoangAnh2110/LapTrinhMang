package client;

import java.io.*;
import java.net.*;
import javax.swing.SwingUtilities;

public class ClientGame {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private ClientGUI gui;
    private boolean connected;
    
    public ClientGame() {
        gui = new ClientGUI(this);
    }
    
    public void connectToServer() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
            
            gui.setStatus("Connected to server");
            gui.resetBoard();
            System.out.println("Connected to server successfully");
            
            // Start listening for server messages
            Thread messageListener = new Thread(this::listenForMessages);
            messageListener.setDaemon(true);
            messageListener.start();
            
        } catch (IOException e) {
            gui.setStatus("Failed to connect to server: " + e.getMessage());
            System.err.println("Connection failed: " + e.getMessage());
        }
    }
    
    private void listenForMessages() {
        try {
            String message;
            while (connected && (message = reader.readLine()) != null) {
                System.out.println("Received from server: " + message);
                handleServerMessage(message);
            }
        } catch (IOException e) {
            if (connected) {
                System.err.println("Connection lost: " + e.getMessage());
                SwingUtilities.invokeLater(() -> {
                    gui.setStatus("Connection lost: " + e.getMessage());
                });
            }
        }
    }
    
    private void handleServerMessage(String message) {
        String[] parts = message.split(":", 2);
        String command = parts[0];
        
        switch (command) {
            case "WAITING":
                SwingUtilities.invokeLater(() -> {
                    gui.setStatus("Waiting for another player...");
                });
                break;
                
            case "GAME_START":
                if (parts.length > 1) {
                    String symbol = parts[1];
                    SwingUtilities.invokeLater(() -> {
                        gui.setPlayerSymbol(symbol);
                        gui.setStatus("Game started! You are " + symbol);
                        gui.setGameStarted(true);
                    });
                    System.out.println("Game started, player is: " + symbol);
                }
                break;
                
            case "BOARD_UPDATE":
                if (parts.length > 1) {
                    String[] boardData = parts[1].split(",");
                    SwingUtilities.invokeLater(() -> {
                        gui.updateBoard(boardData);
                    });
                    System.out.println("Board updated with " + boardData.length + " cells");
                }
                break;
                
            case "YOUR_TURN":
                SwingUtilities.invokeLater(() -> {
                    gui.setStatus("Your turn! Click on an empty cell");
                    gui.setCanMove(true);
                });
                break;
                
            case "WAIT_TURN":
                SwingUtilities.invokeLater(() -> {
                    gui.setStatus("Opponent's turn - Please wait");
                    gui.setCanMove(false);
                });
                break;
                
            case "YOU_WIN":
                SwingUtilities.invokeLater(() -> {
                    gui.setStatus("🎉 You WIN! 🎉");
                    gui.setCanMove(false);
                    // Show game result dialog
                    gui.showGameResult("WIN");
                });
                break;
                
            case "YOU_LOSE":
                SwingUtilities.invokeLater(() -> {
                    gui.setStatus("😞 You LOSE! 😞");
                    gui.setCanMove(false);
                    // Show game result dialog
                    gui.showGameResult("LOSE");
                });
                break;
                
            case "DRAW":
                SwingUtilities.invokeLater(() -> {
                    gui.setStatus("🤝 Game is DRAW! 🤝");
                    gui.setCanMove(false);
                    // Show game result dialog
                    gui.showGameResult("DRAW");
                });
                break;
                
            case "INVALID_MOVE":
                SwingUtilities.invokeLater(() -> {
                    gui.setStatus("Invalid move! Try again");
                    gui.setCanMove(true);
                });
                break;
                
            case "NOT_YOUR_TURN":
                SwingUtilities.invokeLater(() -> {
                    gui.setStatus("Not your turn! Please wait");
                });
                break;
                
            case "OPPONENT_DISCONNECTED":
                SwingUtilities.invokeLater(() -> {
                    gui.setStatus("Opponent disconnected! Game ended");
                    gui.setCanMove(false);
                });
                break;
                
            default:
                System.out.println("Unknown message from server: " + message);
        }
    }
    
    public void makeMove(int row, int col) {
        if (connected && writer != null) {
            String moveMessage = "MOVE:" + row + ":" + col;
            writer.println(moveMessage);
            System.out.println("Sent move to server: " + moveMessage);
            
            // Temporarily disable moves until server responds
            SwingUtilities.invokeLater(() -> {
                gui.setCanMove(false);
                gui.setStatus("Processing move...");
            });
        } else {
            System.err.println("Cannot make move: not connected to server");
        }
    }
    
    public void disconnect() {
        connected = false;
        
        if (writer != null) {
            writer.println("QUIT");
        }
        
        try {
            if (socket != null) socket.close();
            if (reader != null) reader.close();
            if (writer != null) writer.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
        
        System.out.println("Disconnected from server");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientGame game = new ClientGame();
            game.connectToServer();
        });
    }
}