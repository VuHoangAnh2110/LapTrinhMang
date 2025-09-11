package shared.models;

import java.io.Serializable;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String username;
    private int x, y;
    private int health;
    private boolean isAttacking;
    private String direction;
    
    public Player(int id, String username) {
        this.id = id;
        this.username = username;
        this.x = 100;
        this.y = 400;
        this.health = 100;
        this.isAttacking = false;
        this.direction = "right";
    }
    
    // Getters and setters
    public int getId() { return id; }
    public String getUsername() { return username; }
    
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }
    public boolean isAttacking() { return isAttacking; }
    public void setAttacking(boolean attacking) { isAttacking = attacking; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
}