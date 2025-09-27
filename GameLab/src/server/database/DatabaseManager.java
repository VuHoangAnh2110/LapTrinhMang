package server.database;

import server.utils.Logger;
import shared.models.GameHistory;
import shared.models.PlayerState;

import java.sql.*;
import java.util.*;
import java.io.FileInputStream;
import java.io.IOException;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private Properties config;
    private boolean isConnected;
    
    // Database configuration
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    
    private DatabaseManager() {
        loadConfiguration();
        isConnected = false;
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    private void loadConfiguration() {
        config = new Properties();
        try {
            // Try to load from resources first
            try (FileInputStream fis = new FileInputStream("resources/config.properties")) {
                config.load(fis);
            } catch (IOException e) {
                // Use default values if config file not found
                Logger.logSync("Config file not found, using default database settings");
            }
            
            dbUrl = config.getProperty("database.url", "jdbc:mysql://localhost:3306/gamelab");
            dbUser = config.getProperty("database.user", "root");
            dbPassword = config.getProperty("database.password", "");
            
        } catch (Exception e) {
            Logger.logSync("Error loading configuration: " + e.getMessage());
            // Use default values
            dbUrl = "jdbc:mysql://localhost:3306/gamelab";
            dbUser = "root";
            dbPassword = "";
        }
    }
    
    public boolean initialize() {
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Create connection
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            connection.setAutoCommit(true);
            
            isConnected = true;
            Logger.logSync("Database connected successfully: " + dbUrl);
            
            // Create tables if they don't exist
            createTablesIfNotExist();
            
            return true;
            
        } catch (ClassNotFoundException e) {
            Logger.logSync("MySQL driver not found: " + e.getMessage());
            return false;
        } catch (SQLException e) {
            Logger.logSync("Database connection failed: " + e.getMessage());
            return false;
        }
    }
    
    private void createTablesIfNotExist() {
        try {
            Statement stmt = connection.createStatement();
            
            // Create users table
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(100),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_login TIMESTAMP NULL,
                    wins INT DEFAULT 0,
                    losses INT DEFAULT 0,
                    total_games INT DEFAULT 0,
                    total_score INT DEFAULT 0,
                    best_rank INT DEFAULT 1,
                    is_active BOOLEAN DEFAULT TRUE
                )
                """;
            stmt.executeUpdate(createUsersTable);
            
            // Create game_history table
            String createGameHistoryTable = """
                CREATE TABLE IF NOT EXISTS game_history (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    game_id INT UNIQUE NOT NULL,
                    room_id VARCHAR(20) NOT NULL,
                    winner_username VARCHAR(50),
                    duration INT NOT NULL,
                    total_players INT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (winner_username) REFERENCES users(username) ON DELETE SET NULL
                )
                """;
            stmt.executeUpdate(createGameHistoryTable);
            
            // Create game_participants table
            String createParticipantsTable = """
                CREATE TABLE IF NOT EXISTS game_participants (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    game_history_id INT NOT NULL,
                    username VARCHAR(50) NOT NULL,
                    final_score INT DEFAULT 0,
                    kills INT DEFAULT 0,
                    deaths INT DEFAULT 0,
                    rank_achieved INT DEFAULT 1,
                    damage_dealt INT DEFAULT 0,
                    FOREIGN KEY (game_history_id) REFERENCES game_history(id) ON DELETE CASCADE,
                    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
                    UNIQUE KEY unique_game_user (game_history_id, username)
                )
                """;
            stmt.executeUpdate(createParticipantsTable);
            
            stmt.close();
            Logger.logSync("Database tables verified/created successfully");
            
        } catch (SQLException e) {
            Logger.logSync("Error creating tables: " + e.getMessage());
        }
    }
    
    public boolean validateUser(String username, String password) {
        try {
            String query = "SELECT password FROM users WHERE username = ? AND is_active = TRUE";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                boolean isValid = password.equals(storedPassword); // Simple comparison (in production, use hashing)
                
                if (isValid) {
                    // Update last login
                    updateLastLogin(username);
                }
                
                rs.close();
                stmt.close();
                return isValid;
            }
            
            rs.close();
            stmt.close();
            return false;
            
        } catch (SQLException e) {
            Logger.logSync("Error validating user: " + e.getMessage());
            return false;
        }
    }
    
    public boolean createUser(String username, String password) {
        try {
            String query = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password); // In production, hash the password
            
            int result = stmt.executeUpdate();
            stmt.close();
            
            if (result > 0) {
                Logger.logSync("User created: " + username);
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            Logger.logSync("Error creating user: " + e.getMessage());
            return false;
        }
    }
    
    public Map<String, Object> getUserStats(String username) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            String query = "SELECT wins, losses, total_games, total_score FROM users WHERE username = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("wins", rs.getInt("wins"));
                stats.put("losses", rs.getInt("losses"));
                stats.put("total_games", rs.getInt("total_games"));
                stats.put("total_score", rs.getInt("total_score"));
            } else {
                // Return default stats if user not found
                stats.put("wins", 0);
                stats.put("losses", 0);
                stats.put("total_games", 0);
                stats.put("total_score", 0);
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            Logger.logSync("Error getting user stats: " + e.getMessage());
            // Return default stats on error
            stats.put("wins", 0);
            stats.put("losses", 0);
            stats.put("total_games", 0);
            stats.put("total_score", 0);
        }
        
        return stats;
    }
    
    public List<GameHistory> getUserGameHistory(String username) {
        List<GameHistory> history = new ArrayList<>();
        
        try {
            String query = """
                SELECT gh.game_id, gh.room_id, gh.winner_username, gh.duration, 
                       gh.total_players, gh.created_at,
                       GROUP_CONCAT(gp.username ORDER BY gp.final_score DESC) as players
                FROM game_history gh
                JOIN game_participants gp ON gh.id = gp.game_history_id
                WHERE gh.id IN (
                    SELECT DISTINCT game_history_id 
                    FROM game_participants 
                    WHERE username = ?
                )
                GROUP BY gh.id
                ORDER BY gh.created_at DESC
                LIMIT 20
                """;
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int gameId = rs.getInt("game_id");
                String roomId = rs.getString("room_id");
                String winner = rs.getString("winner_username");
                int duration = rs.getInt("duration");
                String playersStr = rs.getString("players");
                
                List<String> players = playersStr != null ? 
                    Arrays.asList(playersStr.split(",")) : new ArrayList<>();
                
                GameHistory gameHistory = new GameHistory(gameId, roomId, players, winner, duration);
                history.add(gameHistory);
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            Logger.logSync("Error getting user game history: " + e.getMessage());
        }
        
        return history;
    }
    
    public boolean saveGameHistory(GameHistory gameHistory, Map<String, PlayerState> finalStates) {
        try {
            connection.setAutoCommit(false);
            
            // Insert game history
            String insertHistory = """
                INSERT INTO game_history (game_id, room_id, winner_username, duration, total_players)
                VALUES (?, ?, ?, ?, ?)
                """;
            
            PreparedStatement historyStmt = connection.prepareStatement(insertHistory, Statement.RETURN_GENERATED_KEYS);
            historyStmt.setInt(1, gameHistory.getGameId());
            historyStmt.setString(2, gameHistory.getRoomId());
            historyStmt.setString(3, gameHistory.getWinner());
            historyStmt.setInt(4, gameHistory.getDuration());
            historyStmt.setInt(5, gameHistory.getPlayers().size());
            
            historyStmt.executeUpdate();
            
            // Get generated history ID
            ResultSet rs = historyStmt.getGeneratedKeys();
            int historyId = 0;
            if (rs.next()) {
                historyId = rs.getInt(1);
            }
            rs.close();
            historyStmt.close();
            
            // Insert participants
            String insertParticipant = """
                INSERT INTO game_participants (game_history_id, username, final_score, kills, deaths, rank_achieved)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
            
            PreparedStatement participantStmt = connection.prepareStatement(insertParticipant);
            
            for (String username : gameHistory.getPlayers()) {
                PlayerState player = finalStates.get(username);
                if (player != null) {
                    participantStmt.setInt(1, historyId);
                    participantStmt.setString(2, username);
                    participantStmt.setInt(3, player.getScore());
                    participantStmt.setInt(4, player.getKills());
                    participantStmt.setInt(5, player.getDeaths());
                    participantStmt.setInt(6, player.getRank());
                    participantStmt.addBatch();
                    
                    // Update user statistics
                    updateUserStats(username, username.equals(gameHistory.getWinner()), 
                                  player.getScore(), player.getKills(), player.getDeaths());
                }
            }
            
            participantStmt.executeBatch();
            participantStmt.close();
            
            connection.commit();
            connection.setAutoCommit(true);
            
            Logger.logSync("Game history saved: " + gameHistory.getRoomId());
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                Logger.logSync("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            Logger.logSync("Error saving game history: " + e.getMessage());
            return false;
        }
    }
    
    private void updateUserStats(String username, boolean won, int score, int kills, int deaths) {
        try {
            String query = """
                UPDATE users SET 
                    wins = wins + ?,
                    losses = losses + ?,
                    total_games = total_games + 1,
                    total_score = total_score + ?
                WHERE username = ?
                """;
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, won ? 1 : 0);
            stmt.setInt(2, won ? 0 : 1);
            stmt.setInt(3, score);
            stmt.setString(4, username);
            
            stmt.executeUpdate();
            stmt.close();
            
        } catch (SQLException e) {
            Logger.logSync("Error updating user stats for " + username + ": " + e.getMessage());
        }
    }
    
    private void updateLastLogin(String username) {
        try {
            String query = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE username = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            Logger.logSync("Error updating last login for " + username + ": " + e.getMessage());
        }
    }
    
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && isConnected;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                isConnected = false;
                Logger.logSync("Database connection closed");
            }
        } catch (SQLException e) {
            Logger.logSync("Error closing database connection: " + e.getMessage());
        }
    }
}