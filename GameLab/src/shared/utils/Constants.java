package shared.utils;

import java.io.InputStream;
import java.util.Properties;

public class Constants {
    // Default values
    public static final int WINDOW_WIDTH;
    public static final int WINDOW_HEIGHT;
    public static final int PLAYER_WIDTH = 40;
    public static final int PLAYER_HEIGHT = 60;
    public static final int PLAYER_SPEED;
    public static final int MAX_HEALTH;
    public static final int ATTACK_DAMAGE;
    public static final int ATTACK_RANGE;
    public static final int ATTACK_COOLDOWN;
    public static final String SERVER_HOST;
    public static final int SERVER_PORT;
    public static final int CONNECTION_TIMEOUT;
    
    // Load configuration
    static {
        Properties props = new Properties();
        try (InputStream is = Constants.class.getResourceAsStream("/resources/config.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception e) {
            System.err.println("Could not load config.properties, using defaults: " + e.getMessage());
        }
        
        // Initialize constants from properties with defaults
        WINDOW_WIDTH = Integer.parseInt(props.getProperty("game.window_width", "800"));
        WINDOW_HEIGHT = Integer.parseInt(props.getProperty("game.window_height", "600"));
        PLAYER_SPEED = Integer.parseInt(props.getProperty("game.player_speed", "5"));
        MAX_HEALTH = Integer.parseInt(props.getProperty("game.max_health", "100"));
        ATTACK_DAMAGE = Integer.parseInt(props.getProperty("game.attack_damage", "10"));
        ATTACK_RANGE = Integer.parseInt(props.getProperty("game.attack_range", "50"));
        ATTACK_COOLDOWN = Integer.parseInt(props.getProperty("game.attack_cooldown", "500"));
        SERVER_HOST = props.getProperty("server.host", "localhost");
        SERVER_PORT = Integer.parseInt(props.getProperty("server.port", "12345"));
        CONNECTION_TIMEOUT = Integer.parseInt(props.getProperty("client.connection_timeout", "5000"));
    }
    
    // Game states
    public static final String GAME_STATE_WAITING = "WAITING";
    public static final String GAME_STATE_FIGHTING = "FIGHTING";
    public static final String GAME_STATE_FINISHED = "FINISHED";
    
    // Colors (for consistency)
    public static final String COLOR_PLAYER_SELF = "#0066FF";
    public static final String COLOR_PLAYER_OTHER = "#FF0000";
    public static final String COLOR_BACKGROUND = "#87CEEB";
    public static final String COLOR_GROUND = "#228B22";
    
    // Utility method to get property with default
    public static String getProperty(String key, String defaultValue) {
        Properties props = new Properties();
        try (InputStream is = Constants.class.getResourceAsStream("/resources/config.properties")) {
            if (is != null) {
                props.load(is);
                return props.getProperty(key, defaultValue);
            }
        } catch (Exception e) {
            // Ignore and use default
        }
        return defaultValue;
    }
}