package com.dune.rts;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class UnitFactory implements EntityFactory {
    
    @Spawns("unit")
    public Entity newUnit(SpawnData data) {
        DuneRTSApp.UnitType type = data.get("type");
        String owner = data.get("owner");
        
        Rectangle view;
        double width, height, speed, health, damage;
        double range;
        
        switch (type) {
            case INFANTRY:
                width = 20;
                height = 20;
                speed = 80;
                health = 50;
                damage = 10;
                range = 100;
                view = new Rectangle(width, height, owner.equals("player") ? Color.BLUE : Color.RED);
                break;
                
            case TANK:
                width = 40;
                height = 30;
                speed = 50;
                health = 150;
                damage = 30;
                range = 150;
                view = new Rectangle(width, height, owner.equals("player") ? Color.DARKBLUE : Color.DARKRED);
                break;
                
            case AIRCRAFT:
                width = 35;
                height = 25;
                speed = 120;
                health = 80;
                damage = 25;
                range = 200;
                view = new Rectangle(width, height, owner.equals("player") ? Color.CYAN : Color.ORANGE);
                break;
                
            case HELICOPTER:
                width = 30;
                height = 30;
                speed = 90;
                health = 100;
                damage = 20;
                range = 180;
                view = new Rectangle(width, height, owner.equals("player") ? Color.LIGHTBLUE : Color.CORAL);
                break;
                
            default:
                width = 20;
                height = 20;
                speed = 60;
                health = 50;
                damage = 10;
                range = 100;
                view = new Rectangle(width, height, Color.GRAY);
        }
        
        Entity entity = entityBuilder()
            .bbox(new HitBox(BoundingShape.box(width, height)))
            .view(view)
            .with(new UnitComponent(speed, health, damage, range, type, owner))
            .build();
        
        entity.setX(data.getX());
        entity.setY(data.getY());
        
        return entity;
    }
}
