CREATE DATABASE IF NOT EXISTS fighting_game;
USE fighting_game;

-- Users table
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
);

-- Game rooms table
CREATE TABLE IF NOT EXISTS game_rooms (
    id INT PRIMARY KEY AUTO_INCREMENT,
    room_id VARCHAR(20) UNIQUE NOT NULL,
    host_username VARCHAR(50) NOT NULL,
    max_players INT DEFAULT 3,
    status ENUM('WAITING', 'STARTING', 'IN_PROGRESS', 'FINISHED') DEFAULT 'WAITING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP NULL,
    ended_at TIMESTAMP NULL,
    game_duration INT DEFAULT 300,
    FOREIGN KEY (host_username) REFERENCES users(username) ON DELETE CASCADE
);

-- Game history table
CREATE TABLE IF NOT EXISTS game_history (
    id INT PRIMARY KEY AUTO_INCREMENT,
    game_id INT UNIQUE NOT NULL,
    room_id VARCHAR(20) NOT NULL,
    winner_username VARCHAR(50),
    duration INT NOT NULL,
    total_players INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (winner_username) REFERENCES users(username) ON DELETE SET NULL
);

-- Game participants table (many-to-many relationship)
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
);

-- Player statistics table
CREATE TABLE IF NOT EXISTS player_stats (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    total_playtime INT DEFAULT 0, -- in seconds
    highest_score INT DEFAULT 0,
    total_kills INT DEFAULT 0,
    total_deaths INT DEFAULT 0,
    favorite_character VARCHAR(50) DEFAULT 'person1',
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);

-- Achievements table
CREATE TABLE IF NOT EXISTS achievements (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    achievement_type VARCHAR(50) NOT NULL,
    achievement_name VARCHAR(100) NOT NULL,
    description TEXT,
    unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);

-- Friend relationships table
CREATE TABLE IF NOT EXISTS friendships (
    id INT PRIMARY KEY AUTO_INCREMENT,
    requester_username VARCHAR(50) NOT NULL,
    addressee_username VARCHAR(50) NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'BLOCKED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (requester_username) REFERENCES users(username) ON DELETE CASCADE,
    FOREIGN KEY (addressee_username) REFERENCES users(username) ON DELETE CASCADE,
    UNIQUE KEY unique_friendship (requester_username, addressee_username)
);

-- Insert default admin user
INSERT IGNORE INTO users (username, password, email, wins, losses) 
VALUES ('admin', 'admin123', 'admin@gamelab.com', 0, 0);

-- Insert sample users for testing
INSERT IGNORE INTO users (username, password, email, wins, losses) VALUES 
('player1', 'pass123', 'player1@test.com', 5, 3),
('player2', 'pass123', 'player2@test.com', 3, 4),
('player3', 'pass123', 'player3@test.com', 7, 2);

-- Insert initial player stats
INSERT IGNORE INTO player_stats (username, total_playtime, highest_score, total_kills, total_deaths) VALUES
('player1', 3600, 150, 25, 18),
('player2', 2400, 120, 18, 22),
('player3', 4800, 200, 35, 15);

-- Sample game history
INSERT IGNORE INTO game_history (game_id, room_id, winner_username, duration, total_players) VALUES
(1, 'ROOM1001', 'player1', 280, 3),
(2, 'ROOM1002', 'player3', 250, 2),
(3, 'ROOM1003', 'player2', 300, 3);

-- Sample game participants
INSERT IGNORE INTO game_participants (game_history_id, username, final_score, kills, deaths, rank_achieved) VALUES
(1, 'player1', 150, 8, 3, 3),
(1, 'player2', 100, 5, 5, 2),
(1, 'player3', 80, 4, 6, 1),
(2, 'player3', 120, 6, 2, 2),
(2, 'player1', 90, 4, 4, 1),
(3, 'player2', 110, 7, 3, 3),
(3, 'player1', 95, 5, 4, 2),
(3, 'player3', 85, 3, 5, 1);

-- Create indexes for better performance
-- CREATE INDEX idx_users_username ON users(username);
-- CREATE INDEX idx_game_history_room_id ON game_history(room_id);
-- CREATE INDEX idx_game_participants_username ON game_participants(username);
-- CREATE INDEX idx_player_stats_username ON player_stats(username);
-- CREATE INDEX idx_game_history_created_at ON game_history(created_at);

-- Views for easier querying
CREATE OR REPLACE VIEW user_game_stats AS
SELECT 
    u.username,
    u.wins,
    u.losses,
    u.total_games,
    ps.total_playtime,
    ps.highest_score,
    ps.total_kills,
    ps.total_deaths,
    CASE 
        WHEN ps.total_deaths > 0 THEN ROUND(ps.total_kills / ps.total_deaths, 2)
        ELSE ps.total_kills
    END as kd_ratio
FROM users u
LEFT JOIN player_stats ps ON u.username = ps.username;

CREATE OR REPLACE VIEW recent_games AS
SELECT 
    gh.game_id,
    gh.room_id,
    gh.winner_username,
    gh.duration,
    gh.total_players,
    gh.created_at,
    GROUP_CONCAT(gp.username ORDER BY gp.final_score DESC) as participants
FROM game_history gh
LEFT JOIN game_participants gp ON gh.id = gp.game_history_id
GROUP BY gh.id
ORDER BY gh.created_at DESC;

DELIMITER //

-- Stored procedure to update user stats after game
CREATE PROCEDURE UpdateUserStatsAfterGame(
    IN p_username VARCHAR(50),
    IN p_won BOOLEAN,
    IN p_score INT,
    IN p_kills INT,
    IN p_deaths INT,
    IN p_playtime INT
)
BEGIN
    DECLARE current_wins INT DEFAULT 0;
    DECLARE current_losses INT DEFAULT 0;
    DECLARE current_total_games INT DEFAULT 0;
    
    -- Update wins/losses in users table
    IF p_won THEN
        UPDATE users SET wins = wins + 1, total_games = total_games + 1 WHERE username = p_username;
    ELSE
        UPDATE users SET losses = losses + 1, total_games = total_games + 1 WHERE username = p_username;
    END IF;
    
    -- Update player stats
    INSERT INTO player_stats (username, total_playtime, highest_score, total_kills, total_deaths)
    VALUES (p_username, p_playtime, p_score, p_kills, p_deaths)
    ON DUPLICATE KEY UPDATE
        total_playtime = total_playtime + p_playtime,
        highest_score = GREATEST(highest_score, p_score),
        total_kills = total_kills + p_kills,
        total_deaths = total_deaths + p_deaths;
END //

DELIMITER ;