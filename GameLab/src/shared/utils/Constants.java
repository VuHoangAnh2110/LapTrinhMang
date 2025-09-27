package shared.utils;

import java.io.InputStream;
import java.util.Properties;

public class Constants {
    // Game window dimensions (from config)
    public static final int WINDOW_WIDTH;
    public static final int WINDOW_HEIGHT;
    public static final int PLAYER_WIDTH = 40;
    public static final int PLAYER_HEIGHT = 60;
    public static final int PLAYER_SPEED;
    public static final int MAX_HEALTH;
    public static final int ATTACK_DAMAGE;
    public static final int ATTACK_RANGE;
    public static final int ATTACK_COOLDOWN;
    
    // Network configuration (from config)
    public static final String SERVER_HOST;
    public static final int SERVER_PORT;
    public static final int CONNECTION_TIMEOUT;
    
    // Game mechanics (new - can be made configurable later)
    public static final int MOVEMENT_SPEED;
    public static final int RESPAWN_TIME;
    public static final int GAME_DURATION;
    
    // Room settings (new)
    public static final int MAX_PLAYERS_PER_ROOM;
    public static final int MIN_PLAYERS_TO_START;
    
    // Network timeouts (new)
    public static final int READ_TIMEOUT;
    
    // Database configuration (new - should be configurable)
    public static final String DB_URL;
    public static final String DB_USER;
    public static final String DB_PASSWORD;
    
    // Load configuration
    static {
        Properties props = new Properties();
        try (InputStream is = Constants.class.getResourceAsStream("/config.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception e) {
            System.err.println("Could not load config.properties, using defaults: " + e.getMessage());
        }
        
        // Initialize constants from properties with defaults
        WINDOW_WIDTH = Integer.parseInt(props.getProperty("game.window_width", "1200"));
        WINDOW_HEIGHT = Integer.parseInt(props.getProperty("game.window_height", "800"));
        PLAYER_SPEED = Integer.parseInt(props.getProperty("game.player_speed", "5"));
        MAX_HEALTH = Integer.parseInt(props.getProperty("game.max_health", "100"));
        ATTACK_DAMAGE = Integer.parseInt(props.getProperty("game.attack_damage", "20"));
        ATTACK_RANGE = Integer.parseInt(props.getProperty("game.attack_range", "50"));
        ATTACK_COOLDOWN = Integer.parseInt(props.getProperty("game.attack_cooldown", "500"));
        SERVER_HOST = props.getProperty("server.host", "localhost");
        SERVER_PORT = Integer.parseInt(props.getProperty("server.port", "8888"));
        CONNECTION_TIMEOUT = Integer.parseInt(props.getProperty("client.connection_timeout", "5000"));
        
        // New game mechanics (can be made configurable later)
        MOVEMENT_SPEED = Integer.parseInt(props.getProperty("game.movement_speed", "5"));
        RESPAWN_TIME = Integer.parseInt(props.getProperty("game.respawn_time", "6000"));
        GAME_DURATION = Integer.parseInt(props.getProperty("game.duration", "300"));
        
        // Room settings
        MAX_PLAYERS_PER_ROOM = Integer.parseInt(props.getProperty("room.max_players", "3"));
        MIN_PLAYERS_TO_START = Integer.parseInt(props.getProperty("room.min_players", "2"));
        
        // Network timeouts
        READ_TIMEOUT = Integer.parseInt(props.getProperty("client.read_timeout", "10000"));
        
        // Database configuration
        DB_URL = props.getProperty("database.url", "jdbc:mysql://localhost:3306/gamelab");
        DB_USER = props.getProperty("database.user", "root");
        DB_PASSWORD = props.getProperty("database.password", "");
    }
    
    // Game states (improved with enum)
    public enum GameState {
        WAITING, STARTING, IN_PROGRESS, FINISHED;
        
        // For backward compatibility
        public static final String GAME_STATE_WAITING = "WAITING";
        public static final String GAME_STATE_FIGHTING = "IN_PROGRESS";
        public static final String GAME_STATE_FINISHED = "FINISHED";
    }
    
    // Colors (for consistency) - kept from original
    public static final String COLOR_PLAYER_SELF = "#0066FF";
    public static final String COLOR_PLAYER_OTHER = "#FF0000";
    public static final String COLOR_BACKGROUND = "#87CEEB";
    public static final String COLOR_GROUND = "#228B22";
    
    // Player animations (new)
    public static final String[] ANIMATION_STATES = {
        "idle", "run", "jump", "attack", "hit", "death"
    };
    
    // Audio files (new)
    public static final String BACKGROUND_MUSIC = "/sounds/background.mp3";
    public static final String ATTACK_SOUND = "/sounds/attack.wav";
    public static final String HIT_SOUND = "/sounds/hit.wav";
    public static final String DEATH_SOUND = "/sounds/death.wav";
    
    // Utility method to get property with default (kept from original)
    public static String getProperty(String key, String defaultValue) {
        Properties props = new Properties();
        try (InputStream is = Constants.class.getResourceAsStream("/config.properties")) {
            if (is != null) {
                props.load(is);
                return props.getProperty(key, defaultValue);
            }
        } catch (Exception e) {
            // Ignore and use default
        }
        return defaultValue;
    }
    
    private Constants() {
        // Utility class - prevent instantiation
    }
}