package server.database;

import shared.models.Player;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserDAO {
    private Connection connection;
    
    public UserDAO() throws SQLException {
        this.connection = DatabaseManager.getInstance().getConnection();
    }
    
    public boolean createUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password));
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            return false;
        }
    }
    
    public boolean authenticateUser(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                return storedPassword.equals(hashPassword(password));
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            return false;
        }
    }
    
    public boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error checking user existence: " + e.getMessage());
            return false;
        }
    }
    
    public void updateUserStats(String username, boolean isWin) {
        String sql = isWin ? 
            "UPDATE users SET wins = wins + 1 WHERE username = ?" :
            "UPDATE users SET losses = losses + 1 WHERE username = ?";
            
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating user stats: " + e.getMessage());
        }
    }
    
    public int[] getUserStats(String username) {
        String sql = "SELECT wins, losses FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new int[]{rs.getInt("wins"), rs.getInt("losses")};
            }
            return new int[]{0, 0};
        } catch (SQLException e) {
            System.err.println("Error getting user stats: " + e.getMessage());
            return new int[]{0, 0};
        }
    }
    
    public void saveGameSession(String player1, String player2, String winner, int duration) {
        String sql = "INSERT INTO game_sessions (player1_id, player2_id, winner_id, duration) " +
                    "VALUES ((SELECT id FROM users WHERE username = ?), " +
                    "(SELECT id FROM users WHERE username = ?), " +
                    "(SELECT id FROM users WHERE username = ?), ?)";
                    
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, player1);
            stmt.setString(2, player2);
            stmt.setString(3, winner);
            stmt.setInt(4, duration);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving game session: " + e.getMessage());
        }
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}