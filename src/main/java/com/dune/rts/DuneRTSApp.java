package com.dune.rts;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawner;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.time.LocalTimer;
import com.almasb.fxgl.ui.TextFactory;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.*;

import static com.almasb.fxgl.dsl.FXGL.*;

public class DuneRTSApp extends GameApplication {

    private Entity playerBase;
    private Entity enemyBase;
    
    private int playerSpice = 1000;
    private int enemySpice = 1000;
    
    private List<Entity> playerUnits = new ArrayList<>();
    private List<Entity> enemyUnits = new ArrayList<>();
    
    private Entity selectedUnit = null;
    private Rectangle selectionBox;
    
    private LocalTimer spiceTimer;
    private LocalTimer enemyAITimer;
    
    private EntityType playerType = EntityType.PLAYER;
    private EntityType enemyType = EntityType.ENEMY;
    
    @Override
    protected void initSettings() {
        setWidth(1280);
        setHeight(720);
        setTitle("Dune RTS - Battle for Arrakis");
        setVersion("1.0");
    }
    
    @Override
    protected void onPreInit() {
        getGameWorld().addEntityFactory(new UnitFactory());
        getGameWorld().addEntityFactory(new BuildingFactory());
    }
    
    @Override
    protected void initInput() {
        Input input = getInput();
        
        // Left click - select unit or give order
        input.addAction(new UserAction("Select/Order") {
            @Override
            protected void onActionBegin() {
                handleLeftClick();
            }
        }, MouseButton.PRIMARY);
        
        // Right click - move selected unit
        input.addAction(new UserAction("Move Unit") {
            @Override
            protected void onActionBegin() {
                handleRightClick();
            }
        }, MouseButton.SECONDARY);
        
        // Keyboard shortcuts for building
        input.addAction(new UserAction("Build Barracks") {
            @Override
            protected void onActionBegin() {
                buildBuilding("barracks");
            }
        }, KeyCode.B);
        
        input.addAction(new UserAction("Build Refinery") {
            @Override
            protected void onActionBegin() {
                buildBuilding("refinery");
            }
        }, KeyCode.R);
        
        input.addAction(new UserAction("Build Airfield") {
            @Override
            protected void onActionBegin() {
                buildBuilding("airfield");
            }
        }, KeyCode.A);
        
        // Unit production shortcuts
        input.addAction(new UserAction("Train Infantry") {
            @Override
            protected void onActionBegin() {
                trainUnit("infantry");
            }
        }, KeyCode.I);
        
        input.addAction(new UserAction("Train Tank") {
            @Override
            protected void onActionBegin() {
                trainUnit("tank");
            }
        }, KeyCode.T);
        
        input.addAction(new UserAction("Train Aircraft") {
            @Override
            protected void onActionBegin() {
                trainUnit("aircraft");
            }
        }, KeyCode.P);
        
        input.addAction(new UserAction("Train Helicopter") {
            @Override
            protected void onActionBegin() {
                trainUnit("helicopter");
            }
        }, KeyCode.H);
    }
    
    @Override
    protected void initGame() {
        // Create ground texture (desert)
        createGround();
        
        // Create player base
        playerBase = spawn("base", new SpawnData(100, 360).put("owner", "player"));
        
        // Create enemy base
        enemyBase = spawn("base", new SpawnData(1180, 360).put("owner", "enemy"));
        
        // Initialize timers
        spiceTimer = createTimer();
        enemyAITimer = createTimer();
        
        // UI for resources
        var spiceText = createText("Spice: " + playerSpice, Font.font("Arial", 20), Color.GOLD);
        spiceText.setTranslateX(10);
        spiceText.setTranslateY(30);
        getGameScene().addUINode(spiceText);
        
        var enemySpiceText = createText("Enemy Spice: " + enemySpice, Font.font("Arial", 20), Color.RED);
        enemySpiceText.setTranslateX(10);
        enemySpiceText.setTranslateY(55);
        getGameScene().addUINode(enemySpiceText);
        
        // Instructions
        var instructions = createText(
            "Controls:\n" +
            "L-Click: Select unit\n" +
            "R-Click: Move/Attack\n" +
            "B: Build Barracks (200 spice)\n" +
            "R: Build Refinery (300 spice)\n" +
            "A: Build Airfield (400 spice)\n" +
            "I: Train Infantry (50 spice)\n" +
            "T: Train Tank (150 spice)\n" +
            "P: Train Aircraft (200 spice)\n" +
            "H: Train Helicopter (180 spice)",
            Font.font("Arial", 14), Color.WHITE
        );
        instructions.setTranslateX(10);
        instructions.setTranslateY(580);
        getGameScene().addUINode(instructions);
        
        // Start resource collection loop
        spiceTimer.capture(Duration.seconds(2), () -> {
            collectSpice();
        });
        
        // Start AI loop
        enemyAITimer.capture(Duration.seconds(3), () -> {
            runEnemyAI();
        });
    }
    
    private void createGround() {
        Rectangle ground = new Rectangle(1280, 720, Color.valueOf("#C2B280"));
        getGameScene().addUINode(ground);
    }
    
    private void collectSpice() {
        // Player collects spice from refineries
        long playerRefineries = getGameWorld().getEntitiesByType(BuildingType.REFINERY)
            .stream()
            .filter(e -> e.getObject("owner").equals("player"))
            .count();
        
        long enemyRefineries = getGameWorld().getEntitiesByType(BuildingType.REFINERY)
            .stream()
            .filter(e -> e.getObject("owner").equals("enemy"))
            .count();
        
        playerSpice += 10 + (int)playerRefineries * 15;
        enemySpice += 10 + (int)enemyRefineries * 15;
        
        updateUI();
    }
    
    private void updateUI() {
        getGameScene().removeUINodes(n -> n instanceof javafx.scene.text.Text);
        
        var spiceText = createText("Spice: " + playerSpice, Font.font("Arial", 20), Color.GOLD);
        spiceText.setTranslateX(10);
        spiceText.setTranslateY(30);
        getGameScene().addUINode(spiceText);
        
        var enemySpiceText = createText("Enemy Spice: " + enemySpice, Font.font("Arial", 20), Color.RED);
        enemySpiceText.setTranslateX(10);
        enemySpiceText.setTranslateY(55);
        getGameScene().addUINode(enemySpiceText);
        
        var instructions = createText(
            "Controls:\n" +
            "L-Click: Select unit\n" +
            "R-Click: Move/Attack\n" +
            "B: Build Barracks (200 spice)\n" +
            "R: Build Refinery (300 spice)\n" +
            "A: Build Airfield (400 spice)\n" +
            "I: Train Infantry (50 spice)\n" +
            "T: Train Tank (150 spice)\n" +
            "P: Train Aircraft (200 spice)\n" +
            "H: Train Helicopter (180 spice)",
            Font.font("Arial", 14), Color.WHITE
        );
        instructions.setTranslateX(10);
        instructions.setTranslateY(580);
        getGameScene().addUINode(instructions);
    }
    
    private void handleLeftClick() {
        double mouseX = getInput().getMousePosition().getX();
        double mouseY = getInput().getMousePosition().getY();
        
        // Check if clicking on a player unit
        for (Entity unit : playerUnits) {
            if (unit.getBounds().contains(mouseX, mouseY)) {
                selectedUnit = unit;
                highlightUnit(unit);
                return;
            }
        }
        
        selectedUnit = null;
    }
    
    private void handleRightClick() {
        if (selectedUnit == null) return;
        
        double mouseX = getInput().getMousePosition().getX();
        double mouseY = getInput().getMousePosition().getY();
        
        // Check if clicking on enemy unit or base
        boolean targetFound = false;
        
        if (enemyBase.getBounds().contains(mouseX, mouseY)) {
            orderAttack(selectedUnit, enemyBase);
            targetFound = true;
        }
        
        if (!targetFound) {
            for (Entity enemyUnit : enemyUnits) {
                if (enemyUnit.getBounds().contains(mouseX, mouseY)) {
                    orderAttack(selectedUnit, enemyUnit);
                    targetFound = true;
                    break;
                }
            }
        }
        
        if (!targetFound) {
            // Move to position
            moveTo(selectedUnit, mouseX, mouseY);
        }
    }
    
    private void highlightUnit(Entity unit) {
        // Visual feedback for selection
        unit.getViewComponent().optionalChildren().forEach(node -> {
            if (node instanceof Rectangle) {
                ((Rectangle) node).setStroke(Color.YELLOW);
                ((Rectangle) node).setStrokeWidth(3);
            }
        });
    }
    
    private void moveTo(Entity unit, double x, double y) {
        unit.getComponent(UnitComponent.class).ifPresent(comp -> {
            comp.setTargetPosition(x, y);
            comp.setTarget(null);
        });
    }
    
    private void orderAttack(Entity attacker, Entity target) {
        attacker.getComponent(UnitComponent.class).ifPresent(comp -> {
            comp.setTarget(target);
        });
    }
    
    private void buildBuilding(String buildingType) {
        double mouseX = getInput().getMousePosition().getX();
        double mouseY = getInput().getMousePosition().getY();
        
        int cost = getBuildingCost(buildingType);
        if (playerSpice < cost) return;
        
        playerSpice -= cost;
        
        BuildingType type;
        switch (buildingType) {
            case "barracks": type = BuildingType.BARRACKS; break;
            case "refinery": type = BuildingType.REFINERY; break;
            case "airfield": type = BuildingType.AIRFIELD; break;
            default: return;
        }
        
        spawn("building", new SpawnData(mouseX, mouseY)
            .put("type", type)
            .put("owner", "player"));
        
        updateUI();
    }
    
    private int getBuildingCost(String type) {
        switch (type) {
            case "barracks": return 200;
            case "refinery": return 300;
            case "airfield": return 400;
            default: return 0;
        }
    }
    
    private void trainUnit(String unitType) {
        int cost = getUnitCost(unitType);
        if (playerSpice < cost) return;
        
        // Check if required building exists
        if (!hasRequiredBuilding(unitType, "player")) return;
        
        playerSpice -= cost;
        
        UnitType type;
        switch (unitType) {
            case "infantry": type = UnitType.INFANTRY; break;
            case "tank": type = UnitType.TANK; break;
            case "aircraft": type = UnitType.AIRCRAFT; break;
            case "helicopter": type = UnitType.HELICOPTER; break;
            default: return;
        }
        
        Entity unit = spawn("unit", new SpawnData(playerBase.getX() + 50, playerBase.getY())
            .put("type", type)
            .put("owner", "player"));
        
        playerUnits.add(unit);
        updateUI();
    }
    
    private int getUnitCost(String type) {
        switch (type) {
            case "infantry": return 50;
            case "tank": return 150;
            case "aircraft": return 200;
            case "helicopter": return 180;
            default: return 0;
        }
    }
    
    private boolean hasRequiredBuilding(String unitType, String owner) {
        if (unitType.equals("infantry")) {
            return getGameWorld().getEntitiesByType(BuildingType.BARRACKS)
                .stream()
                .anyMatch(e -> e.getObject("owner").equals(owner));
        } else if (unitType.equals("tank")) {
            return getGameWorld().getEntitiesByType(BuildingType.BARRACKS)
                .stream()
                .anyMatch(e -> e.getObject("owner").equals(owner));
        } else if (unitType.equals("aircraft") || unitType.equals("helicopter")) {
            return getGameWorld().getEntitiesByType(BuildingType.AIRFIELD)
                .stream()
                .anyMatch(e -> e.getObject("owner").equals(owner));
        }
        return false;
    }
    
    private void runEnemyAI() {
        Random rand = new Random();
        
        // AI builds structures
        long enemyBarracks = getGameWorld().getEntitiesByType(BuildingType.BARRACKS)
            .stream()
            .filter(e -> e.getObject("owner").equals("enemy"))
            .count();
        
        long enemyAirfield = getGameWorld().getEntitiesByType(BuildingType.AIRFIELD)
            .stream()
            .filter(e -> e.getObject("owner").equals("enemy"))
            .count();
        
        if (enemySpice >= 200 && enemyBarracks == 0 && rand.nextDouble() < 0.3) {
            enemySpice -= 200;
            spawn("building", new SpawnData(1100, 300 + rand.nextInt(200))
                .put("type", BuildingType.BARRACKS)
                .put("owner", "enemy"));
        } else if (enemySpice >= 400 && enemyAirfield == 0 && rand.nextDouble() < 0.2) {
            enemySpice -= 400;
            spawn("building", new SpawnData(1050, 300 + rand.nextInt(200))
                .put("type", BuildingType.AIRFIELD)
                .put("owner", "enemy"));
        }
        
        // AI trains units
        if (rand.nextDouble() < 0.4) {
            if (enemySpice >= 50 && enemyBarracks > 0) {
                enemySpice -= 50;
                Entity unit = spawn("unit", new SpawnData(enemyBase.getX() - 50, enemyBase.getY())
                    .put("type", UnitType.INFANTRY)
                    .put("owner", "enemy"));
                enemyUnits.add(unit);
            } else if (enemySpice >= 150 && enemyBarracks > 0 && rand.nextDouble() < 0.5) {
                enemySpice -= 150;
                Entity unit = spawn("unit", new SpawnData(enemyBase.getX() - 50, enemyBase.getY())
                    .put("type", UnitType.TANK)
                    .put("owner", "enemy"));
                enemyUnits.add(unit);
            } else if (enemySpice >= 200 && enemyAirfield > 0 && rand.nextDouble() < 0.3) {
                enemySpice -= 200;
                Entity unit = spawn("unit", new SpawnData(enemyBase.getX() - 50, enemyBase.getY())
                    .put("type", UnitType.AIRCRAFT)
                    .put("owner", "enemy"));
                enemyUnits.add(unit);
            }
        }
        
        // AI attacks with units
        for (Entity enemyUnit : enemyUnits) {
            enemyUnit.getComponent(UnitComponent.class).ifPresent(comp -> {
                if (comp.getTarget() == null || !comp.getTarget().isActive()) {
                    // Find nearest player unit or base
                    Entity target = findNearestEnemy(enemyUnit);
                    if (target != null) {
                        comp.setTarget(target);
                    }
                }
            });
        }
        
        updateUI();
    }
    
    private Entity findNearestEnemy(Entity from) {
        double minDist = Double.MAX_VALUE;
        Entity nearest = null;
        
        // Check player base
        double distToBase = FXGLMath.distance(from.getX(), from.getY(), playerBase.getX(), playerBase.getY());
        if (distToBase < minDist) {
            minDist = distToBase;
            nearest = playerBase;
        }
        
        // Check player units
        for (Entity playerUnit : playerUnits) {
            double dist = FXGLMath.distance(from.getX(), from.getY(), playerUnit.getX(), playerUnit.getY());
            if (dist < minDist) {
                minDist = dist;
                nearest = playerUnit;
            }
        }
        
        return nearest;
    }
    
    public enum EntityType {
        PLAYER, ENEMY
    }
    
    public enum UnitType {
        INFANTRY, TANK, AIRCRAFT, HELICOPTER
    }
    
    public enum BuildingType {
        BARRACKS, REFINERY, AIRFIELD
    }
}
