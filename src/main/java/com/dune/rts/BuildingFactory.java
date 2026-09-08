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

public class BuildingFactory implements EntityFactory {
    
    @Spawns("building")
    public Entity newBuilding(SpawnData data) {
        DuneRTSApp.BuildingType type = data.get("type");
        String owner = data.get("owner");
        
        Rectangle view;
        double width, height;
        
        switch (type) {
            case BARRACKS:
                width = 60;
                height = 50;
                view = new Rectangle(width, height, owner.equals("player") ? Color.GREEN : Color.DARKGREEN);
                break;
                
            case REFINERY:
                width = 70;
                height = 60;
                view = new Rectangle(width, height, owner.equals("player") ? Color.GOLD : Color.SADDLEBROWN);
                break;
                
            case AIRFIELD:
                width = 80;
                height = 70;
                view = new Rectangle(width, height, owner.equals("player") ? Color.LIGHTGREEN : Color.OLIVE);
                break;
                
            default:
                width = 50;
                height = 50;
                view = new Rectangle(width, height, Color.GRAY);
        }
        
        Entity entity = entityBuilder()
            .bbox(new HitBox(BoundingShape.box(width, height)))
            .view(view)
            .with(new BuildingComponent(type, owner))
            .build();
        
        entity.setX(data.getX());
        entity.setY(data.getY());
        
        return entity;
    }
    
    @Spawns("base")
    public Entity newBase(SpawnData data) {
        String owner = data.get("owner");
        
        double width = 100;
        double height = 80;
        Rectangle view = new Rectangle(width, height, owner.equals("player") ? Color.BLUEVIOLET : Color.DARKRED);
        
        Entity entity = entityBuilder()
            .bbox(new HitBox(BoundingShape.box(width, height)))
            .view(view)
            .with(new BaseComponent(owner))
            .build();
        
        entity.setX(data.getX());
        entity.setY(data.getY());
        
        return entity;
    }
}
