package shared.models;

import shared.utils.Constants;
import java.io.Serializable;

public class PlayerState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String username;
    private int x, y;
    private int health;
    private int maxHealth;
    private int damage;
    private int score;
    private int kills;
    private int deaths;
    private boolean isAttacking;
    private boolean isAlive;
    private boolean isReady;
    private String direction;
    private long lastMoveTime;
    private long respawnTime;
    private int rank;
    private long lastAttackTime;
    
    public PlayerState(int id, String username) {
        this.id = id;
        this.username = username;
        resetToDefault();
    }
    
    public void resetToDefault() {
        this.x = 100;
        this.y = Constants.WINDOW_HEIGHT - 100;
        this.health = Constants.MAX_HEALTH;
        this.maxHealth = Constants.MAX_HEALTH;
        this.damage = Constants.ATTACK_DAMAGE;
        this.score = 0;
        this.isAttacking = false;
        this.isAlive = true;
        this.isReady = false;
        this.direction = "right";
        this.lastMoveTime = System.currentTimeMillis();
        this.lastAttackTime = 0; 
        this.respawnTime = 0;
        this.rank = 1;
    }

    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health <= 0) {
            this.health = 0;
            die();
        }
    }
    
    public void die() {
        this.isAlive = false;
        this.deaths++;
        this.respawnTime = System.currentTimeMillis() + 6000; // 6 second respawn delay
    }
    
    public void respawn() {
        this.isAlive = true;
        this.health = this.maxHealth;
        this.respawnTime = 0;
        
        // Reset position to spawn point
        this.x = 100 + (int)(Math.random() * 200); // Random spawn
        this.y = Constants.WINDOW_HEIGHT - 100;
    }

    public void addKill() {
        this.kills++;
        this.score += 10; // Points for kill
        this.rank = Math.max(1, this.rank + 1);
    }
    
    public void addKill(PlayerState victim) {
        this.kills++;
        this.score += victim.getScore() / 2; // Half of victim's score
        this.damage += 5; // Increase damage
        this.rank++; // Increase rank
    }

    public boolean canAttack() {
        long attackCooldown = 500; // 500ms cooldown
        return System.currentTimeMillis() - lastAttackTime >= attackCooldown;
    }

    public void setLastAttackTime(long time) {
        this.lastAttackTime = time;
    }

    public long getLastAttackTime() {
        return lastAttackTime;
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
        this.health = Math.max(0, Math.min(this.maxHealth, health)); 
    }
    
    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }
    
    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }
    
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    
    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    
    public boolean isAttacking() { return isAttacking; }
    public void setAttacking(boolean attacking) { this.isAttacking = attacking; }
    
    public boolean isAlive() { return isAlive; }
    public void setAlive(boolean alive) { this.isAlive = alive; }
    
    public boolean isReady() { return isReady; }
    public void setReady(boolean ready) { this.isReady = ready; }
    
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    
    public long getLastMoveTime() { return lastMoveTime; }
    public long getRespawnTime() { return respawnTime; }
    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    
    public boolean canRespawn() {
        return !isAlive && respawnTime > 0 && System.currentTimeMillis() >= respawnTime;
    }
    
    @Override
    public String toString() {
        return "PlayerState{" +
                "username='" + username + '\'' +
                ", x=" + x + ", y=" + y +
                ", health=" + health + "/" + maxHealth +
                ", score=" + score + ", rank=" + rank +
                ", kills=" + kills + ", deaths=" + deaths +
                ", isAlive=" + isAlive +
                '}';
    }
}