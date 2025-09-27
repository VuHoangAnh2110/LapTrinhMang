package server.database;

import server.utils.Logger;

import java.sql.*;
import java.util.*;

public class UserDAO {
    private DatabaseManager dbManager;
    
    public UserDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    public boolean createUser(String username, String password, String email) {
        if (!dbManager.isConnected()) {
            Logger.logSync("Database not connected");
            return false;
        }
        
        try {
            String query = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
            // Note: In production, you should hash the password
            return executeUpdate(query, username, password, email) > 0;
        } catch (Exception e) {
            Logger.logSync("Error creating user: " + e.getMessage());
            return false;
        }
    }
    
    public boolean authenticateUser(String username, String password) {
        if (!dbManager.isConnected()) {
            Logger.logSync("Database not connected");
            return false;
        }
        
        try {
            String query = "SELECT password FROM users WHERE username = ? AND is_active = TRUE";
            Map<String, Object> result = executeQuery(query, username);
            
            if (result != null && result.containsKey("password")) {
                String storedPassword = (String) result.get("password");
                // Note: In production, you should hash and compare passwords
                return password.equals(storedPassword);
            }
            
            return false;
        } catch (Exception e) {
            Logger.logSync("Error authenticating user: " + e.getMessage());
            return false;
        }
    }
    
    public Map<String, Object> getUserProfile(String username) {
        if (!dbManager.isConnected()) {
            return new HashMap<>();
        }
        
        try {
            String query = """
                SELECT username, email, created_at, last_login, wins, losses, 
                       total_games, total_score, best_rank
                FROM users 
                WHERE username = ? AND is_active = TRUE
                """;
            
            return executeQuery(query, username);
        } catch (Exception e) {
            Logger.logSync("Error getting user profile: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    public boolean updateUserStats(String username, boolean won, int scoreGained) {
        if (!dbManager.isConnected()) {
            return false;
        }
        
        try {
            String query = """
                UPDATE users SET 
                    wins = wins + ?,
                    losses = losses + ?,
                    total_games = total_games + 1,
                    total_score = total_score + ?
                WHERE username = ?
                """;
            
            return executeUpdate(query, won ? 1 : 0, won ? 0 : 1, scoreGained, username) > 0;
        } catch (Exception e) {
            Logger.logSync("Error updating user stats: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateLastLogin(String username) {
        if (!dbManager.isConnected()) {
            return false;
        }
        
        try {
            String query = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE username = ?";
            return executeUpdate(query, username) > 0;
        } catch (Exception e) {
            Logger.logSync("Error updating last login: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deactivateUser(String username) {
        if (!dbManager.isConnected()) {
            return false;
        }
        
        try {
            String query = "UPDATE users SET is_active = FALSE WHERE username = ?";
            return executeUpdate(query, username) > 0;
        } catch (Exception e) {
            Logger.logSync("Error deactivating user: " + e.getMessage());
            return false;
        }
    }
    
    public List<Map<String, Object>> getTopPlayers(int limit) {
        List<Map<String, Object>> topPlayers = new ArrayList<>();
        
        if (!dbManager.isConnected()) {
            return topPlayers;
        }
        
        try {
            String query = """
                SELECT username, wins, losses, total_games, total_score,
                       CASE WHEN total_games > 0 
                            THEN ROUND((wins * 100.0 / total_games), 2) 
                            ELSE 0 
                       END as win_rate
                FROM users 
                WHERE is_active = TRUE AND total_games > 0
                ORDER BY wins DESC, win_rate DESC
                LIMIT ?
                """;
            
            topPlayers = executeQueryList(query, limit);
        } catch (Exception e) {
            Logger.logSync("Error getting top players: " + e.getMessage());
        }
        
        return topPlayers;
    }
    
    public boolean userExists(String username) {
        if (!dbManager.isConnected()) {
            return false;
        }
        
        try {
            String query = "SELECT COUNT(*) as count FROM users WHERE username = ?";
            Map<String, Object> result = executeQuery(query, username);
            
            if (result != null && result.containsKey("count")) {
                int count = ((Number) result.get("count")).intValue();
                return count > 0;
            }
            
            return false;
        } catch (Exception e) {
            Logger.logSync("Error checking if user exists: " + e.getMessage());
            return false;
        }
    }
    
    // Helper methods for database operations
    private Map<String, Object> executeQuery(String query, Object... params) {
        // This would need to be implemented with actual database connection
        // For now, returning null as placeholder
        return dbManager.getUserStats((String)params[0]); // Simplified
    }
    
    private List<Map<String, Object>> executeQueryList(String query, Object... params) {
        List<Map<String, Object>> results = new ArrayList<>();
        // This would need to be implemented with actual database connection
        // For now, returning empty list as placeholder
        return results;
    }
    
    private int executeUpdate(String query, Object... params) {
        // This would need to be implemented with actual database connection
        // For now, returning 1 as placeholder for success
        return 1;
    }
}