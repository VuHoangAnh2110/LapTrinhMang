package shared.models;

import shared.utils.Constants;
import java.io.Serializable;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String username;
    private int x, y;
    private int health;
    private boolean isAttacking;
    private String direction;
    private long lastMoveTime;
    
    public Player(int id, String username) {
        this.id = id;
        this.username = username;
        this.x = 100;
        this.y = Constants.WINDOW_HEIGHT - 100;
        this.health = Constants.MAX_HEALTH;
        this.isAttacking = false;
        this.direction = "right";
        this.lastMoveTime = System.currentTimeMillis();
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public int getX() { return x; }
    public void setX(int x) { 
        this.x = x; 
        this.lastMoveTime = System.currentTimeMillis();
    }
    
    public int getY() { return y; }
    public void setY(int y) { 
        this.y = y; 
        this.lastMoveTime = System.currentTimeMillis();
    }
    
    public int getHealth() { return health; }
    public void setHealth(int health) { 
        this.health = Math.max(0, Math.min(Constants.MAX_HEALTH, health)); 
    }
    
    public boolean isAttacking() { return isAttacking; }
    public void setAttacking(boolean attacking) { this.isAttacking = attacking; }
    
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    
    public long getLastMoveTime() { return lastMoveTime; }
    
    public boolean isAlive() { return health > 0; }
    
    @Override
    public String toString() {
        return "Player{" +
                "username='" + username + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", health=" + health +
                ", direction='" + direction + '\'' +
                '}';
    }
}