package com.dune.rts;

import com.almasb.fxgl.entity.component.Component;

public class BaseComponent extends Component {
    
    private String owner;
    private double health = 500;
    
    public BaseComponent(String owner) {
        this.owner = owner;
    }
    
    public String getOwner() {
        return owner;
    }
    
    public double getHealth() {
        return health;
    }
    
    public void takeDamage(double amount) {
        health -= amount;
        if (health <= 0) {
            // Base destroyed - game over logic could be added here
            getEntity().removeFromWorld();
            
            if (owner.equals("player")) {
                System.out.println("Player base destroyed! Game Over.");
            } else {
                System.out.println("Enemy base destroyed! Victory!");
            }
        }
    }
}
