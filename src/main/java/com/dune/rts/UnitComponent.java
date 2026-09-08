package com.dune.rts;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.time.LocalTimer;
import javafx.util.Duration;

import java.util.Optional;

public class UnitComponent extends Component {
    
    private double speed;
    private double health;
    private double maxHealth;
    private double damage;
    private double range;
    private DuneRTSApp.UnitType type;
    private String owner;
    
    private Entity target;
    private double targetX, targetY;
    private boolean moving = false;
    
    private LocalTimer attackTimer;
    
    public UnitComponent(double speed, double health, double damage, double range, 
                         DuneRTSApp.UnitType type, String owner) {
        this.speed = speed;
        this.health = health;
        this.maxHealth = health;
        this.damage = damage;
        this.range = range;
        this.type = type;
        this.owner = owner;
        
        attackTimer = new LocalTimer();
    }
    
    @Override
    public void onUpdate(double tpf) {
        // Movement logic
        if (moving && target == null) {
            moveToPosition(tpf);
        }
        
        // Combat logic
        if (target != null && target.isActive()) {
            double dist = FXGLMath.distance(getEntity().getX(), getEntity().getY(), 
                                           target.getX(), target.getY());
            
            if (dist <= range) {
                // In range - attack
                if (attackTimer.elapsedTime(Duration.seconds(1))) {
                    if (target != null && target.isActive()) {
                        target.getComponent(UnitComponent.class).ifPresent(tc -> {
                            tc.takeDamage(damage);
                        });
                        target.getComponent(BaseComponent.class).ifPresent(bc -> {
                            bc.takeDamage(damage);
                        });
                    }
                    attackTimer.capture();
                }
            } else {
                // Move towards target
                moveTowardsTarget(tpf);
            }
        } else if (target != null && !target.isActive()) {
            target = null;
        }
    }
    
    private void moveToPosition(double tpf) {
        Entity entity = getEntity();
        double dx = targetX - entity.getX();
        double dy = targetY - entity.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist < 5) {
            moving = false;
            return;
        }
        
        double moveX = (dx / dist) * speed * tpf;
        double moveY = (dy / dist) * speed * tpf;
        
        entity.setX(entity.getX() + moveX);
        entity.setY(entity.getY() + moveY);
    }
    
    private void moveTowardsTarget(double tpf) {
        Entity entity = getEntity();
        double dx = target.getX() - entity.getX();
        double dy = target.getY() - entity.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist < 5) {
            return;
        }
        
        double moveX = (dx / dist) * speed * tpf;
        double moveY = (dy / dist) * speed * tpf;
        
        entity.setX(entity.getX() + moveX);
        entity.setY(entity.getY() + moveY);
    }
    
    public void setTargetPosition(double x, double y) {
        this.targetX = x;
        this.targetY = y;
        this.target = null;
        this.moving = true;
    }
    
    public void setTarget(Entity target) {
        this.target = target;
        this.moving = false;
    }
    
    public Entity getTarget() {
        return target;
    }
    
    public void takeDamage(double amount) {
        health -= amount;
        if (health <= 0) {
            getEntity().removeFromWorld();
        }
    }
    
    public double getHealth() {
        return health;
    }
    
    public double getMaxHealth() {
        return maxHealth;
    }
    
    public String getOwner() {
        return owner;
    }
}
