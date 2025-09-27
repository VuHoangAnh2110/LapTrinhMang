package shared;

import java.io.Serializable;

public class CaroBoard implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int BOARD_SIZE = 15;
    public static final int EMPTY = 0;
    public static final int PLAYER_X = 1;
    public static final int PLAYER_O = 2;
    
    private int[][] board;
    
    public CaroBoard() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        initBoard();
    }
    
    public void initBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = EMPTY;
            }
        }
    }
    
    public boolean makeMove(int row, int col, int player) {
        if (isValidMove(row, col)) {
            board[row][col] = player;
            return true;
        }
        return false;
    }
    
    public boolean isValidMove(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && 
               col >= 0 && col < BOARD_SIZE && 
               board[row][col] == EMPTY;
    }
    
    public int getCell(int row, int col) {
        return board[row][col];
    }
    
    public boolean checkWin(int row, int col, int player) {
        // Check horizontal
        if (checkDirection(row, col, 0, 1, player) || 
            // Check vertical
            checkDirection(row, col, 1, 0, player) || 
            // Check diagonal
            checkDirection(row, col, 1, 1, player) || 
            // Check anti-diagonal
            checkDirection(row, col, 1, -1, player)) {
            return true;
        }
        return false;
    }
    
    private boolean checkDirection(int row, int col, int dRow, int dCol, int player) {
        int count = 1; // Current piece
        
        // Check positive direction
        int r = row + dRow, c = col + dCol;
        while (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && 
               board[r][c] == player) {
            count++;
            r += dRow;
            c += dCol;
        }
        
        // Check negative direction
        r = row - dRow;
        c = col - dCol;
        while (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && 
               board[r][c] == player) {
            count++;
            r -= dRow;
            c -= dCol;
        }
        
        return count >= 5;
    }
    
    public boolean isBoardFull() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public int[][] getBoard() {
        return board;
    }
}