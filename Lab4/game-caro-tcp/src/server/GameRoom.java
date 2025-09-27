package server;

import shared.CaroBoard;

public class GameRoom {
    private ClientHandler player1;
    private ClientHandler player2;
    private CaroBoard board;
    private int currentPlayer;
    private boolean gameActive;
    private MainServer server;
    
    public GameRoom(ClientHandler player1, ClientHandler player2, MainServer server) {
        this.player1 = player1;
        this.player2 = player2;
        this.board = new CaroBoard();
        this.currentPlayer = CaroBoard.PLAYER_X;
        this.gameActive = false;
        this.server = server;
        
        player1.setGameRoom(this);
        player2.setGameRoom(this);
    }
    
    public void startGame() {
        gameActive = true;
        System.out.println("Starting game in room");
        
        // Notify players about game start and their symbols
        player1.sendMessage("GAME_START:X");
        player2.sendMessage("GAME_START:O");
        
        // Send initial board state
        sendBoardUpdate();
        
        // Small delay to ensure board is updated first
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Notify whose turn it is
        player1.sendMessage("YOUR_TURN");
        player2.sendMessage("WAIT_TURN");
        
        System.out.println("Game started successfully. Player X turn.");
    }
    
    public synchronized void makeMove(ClientHandler player, int row, int col) {
        if (!gameActive) {
            System.out.println("Move rejected: Game not active");
            player.sendMessage("INVALID_MOVE");
            return;
        }
        
        System.out.println("Move attempt: Player " + (player == player1 ? "X" : "O") + 
                          " at (" + row + "," + col + ")");
        
        // Check if it's the player's turn
        if ((currentPlayer == CaroBoard.PLAYER_X && player != player1) ||
            (currentPlayer == CaroBoard.PLAYER_O && player != player2)) {
            player.sendMessage("NOT_YOUR_TURN");
            System.out.println("Move rejected: Not player's turn");
            return;
        }
        
        // Try to make the move
        if (board.makeMove(row, col, currentPlayer)) {
            System.out.println("Move accepted: " + (currentPlayer == CaroBoard.PLAYER_X ? "X" : "O") + 
                              " placed at (" + row + "," + col + ")");
            
            // Send board update to both players FIRST
            sendBoardUpdate();
            
            // Small delay to ensure board update is processed
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Check for win
            if (board.checkWin(row, col, currentPlayer)) {
                gameActive = false;
                if (currentPlayer == CaroBoard.PLAYER_X) {
                    player1.sendMessage("YOU_WIN");
                    player2.sendMessage("YOU_LOSE");
                    System.out.println("Player X wins!");
                } else {
                    player2.sendMessage("YOU_WIN");
                    player1.sendMessage("YOU_LOSE");
                    System.out.println("Player O wins!");
                }
                endGame();
                return;
            }
            
            // Check for draw
            if (board.isBoardFull()) {
                gameActive = false;
                player1.sendMessage("DRAW");
                player2.sendMessage("DRAW");
                System.out.println("Game ended in draw");
                endGame();
                return;
            }
            
            // Switch turns
            currentPlayer = (currentPlayer == CaroBoard.PLAYER_X) ? 
                            CaroBoard.PLAYER_O : CaroBoard.PLAYER_X;
            
            // Notify players about turn change
            if (currentPlayer == CaroBoard.PLAYER_X) {
                player1.sendMessage("YOUR_TURN");
                player2.sendMessage("WAIT_TURN");
                System.out.println("Turn switched to Player X");
            } else {
                player2.sendMessage("YOUR_TURN");
                player1.sendMessage("WAIT_TURN");
                System.out.println("Turn switched to Player O");
            }
        } else {
            player.sendMessage("INVALID_MOVE");
            // Re-enable the player's turn since move was invalid
            if (currentPlayer == CaroBoard.PLAYER_X && player == player1) {
                player1.sendMessage("YOUR_TURN");
            } else if (currentPlayer == CaroBoard.PLAYER_O && player == player2) {
                player2.sendMessage("YOUR_TURN");
            }
            System.out.println("Move rejected: Invalid position");
        }
    }
    
    private void sendBoardUpdate() {
        StringBuilder boardData = new StringBuilder("BOARD_UPDATE:");
        int[][] boardArray = board.getBoard();
        
        for (int i = 0; i < CaroBoard.BOARD_SIZE; i++) {
            for (int j = 0; j < CaroBoard.BOARD_SIZE; j++) {
                boardData.append(boardArray[i][j]);
                if (i != CaroBoard.BOARD_SIZE - 1 || j != CaroBoard.BOARD_SIZE - 1) {
                    boardData.append(",");
                }
            }
        }
        
        String message = boardData.toString();
        
        // Send to both players
        if (player1 != null) {
            player1.sendMessage(message);
        }
        if (player2 != null) {
            player2.sendMessage(message);
        }
        
        System.out.println("Board update sent to both players: " + message.substring(0, Math.min(50, message.length())) + "...");
    }
    
    public void playerDisconnected(ClientHandler player) {
        gameActive = false;
        System.out.println("Player disconnected from game room");
        
        if (player == player1 && player2 != null) {
            player2.sendMessage("OPPONENT_DISCONNECTED");
        } else if (player == player2 && player1 != null) {
            player1.sendMessage("OPPONENT_DISCONNECTED");
        }
        
        endGame();
    }
    
    private void endGame() {
        System.out.println("Game ended, removing room");
        if (server != null) {
            server.removeGameRoom(this);
        }
    }
}